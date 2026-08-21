// Pre-creates the autocomplete search index on the recruitflow_test database,
// used when running the integration tests against an external Atlas Local
// instance (Option B). Run with: docker exec ... mongosh --file this.

const target = db.getSiblingDB("recruitflow_test");

try {
    target.createCollection("candidate_profiles");
} catch (e) {
    // already exists
}

const existing = target.candidate_profiles.getSearchIndexes("candidateAutocomplete");
if (existing.length === 0) {
    target.candidate_profiles.createSearchIndex(
        "candidateAutocomplete",
        { mappings: { dynamic: false, fields: { name: { type: "autocomplete" } } } }
    );
    print("Created candidateAutocomplete on recruitflow_test.");
} else {
    print("candidateAutocomplete already exists on recruitflow_test.");
}
