package com.recruitflow.search;

import com.mongodb.client.MongoCollection;
import com.recruitflow.AbstractIntegrationTest;
import com.recruitflow.dto.response.CandidateResponse;
import com.recruitflow.model.CandidateProfile;
import com.recruitflow.repository.CandidateRepository;
import com.recruitflow.service.CandidateSearchService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies MongoDB Atlas Search autocomplete: "mar" must match "Mark" and "Marcus".
 * (Native $text cannot do this — hence the Atlas Local container.)
 */
class CandidateSearchServiceTest extends AbstractIntegrationTest {

    @Autowired
    private CandidateSearchService searchService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final List<String> SEED_NAMES =
            List.of("Mark Johnson", "Marcus Lee", "Maria Gomez", "Sarah Kim");

    @BeforeEach
    void seedAndIndex() throws InterruptedException {
        // Reset just these names so repeated runs against the shared test DB stay
        // deterministic (otherwise duplicates accumulate and crowd out results).
        mongoTemplate.remove(new Query(Criteria.where("name").in(SEED_NAMES)), CandidateProfile.class);
        candidateRepository.saveAll(List.of(
                new CandidateProfile("Mark Johnson", "Backend", List.of("Java"), 6),
                new CandidateProfile("Marcus Lee", "Platform", List.of("Go"), 8),
                new CandidateProfile("Maria Gomez", "Data", List.of("Python"), 5),
                new CandidateProfile("Sarah Kim", "Frontend", List.of("React"), 4)
        ));
        ensureSearchIndex();
    }

    @Test
    void shortPrefixMatchesMarNamesButNotFuzzyNoise() throws InterruptedException {
        // Short query (< 4 chars) skips fuzzy, so no false positives like "Sarah Kim".
        List<String> names = awaitResults("mar").stream()
                .map(CandidateResponse::name)
                .toList();
        assertThat(names).contains("Mark Johnson", "Marcus Lee");
        assertThat(names).doesNotContain("Sarah Kim");
    }

    @Test
    void longerQueryToleratesTypos() throws InterruptedException {
        // "marcuss" is one edit from "Marcus"; fuzzy applies for queries >= 4 chars.
        List<String> names = awaitResults("marcuss").stream()
                .map(CandidateResponse::name)
                .toList();
        assertThat(names).contains("Marcus Lee");
    }

    /**
     * Best-effort index creation. On Atlas Local reached via a direct driver
     * connection, search-index management commands may be unavailable — in that
     * case the index is expected to be pre-created (see scripts/) and this is a
     * no-op. Readiness is confirmed by polling the actual search query.
     */
    private void ensureSearchIndex() {
        MongoCollection<Document> coll =
                mongoTemplate.getDb().getCollection("candidate_profiles");
        try {
            coll.createSearchIndex("candidateAutocomplete",
                    new Document("mappings", new Document("dynamic", false)
                            .append("fields", new Document("name",
                                    new Document("type", "autocomplete")))));
        } catch (RuntimeException e) {
            // Index already exists, or management not available on this connection.
        }
    }

    /**
     * Atlas Search is eventually consistent, and mongot may briefly reject $search
     * with "only allowed on Atlas" just after an index is created — so tolerate
     * exceptions and poll until results appear.
     */
    private List<CandidateResponse> awaitResults(String query) throws InterruptedException {
        List<CandidateResponse> results = List.of();
        for (int i = 0; i < 60; i++) {
            try {
                results = searchService.autocompleteByName(query, 10);
                if (!results.isEmpty()) {
                    return results;
                }
            } catch (RuntimeException e) {
                // mongot not ready to serve this index yet — retry.
            }
            Thread.sleep(1000);
        }
        return results;
    }
}
