package com.marina.demo.config;

import com.marina.demo.model.JobEntity;
import com.marina.demo.model.User;
import com.marina.demo.repository.JobRepository;
import com.marina.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Configuration
public class JobDataLoader {

    @Bean
    CommandLineRunner commandLineRunner(JobRepository repository, UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User defaultUser = new User();
                defaultUser.setFullName("Тест Корисник");
                defaultUser.setEmail("test@marinajobs.mk");
                defaultUser.setPassword("test");
                defaultUser.setRole(User.Role.CANDIDATE);
                userRepository.save(defaultUser);
            }
            if (repository.count() == 0) {
                JobEntity job1 = new JobEntity();
                job1.setTitle("Java Developer");
                job1.setDescription("Building scalable backends with Spring Boot");
                job1.setCompanyName("Intelegenta");
                job1.setLocation("Skopje");
                job1.setSalary(new BigDecimal("1200"));

                JobEntity job2 = new JobEntity();
                job2.setTitle("React Architect");
                job2.setDescription("Designing modern UIs with React and Tailwind");
                job2.setCompanyName("AITONIX");
                job2.setLocation("Remote");
                job2.setSalary(new BigDecimal("1500"));

                JobEntity job3 = new JobEntity();
                job3.setTitle("SQL Specialist");
                job3.setDescription("Database optimization and PostgreSQL management");
                job3.setCompanyName("Netcetera");
                job3.setLocation("Bitola");
                job3.setSalary(new BigDecimal("1100"));

                repository.saveAll(Objects.requireNonNull(List.of(job1, job2, job3)));
                System.out.println("--- Test data loaded into PostgreSQL successfully ---");
            }
        };
    }
}