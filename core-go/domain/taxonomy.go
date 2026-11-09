package domain

import "time"

// TaxonomyEntry models a classification rule set from PostgreSQL highlight_taxonomy.
type TaxonomyEntry struct {
	ID                         string    `json:"id"`
	Category                   string    `json:"category"`
	Language                   string    `json:"language"`
	Terms                      []string  `json:"terms"`
	RegexPatterns              []string  `json:"regex_patterns"`
	OverrideIfPositiveHighlight bool      `json:"override_if_positive_highlight"`
	RejectionLabel             string    `json:"rejection_label"`
	UpdatedAt                  time.Time `json:"updated_at,omitempty"`
}

// TaxonomyVerdict represents the outcome of evaluating a candidate title against taxonomy rules.
type TaxonomyVerdict struct {
	IsNegative bool
	Category   string
	Reason     string
	MatchedTerm string
}
