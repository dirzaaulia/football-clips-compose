package main

import (
	"bufio"
	"flag"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
)

type deployConfig struct {
	track     string
	priority  int
	dryRun    bool
	skipBuild bool
}

func main() {
	var isProd, isInternal, isForce, isPrompt, isSilent, dryRun, skipBuild bool
	var explicitTrack string
	var explicitPriority int

	flag.BoolVar(&isProd, "production", false, "Deploy to Google Play Production track")
	flag.BoolVar(&isInternal, "internal", false, "Deploy to Google Play Internal Testing track")
	flag.StringVar(&explicitTrack, "track", "", "Target track (internal, alpha, beta, production)")
	flag.BoolVar(&isForce, "force", false, "Immediate force update (Priority 5)")
	flag.BoolVar(&isPrompt, "prompt", false, "Flexible update prompt (Priority 2)")
	flag.BoolVar(&isSilent, "silent", false, "Silent release (Priority 0)")
	flag.IntVar(&explicitPriority, "priority", -1, "Explicit update priority (0-5)")
	flag.BoolVar(&dryRun, "dry-run", false, "Validate without pushing git or publishing")
	flag.BoolVar(&skipBuild, "skip-build", false, "Skip local pre-flight compile check")
	flag.Parse()

	cfg := resolveConfig(isProd, isInternal, explicitTrack, isForce, isPrompt, isSilent, explicitPriority, dryRun, skipBuild)
	rootDir := findRepoRoot()

	props, propPath := readVersionProperties(rootDir)
	major := props["VERSION_MAJOR"]
	minor := props["VERSION_MINOR"]
	patch := props["VERSION_PATCH"]
	currBuild, _ := strconv.Atoi(props["VERSION_BUILD"])
	targetBuild := currBuild
	if !cfg.dryRun {
		targetBuild = currBuild + 1
	}

	versionName := fmt.Sprintf("%s.%s.%s (%d)", major, minor, patch, targetBuild)
	if cfg.track != "production" {
		versionName = fmt.Sprintf("%s.%s.%s-%s.%d", major, minor, patch, cfg.track, targetBuild)
	}

	printBanner(cfg, versionName, targetBuild)

	if !cfg.skipBuild {
		runPreflight(rootDir)
	}

	if cfg.dryRun {
		fmt.Println("\n✨ Dry run completed successfully. No changes dispatched.")
		return
	}

	writeVersionProperties(propPath, props, targetBuild)
	commitAndTag(rootDir, targetBuild, versionName, cfg)
	publishGPP(rootDir, cfg)
}

func resolveConfig(isProd, isInternal bool, expTrack string, isForce, isPrompt, isSilent bool, expPriority int, dryRun, skipBuild bool) deployConfig {
	track := "internal"
	if isProd {
		track = "production"
	} else if isInternal {
		track = "internal"
	} else if expTrack != "" {
		track = expTrack
	} else if isForce || isPrompt || expPriority >= 0 {
		track = "production"
	}

	priority := 0
	if track == "production" {
		priority = 2
	}
	if isForce {
		priority = 5
	} else if isPrompt {
		priority = 2
	} else if isSilent {
		priority = 0
	} else if expPriority >= 0 {
		priority = expPriority
	}

	return deployConfig{track: track, priority: priority, dryRun: dryRun, skipBuild: skipBuild}
}

