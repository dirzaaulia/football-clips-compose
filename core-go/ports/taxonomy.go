package ports

import (
	"context"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// TaxonomyRepository defines data access for dynamic classification taxonomies.
type TaxonomyRepository interface {
	GetTaxonomyEntries(ctx context.Context) ([]domain.TaxonomyEntry, error)
	UpsertTaxonomyEntry(ctx context.Context, entry domain.TaxonomyEntry) error
}
