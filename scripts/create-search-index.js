// Creates the Atlas Search autocomplete index that powers "mar" -> "Mark", "Marcus".
// Run with mongosh (see README). Native $text search cannot do prefix matching;
// this "autocomplete" field type (edge n-grams) is what makes it work.

const target = db.getSiblingDB("recruitflow");

// createSearchIndex requires the collection to exist.
try {
    target.createCollection("candidate_profiles");
} catch (e) {
    // already exists - fine
}

const existing = target.candidate_profiles.getSearchIndexes("candidateAutocomplete");
if (existing.length > 0) {
    print("Search index 'candidateAutocomplete' already exists.");
} else {
    target.candidate_profiles.createSearchIndex(
        "candidateAutocomplete",
        {
            mappings: {
                dynamic: false,
                fields: {
                    name: { type: "autocomplete" }
                }
            }
        }
    );
    print("Created search index 'candidateAutocomplete'. It may take ~10-30s to become queryable.");
}