func findRepoRoot() string {
	dir, _ := os.Getwd()
	for {
		if _, err := os.Stat(filepath.Join(dir, "version.properties")); err == nil {
			return dir
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}
	return "."
}

func readVersionProperties(rootDir string) (map[string]string, string) {
	propPath := filepath.Join(rootDir, "version.properties")
	props := map[string]string{"VERSION_MAJOR": "3", "VERSION_MINOR": "0", "VERSION_PATCH": "1", "VERSION_BUILD": "89832570"}
	file, err := os.Open(propPath)
	if err != nil {
		return props, propPath
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line != "" && !strings.HasPrefix(line, "#") && strings.Contains(line, "=") {
			parts := strings.SplitN(line, "=", 2)
			props[strings.TrimSpace(parts[0])] = strings.TrimSpace(parts[1])
		}
	}
	return props, propPath
}

func writeVersionProperties(propPath string, props map[string]string, newBuild int) {
	content := fmt.Sprintf("# Auto-incremented by fc-deploy runner\nVERSION_BUILD=%d\nVERSION_MAJOR=%s\nVERSION_MINOR=%s\nVERSION_PATCH=%s\n",
		newBuild, props["VERSION_MAJOR"], props["VERSION_MINOR"], props["VERSION_PATCH"])
	_ = os.WriteFile(propPath, []byte(content), 0644)
}

func runPreflight(rootDir string) {
	fmt.Println("\n🔍 Running local pre-flight compile check...")
	gradleCmd := "./gradlew"
	if runtime.GOOS == "windows" {
		gradleCmd = "gradlew.bat"
	}
	cmd := exec.Command(filepath.Join(rootDir, gradleCmd), ":androidApp:compileReleaseKotlin")
	cmd.Dir = rootDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		fmt.Printf("❌ Pre-flight check failed: %v\n", err)
		os.Exit(1)
	}
	fmt.Println("✅ Pre-flight compilation passed!")
}

func commitAndTag(rootDir string, buildNum int, verName string, cfg deployConfig) {
	commitMsg := fmt.Sprintf("chore(release): bump VERSION_BUILD to %d [track: %s, priority: %d]", buildNum, cfg.track, cfg.priority)
	tagName := fmt.Sprintf("v%s-%d", strings.Split(verName, " ")[0], buildNum)

	execCmd(rootDir, "git", "add", "version.properties")
	execCmd(rootDir, "git", "commit", "-m", commitMsg)
	execCmd(rootDir, "git", "tag", "-a", tagName, "-m", fmt.Sprintf("Release %s", verName))
	execCmd(rootDir, "git", "push", "origin", "main")
	execCmd(rootDir, "git", "push", "origin", tagName)
}

func publishGPP(rootDir string, cfg deployConfig) {
	gradleCmd := "./gradlew"
	if runtime.GOOS == "windows" {
		gradleCmd = "gradlew.bat"
	}
	trackArg := fmt.Sprintf("-Ptrack=%s", cfg.track)
	priArg := fmt.Sprintf("-PupdatePriority=%d", cfg.priority)
	fmt.Printf("\n🚀 Publishing Release Bundle to Google Play (%s) via GPP...\n", strings.ToUpper(cfg.track))

	cmd := exec.Command(filepath.Join(rootDir, gradleCmd), ":androidApp:publishReleaseBundle", trackArg, priArg)
	cmd.Dir = rootDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		fmt.Printf("❌ GPP deployment failed: %v\n", err)
		os.Exit(1)
	}
	fmt.Println("\n🎉 Google Play deployment completed successfully via GPP!")
}

func execCmd(rootDir, name string, args ...string) {
	cmd := exec.Command(name, args...)
	cmd.Dir = rootDir
	_ = cmd.Run()
}

func printBanner(cfg deployConfig, verName string, buildNum int) {
	fmt.Println("==================================================")
	fmt.Println("⚽ FootballClips Native Go Release Deployment")
	fmt.Println("==================================================")
	fmt.Printf("📦 Target Track:    %s\n", strings.ToUpper(cfg.track))
	fmt.Printf("🔢 Version Name:    %s\n", verName)
	fmt.Printf("⚙️  Version Code:    %d\n", buildNum)
	fmt.Printf("⚡ Update Priority: Level %d\n", cfg.priority)
	fmt.Printf("🛡️  Dry Run Mode:    %t\n", cfg.dryRun)
	fmt.Println("==================================================")
}
