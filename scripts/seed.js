// Seeds demo candidates so you can test the "mar" -> Mark/Marcus autocomplete.
// Run with mongosh (see README).

const target = db.getSiblingDB("recruitflow");

target.candidate_profiles.deleteMany({});
target.candidate_profiles.insertMany([
    { name: "Mark Johnson",  bio: "Backend engineer",  skills: ["Java", "Spring Boot"],     yearsExperience: 6 },
    { name: "Marcus Lee",    bio: "Platform engineer", skills: ["Go", "Kubernetes"],        yearsExperience: 8 },
    { name: "Maria Gomez",   bio: "Data engineer",     skills: ["Python", "Spark"],         yearsExperience: 5 },
    { name: "Marta Novak",   bio: "QA engineer",       skills: ["Selenium", "Java"],        yearsExperience: 3 },
    { name: "Sarah Kim",     bio: "Frontend engineer", skills: ["React", "TypeScript"],     yearsExperience: 4 },
    { name: "David Chen",    bio: "Fullstack engineer",skills: ["Node.js", "React"],        yearsExperience: 7 }
]);

print("Seeded " + target.candidate_profiles.countDocuments() + " candidates.");
