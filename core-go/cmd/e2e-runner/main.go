package main

import (
	"flag"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

type suiteResult struct {
	name        string
	passed      bool
	durationSec float64
	summary     string
}

func runStep(name string, dir string, command string, args ...string) suiteResult {
	fmt.Printf("\n============================================================\n")
	fmt.Printf("[RUNNING] %s\n", name)
	fmt.Printf("          Command: %s %s\n", command, strings.Join(args, " "))
	fmt.Printf("============================================================\n")

	start := time.Now()
	cmd := exec.Command(command, args...)
	cmd.Dir = dir

	output, err := cmd.CombinedOutput()
	duration := time.Since(start).Seconds()

	passed := err == nil
	status := "PASS"
	if !passed {
		status = "FAIL"
	}
	fmt.Printf("[%s] %s finished in %.2fs\n", status, name, duration)

	outStr := strings.TrimSpace(string(output))
	lines := strings.Split(outStr, "\n")
	summary := status
	if len(lines) > 0 {
		summary = strings.TrimSpace(lines[len(lines)-1])
	}

	return suiteResult{
		name:        name,
		passed:      passed,
		durationSec: duration,
		summary:     summary,
	}
}

func main() {
	allFlag := flag.Bool("all", false, "Run all suites")
	goFlag := flag.Bool("go", false, "Run Go test suite")
	pyFlag := flag.Bool("backend", false, "Run Python pytest suite")
	webFlag := flag.Bool("web", false, "Run Next.js web test suite")
	androidFlag := flag.Bool("android", false, "Run Android CMP test suite")
	flag.Parse()

	runAll := *allFlag || (!*goFlag && !*pyFlag && !*webFlag && !*androidFlag)
	rootDir, _ := filepath.Abs("../..")
	if _, err := os.Stat(filepath.Join(rootDir, "core-go")); err != nil {
		rootDir, _ = filepath.Abs(".")
	}

	var results []suiteResult

	goBin := "go"
	if _, err := exec.LookPath("go"); err != nil {
		if _, err2 := os.Stat(`C:\Program Files\Go\bin\go.exe`); err2 == nil {
			goBin = `C:\Program Files\Go\bin\go.exe`
		}
	}

	if runAll || *goFlag {
		results = append(results, runStep(
			"Golang 1.23+ Core Domain & Parity Suite (go test)",
			filepath.Join(rootDir, "core-go"),
			goBin, "test", "-v", "./...",
		))
	}

	if runAll || *pyFlag {
		results = append(results, runStep(
			"Python Backend & E2E Lifecycle Suite (pytest)",
			rootDir,
			"uv", "run", "pytest", "worker/tests/", "-q",
		))
	}

	if runAll || *webFlag {
		npmCmd := "npm"
		if os.PathSeparator == '\\' {
			npmCmd = "npm.cmd"
		}
		results = append(results, runStep(
			"Next.js 16 Web Vitest Suite",
			filepath.Join(rootDir, "web"),
			npmCmd, "run", "test",
		))
	}

	if runAll || *androidFlag {
		gradleCmd := "./gradlew"
		if os.PathSeparator == '\\' {
			gradleCmd = "gradlew.bat"
		}
		results = append(results, runStep(
			"Android CMP Host Suite & Device Matrix",
			rootDir,
			filepath.Join(rootDir, gradleCmd), ":composeApp:testAndroidHostTest",
		))
	}

	writeReport(rootDir, results)
}

func writeReport(rootDir string, results []suiteResult) {
	reportPath := filepath.Join(rootDir, "tests", "reports", "e2e_report.md")
	_ = os.MkdirAll(filepath.Dir(reportPath), 0755)

	allPassed := true
	for _, r := range results {
		if !r.passed {
			allPassed = false
			break
		}
	}

	overall := "PASSED (100%)"
	if !allPassed {
		overall = "FAILED"
	}

	var b strings.Builder
	b.WriteString("# FootballClips Hermetic E2E & Responsive Matrix Verification Report\n\n")
	b.WriteString(fmt.Sprintf("- **Timestamp**: `%s UTC`\n", time.Now().UTC().Format("2006-01-02 15:04:05")))
	b.WriteString(fmt.Sprintf("- **Overall Status**: `%s`\n\n", overall))
	b.WriteString("## Suite Execution Waterfall\n\n")
	b.WriteString("| Layer / Suite | Status | Duration (s) | Summary |\n")
	b.WriteString("| :--- | :--- | :---: | :--- |\n")

	for _, r := range results {
		status := "PASS"
		if !r.passed {
			status = "FAIL"
		}
		cleanSummary := strings.ReplaceAll(r.summary, "|", "/")
		b.WriteString(fmt.Sprintf("| **%s** | `%s` | `%.2fs` | %s |\n", r.name, status, r.durationSec, cleanSummary))
	}

	_ = os.WriteFile(reportPath, []byte(b.String()), 0644)
	fmt.Printf("\n[REPORT] Written to %s\n", reportPath)
}
