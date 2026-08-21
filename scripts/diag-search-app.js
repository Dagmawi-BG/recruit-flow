const t = db.getSiblingDB("recruitflow");
print("total candidate_profiles: " + t.candidate_profiles.countDocuments());
print("--- all names ---");
t.candidate_profiles.find({}, { name: 1, _id: 0 }).forEach(d => print("  " + d.name));
print("--- $search 'mar' with fuzzy maxEdits:1, ranked by score ---");
t.candidate_profiles.aggregate([
    { $search: { index: "candidateAutocomplete", autocomplete: { query: "mar", path: "name", fuzzy: { maxEdits: 1 } } } },
    { $project: { _id: 0, name: 1, score: { $meta: "searchScore" } } }
]).toArray().forEach(x => print("  " + x.name + "  score=" + x.score));
print("--- $search 'mar' WITHOUT fuzzy, ranked by score ---");
t.candidate_profiles.aggregate([
    { $search: { index: "candidateAutocomplete", autocomplete: { query: "mar", path: "name" } } },
    { $project: { _id: 0, name: 1, score: { $meta: "searchScore" } } }
]).toArray().forEach(x => print("  " + x.name + "  score=" + x.score));
