package fcm

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"strings"
	"sync"
	"time"
)

type serviceAccountKey struct {
	Type        string `json:"type"`
	ProjectID   string `json:"project_id"`
	PrivateKey  string `json:"private_key"`
	ClientEmail string `json:"client_email"`
	TokenURI    string `json:"token_uri"`
}

type TokenProvider struct {
	mu         sync.Mutex
	sa         *serviceAccountKey
	privKey    *rsa.PrivateKey
	cachedTok  string
	expiryTime time.Time
	httpClient *http.Client
}

func NewTokenProvider(credentialsPath string) (*TokenProvider, error) {
	if credentialsPath == "" {
		return nil, errors.New("credentials path is empty")
	}
	data, err := os.ReadFile(credentialsPath)
	if err != nil {
		return nil, fmt.Errorf("read credentials: %w", err)
	}

	var sa serviceAccountKey
	if err := json.Unmarshal(data, &sa); err != nil {
		return nil, fmt.Errorf("unmarshal service account: %w", err)
	}

	block, _ := pem.Decode([]byte(sa.PrivateKey))
	if block == nil {
		return nil, errors.New("failed to parse PEM private key")
	}

	parsedKey, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		return nil, fmt.Errorf("parse pkcs8 key: %w", err)
	}
	rsaKey, ok := parsedKey.(*rsa.PrivateKey)
	if !ok {
		return nil, errors.New("private key is not RSA")
	}

	if sa.TokenURI == "" {
		sa.TokenURI = "https://oauth2.googleapis.com/token"
	}

	return &TokenProvider{
		sa:         &sa,
		privKey:    rsaKey,
		httpClient: &http.Client{Timeout: 10 * time.Second},
	}, nil
}

func (tp *TokenProvider) ProjectID() string {
	if tp.sa == nil {
		return ""
	}
	return tp.sa.ProjectID
}

func (tp *TokenProvider) GetAccessToken() (string, error) {
	tp.mu.Lock()
	defer tp.mu.Unlock()

	if tp.cachedTok != "" && time.Now().Before(tp.expiryTime.Add(-5*time.Minute)) {
		return tp.cachedTok, nil
	}

	jwtStr, err := tp.createSignedJWT()
	if err != nil {
		return "", err
	}

	form := url.Values{}
	form.Set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
	form.Set("assertion", jwtStr)

	resp, err := tp.httpClient.Post(tp.sa.TokenURI, "application/x-www-form-urlencoded", strings.NewReader(form.Encode()))
	if err != nil {
		return "", fmt.Errorf("request token: %w", err)
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("token endpoint returned status %d: %s", resp.StatusCode, string(body))
	}

	var res struct {
		AccessToken string `json:"access_token"`
		ExpiresIn   int    `json:"expires_in"`
	}
	if err := json.Unmarshal(body, &res); err != nil {
		return "", fmt.Errorf("unmarshal token response: %w", err)
	}

	tp.cachedTok = res.AccessToken
	tp.expiryTime = time.Now().Add(time.Duration(res.ExpiresIn) * time.Second)
	return tp.cachedTok, nil
}

func (tp *TokenProvider) createSignedJWT() (string, error) {
	now := time.Now().UTC()
	header := base64.RawURLEncoding.EncodeToString([]byte(`{"alg":"RS256","typ":"JWT"}`))

	claims := map[string]any{
		"iss":   tp.sa.ClientEmail,
		"scope": "https://www.googleapis.com/auth/firebase.messaging",
		"aud":   tp.sa.TokenURI,
		"exp":   now.Add(time.Hour).Unix(),
		"iat":   now.Unix(),
	}
	payloadBytes, err := json.Marshal(claims)
	if err != nil {
		return "", err
	}
	payload := base64.RawURLEncoding.EncodeToString(payloadBytes)

	signingInput := header + "." + payload
	h := sha256.New()
	h.Write([]byte(signingInput))
	digest := h.Sum(nil)

	sig, err := rsa.SignPKCS1v15(rand.Reader, tp.privKey, crypto.SHA256, digest)
	if err != nil {
		return "", fmt.Errorf("sign jwt: %w", err)
	}
	signature := base64.RawURLEncoding.EncodeToString(sig)

	return signingInput + "." + signature, nil
}
