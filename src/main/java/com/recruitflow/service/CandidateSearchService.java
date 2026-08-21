package com.recruitflow.service;

import com.recruitflow.dto.response.CandidateResponse;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes MongoDB Atlas Search autocomplete queries against candidate profiles.
 *
 * <p>This uses the Atlas {@code $search} aggregation stage (available on the
 * {@code mongodb/mongodb-atlas-local} image), which is what enables prefix
 * matching such as "mar" -> "Mark", "Marcus". Native {@code $text} search cannot
 * do this; it only matches whole words.
 */
@Service
public class CandidateSearchService {

    private static final String COLLECTION = "candidate_profiles";
    private static final String SEARCH_INDEX = "candidateAutocomplete";

    /** Only apply fuzzy (typo-tolerant) matching for queries at least this long. */
    private static final int FUZZY_MIN_QUERY_LENGTH = 4;

    private final MongoTemplate mongoTemplate;

    public CandidateSearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CandidateResponse> autocompleteByName(String query, int limit) {
        Document autocomplete = new Document("query", query).append("path", "name");
        // Typo tolerance only for longer queries; on very short prefixes fuzzy
        // matching yields noisy false positives (e.g. "mar" fuzzily matches "Sarah").
        if (query != null && query.trim().length() >= FUZZY_MIN_QUERY_LENGTH) {
            autocomplete.append("fuzzy", new Document("maxEdits", 1));
        }

        List<Document> pipeline = List.of(
                new Document("$search", new Document("index", SEARCH_INDEX)
                        .append("autocomplete", autocomplete)),
                new Document("$limit", limit),
                new Document("$project", new Document("name", 1)
                        .append("bio", 1)
                        .append("skills", 1)
                        .append("yearsExperience", 1)
                        .append("score", new Document("$meta", "searchScore")))
        );

        List<CandidateResponse> results = new ArrayList<>();
        for (Document doc : mongoTemplate.getDb().getCollection(COLLECTION).aggregate(pipeline)) {
            results.add(toResponse(doc));
        }
        return results;
    }

    private CandidateResponse toResponse(Document doc) {
        String id = doc.getObjectId("_id") != null ? doc.getObjectId("_id").toHexString() : null;
        int years = doc.get("yearsExperience") instanceof Number n ? n.intValue() : 0;
        double score = doc.get("score") instanceof Number n ? n.doubleValue() : 0.0;
        return new CandidateResponse(
                id,
                doc.getString("name"),
                doc.getString("bio"),
                doc.getList("skills", String.class),
                years,
                score
        );
    }
}
