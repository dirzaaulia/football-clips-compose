package youtube

import (
	"context"
	"fmt"
	"log/slog"
	"regexp"
	"strings"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

type compiledRegexRule struct {
	Regex                         *regexp.Regexp
	Category                      string
	RejectionLabel                string
	OverrideIfPositiveHighlight  bool
}

type negativeCategoryRule struct {
	Category                      string
	RejectionLabel                string
	OverrideIfPositiveHighlight  bool
	Terms                         []string
}

// TaxonomyRegistry manages dynamic multilingual taxonomy rules in-memory with thread-safe hot reload.
type TaxonomyRegistry struct {
	mu             sync.RWMutex
	positiveTerms  []string
	negativeRules  []negativeCategoryRule
	regexRules     []compiledRegexRule
	lastUpdated    time.Time
	logger         *slog.Logger
}

// NewTaxonomyRegistry initializes an empty thread-safe taxonomy registry.
func NewTaxonomyRegistry(logger *slog.Logger) *TaxonomyRegistry {
	if logger == nil {
		logger = slog.Default()
	}
	r := &TaxonomyRegistry{
		logger: logger,
	}
	r.loadDefaultFallback()
	return r
}

// LoadFromDB fetches all taxonomy rules from PostgreSQL and rebuilds the in-memory index.
func (r *TaxonomyRegistry) LoadFromDB(ctx context.Context, repo ports.TaxonomyRepository) error {
	entries, err := repo.GetTaxonomyEntries(ctx)
	if err != nil {
		return fmt.Errorf("load taxonomy from db: %w", err)
	}
	if len(entries) == 0 {
		r.logger.Warn("[TAXONOMY] Database returned 0 entries, keeping existing rules")
		return nil
	}

	var positive []string
	var negative []negativeCategoryRule
	var regexRules []compiledRegexRule

	for _, e := range entries {
		if e.Category == "HIGHLIGHT_POSITIVE" {
			for _, t := range e.Terms {
				tLow := strings.ToLower(strings.TrimSpace(t))
				if tLow != "" {
					positive = append(positive, tLow)
				}
			}
			continue
		}

		var cleanTerms []string
		for _, t := range e.Terms {
			tLow := strings.ToLower(strings.TrimSpace(t))
			if tLow != "" {
				cleanTerms = append(cleanTerms, tLow)
			}
		}

		label := e.RejectionLabel
		if label == "" {
			label = e.Category
		}

		negative = append(negative, negativeCategoryRule{
			Category:                     e.Category,
			RejectionLabel:               label,
			OverrideIfPositiveHighlight: e.OverrideIfPositiveHighlight,
			Terms:                        cleanTerms,
		})

		for _, pat := range e.RegexPatterns {
			if strings.TrimSpace(pat) == "" {
				continue
			}
			re, err := regexp.Compile("(?i)" + pat)
			if err == nil {
				regexRules = append(regexRules, compiledRegexRule{
					Regex:                        re,
					Category:                     e.Category,
					RejectionLabel:               label,
					OverrideIfPositiveHighlight: e.OverrideIfPositiveHighlight,
				})
			}
		}
	}

	r.mu.Lock()
	r.positiveTerms = positive
	r.negativeRules = negative
	r.regexRules = regexRules
	r.lastUpdated = time.Now().UTC()
	r.mu.Unlock()

	r.logger.Info("[TAXONOMY_HOT_RELOAD] Successfully reloaded taxonomy rules",
		slog.Int("positive_terms", len(positive)),
		slog.Int("negative_categories", len(negative)),
		slog.Int("regex_rules", len(regexRules)),
	)
	return nil
}

// StartAutoRefresh starts a background ticker that periodically reloads taxonomy rules from DB.
func (r *TaxonomyRegistry) StartAutoRefresh(ctx context.Context, repo ports.TaxonomyRepository, interval time.Duration) {
	go func() {
		ticker := time.NewTicker(interval)
		defer ticker.Stop()

		for {
			select {
			case <-ctx.Done():
				return
			case <-ticker.C:
				if err := r.LoadFromDB(ctx, repo); err != nil {
					r.logger.Warn("[TAXONOMY_AUTO_REFRESH] Failed reloading taxonomy", slog.String("err", err.Error()))
				}
			}
		}
	}()
}

// HasPositiveHighlight checks if title contains recognized positive highlight tokens.
func (r *TaxonomyRegistry) HasPositiveHighlight(title string) bool {
	r.mu.RLock()
	defer r.mu.RUnlock()
	tLow := strings.ToLower(title)
	for _, term := range r.positiveTerms {
		if strings.Contains(tLow, term) {
			return true
		}
	}
	return false
}

// Evaluate evaluates a video title and returns a structured classification verdict.
func (r *TaxonomyRegistry) Evaluate(title string) domain.TaxonomyVerdict {
	r.mu.RLock()
	defer r.mu.RUnlock()

	tLow := strings.ToLower(title)
	hasPositive := false
	for _, term := range r.positiveTerms {
		if strings.Contains(tLow, term) {
			hasPositive = true
			break
		}
	}

	// 1. Check Negative Terms across categories
	for _, catRule := range r.negativeRules {
		for _, term := range catRule.Terms {
			if strings.Contains(tLow, term) {
				if catRule.OverrideIfPositiveHighlight && hasPositive {
					continue
				}
				return domain.TaxonomyVerdict{
					IsNegative:  true,
					Category:    catRule.Category,
					Reason:      catRule.RejectionLabel,
					MatchedTerm: term,
				}
			}
		}
	}

	// 2. Check Compiled Regex Patterns
	for _, regRule := range r.regexRules {
		if regRule.Regex.MatchString(title) {
			if regRule.OverrideIfPositiveHighlight && hasPositive {
				continue
			}
			return domain.TaxonomyVerdict{
				IsNegative:  true,
				Category:    regRule.Category,
				Reason:      regRule.RejectionLabel,
				MatchedTerm: regRule.Regex.String(),
			}
		}
	}

	return domain.TaxonomyVerdict{
		IsNegative: false,
	}
}

func (r *TaxonomyRegistry) loadDefaultFallback() {
	r.positiveTerms = []string{
		"highlights", "highlight", "resumen", "sintesi", "samenvatting",
		"goals", "goles", "buts", "tore", "cuplikan", "özet", "στιγμιότυπα",
	}
	r.negativeRules = []negativeCategoryRule{
		{
			Category:       "PRESS_CONFERENCE",
			RejectionLabel: "Press Conference",
			Terms: []string{
				"press conference", "presser", "conferenza", "rueda de prensa",
				"conférence", "pressekonferenz",
			},
		},
		{
			Category:       "INTERVIEW",
			RejectionLabel: "Interview/Vlog",
			Terms: []string{
				"interview", "intervista", "entrevista", "commenta",
				"dichiarazioni", "aftermovie", "vlog", "all access",
			},
		},
	}
	r.lastUpdated = time.Now().UTC()
}
