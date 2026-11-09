package postgres

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

var _ ports.TaxonomyRepository = (*Repository)(nil)

// GetTaxonomyEntries fetches all classification taxonomy rules from PostgreSQL.
func (r *Repository) GetTaxonomyEntries(ctx context.Context) ([]domain.TaxonomyEntry, error) {
	query := `
		SELECT id, category, language, terms, regex_patterns,
		       override_if_positive_highlight, COALESCE(rejection_label, '')
		FROM highlight_taxonomy
		ORDER BY category ASC, id ASC`

	rows, err := r.pool.Query(ctx, query)
	if err != nil {
		return nil, fmt.Errorf("query highlight_taxonomy: %w", err)
	}
	defer rows.Close()

	var entries []domain.TaxonomyEntry
	for rows.Next() {
		var e domain.TaxonomyEntry
		var termsRaw, regexRaw []byte
		if err := rows.Scan(
			&e.ID, &e.Category, &e.Language, &termsRaw, &regexRaw,
			&e.OverrideIfPositiveHighlight, &e.RejectionLabel,
		); err != nil {
			return nil, fmt.Errorf("scan taxonomy row: %w", err)
		}

		if len(termsRaw) > 0 {
			_ = json.Unmarshal(termsRaw, &e.Terms)
		}
		if len(regexRaw) > 0 {
			_ = json.Unmarshal(regexRaw, &e.RegexPatterns)
		}
		entries = append(entries, e)
	}
	return entries, nil
}

// UpsertTaxonomyEntry saves or updates a taxonomy entry in PostgreSQL.
func (r *Repository) UpsertTaxonomyEntry(ctx context.Context, entry domain.TaxonomyEntry) error {
	termsJSON, _ := json.Marshal(entry.Terms)
	regexJSON, _ := json.Marshal(entry.RegexPatterns)

	query := `
		INSERT INTO highlight_taxonomy (
			id, category, language, terms, regex_patterns,
			override_if_positive_highlight, rejection_label, updated_at
		) VALUES ($1, $2, $3, $4::jsonb, $5::jsonb, $6, $7, NOW())
		ON CONFLICT (id) DO UPDATE SET
			terms = EXCLUDED.terms,
			regex_patterns = EXCLUDED.regex_patterns,
			override_if_positive_highlight = EXCLUDED.override_if_positive_highlight,
			rejection_label = EXCLUDED.rejection_label,
			updated_at = NOW()`

	_, err := r.pool.Exec(
		ctx, query,
		entry.ID, entry.Category, entry.Language,
		termsJSON, regexJSON, entry.OverrideIfPositiveHighlight,
		entry.RejectionLabel,
	)
	if err != nil {
		return fmt.Errorf("upsert taxonomy entry %s: %w", entry.ID, err)
	}
	return nil
}
