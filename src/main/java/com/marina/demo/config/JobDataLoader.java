package com.marina.demo.config;

import com.marina.demo.model.JobEntity;
import com.marina.demo.model.User;
import com.marina.demo.repository.JobRepository;
import com.marina.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Демо податоци за локален развој:
 *   candidate@marinajobs.mk / test  (CANDIDATE)
 *   employer@marinajobs.mk  / test  (EMPLOYER, ги поседува трите огласи)
 */
@Configuration
public class JobDataLoader {

    private static final Logger log = LoggerFactory.getLogger(JobDataLoader.class);

    @Bean
    CommandLineRunner commandLineRunner(JobRepository repository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0 || repository.count() > 0) {
                return;
            }

            User candidate = newUser("Тест Кандидат", "candidate@marinajobs.mk", passwordEncoder.encode("test"), User.Role.CANDIDATE);
            User employer = newUser("Тест Работодавач", "employer@marinajobs.mk", passwordEncoder.encode("test"), User.Role.EMPLOYER);
            userRepository.saveAll(List.of(candidate, employer));

            repository.saveAll(List.of(
                newJob("Java Developer", "Building scalable backends with Spring Boot", "Intelegenta", "Skopje", "1200", employer),
                newJob("React Architect", "Designing modern UIs with React and Tailwind", "AITONIX", "Remote", "1500", employer),
                newJob("SQL Specialist", "Database optimization and PostgreSQL management", "Netcetera", "Bitola", "1100", employer)
            ));
            log.info("Demo data loaded: 2 users, 3 jobs");
        };
    }

    private static User newUser(String fullName, String email, String passwordHash, User.Role role) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(passwordHash);
        u.setRole(role);
        return u;
    }

    private static JobEntity newJob(String title, String description, String company, String location, String salary, User employer) {
        JobEntity j = new JobEntity();
        j.setTitle(title);
        j.setDescription(description);
        j.setCompanyName(company);
        j.setLocation(location);
        j.setSalary(new BigDecimal(salary));
        j.setEmployer(employer);
        return j;
    }
}
