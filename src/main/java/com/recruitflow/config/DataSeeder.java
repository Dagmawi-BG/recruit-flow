package com.recruitflow.config;

import com.recruitflow.model.Application;
import com.recruitflow.model.ApplicationStage;
import com.recruitflow.model.CandidateProfile;
import com.recruitflow.model.Job;
import com.recruitflow.model.Role;
import com.recruitflow.model.User;
import com.recruitflow.repository.ApplicationRepository;
import com.recruitflow.repository.CandidateRepository;
import com.recruitflow.repository.JobRepository;
import com.recruitflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds demo users (idempotently) so the app is immediately usable:
 *   admin/admin, recruiter/recruiter, candidate/candidate,
 *   eng_manager/eng_manager (Engineering), mkt_manager/mkt_manager (Marketing).
 * Also seeds an Engineering job + application so department security is demoable.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CandidateRepository candidateRepository,
                      JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin", Role.ADMIN, null);
        seedUser("recruiter", "recruiter", Role.RECRUITER, null);
        seedUser("recruiter2", "recruiter2", Role.RECRUITER, null);
        seedUser("candidate", "candidate", Role.CANDIDATE, null);
        seedUser("eng_manager", "eng_manager", Role.HIRING_MANAGER, "Engineering");
        seedUser("mkt_manager", "mkt_manager", Role.HIRING_MANAGER, "Marketing");

        if (candidateRepository.findByUserId("candidate").isEmpty()) {
            CandidateProfile profile = new CandidateProfile(
                    "Charlie Candidate", "Aspiring backend developer",
                    List.of("Java", "Spring"), 2);
            profile.setUserId("candidate");
            candidateRepository.save(profile);
        }

        if (jobRepository.count() == 0) {
            Job job = jobRepository.save(
                    new Job("Backend Engineer", "Build APIs", "Engineering", 3));
            applicationRepository.save(
                    new Application("candidate", job.getId(), ApplicationStage.APPLIED, "Engineering"));
        }
    }

    private void seedUser(String username, String rawPassword, Role role, String department) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new User(username, passwordEncoder.encode(rawPassword), role, department));
        }
    }
}
