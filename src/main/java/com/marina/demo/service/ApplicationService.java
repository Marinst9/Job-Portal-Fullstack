package com.marina.demo.service;

import com.marina.demo.model.ApplicationEntity;
import com.marina.demo.model.JobEntity;
import com.marina.demo.model.User;
import com.marina.demo.repository.ApplicationRepository;
import com.marina.demo.repository.JobRepository;
import com.marina.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AIService aiService; // Додадено за AI пресметка

    public ApplicationService(ApplicationRepository applicationRepository, 
                              JobRepository jobRepository, 
                              UserRepository userRepository,
                              AIService aiService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Transactional
    public ApplicationEntity applyForJob(ApplicationEntity application) {
        // 1. Поврзи со постоечки Job
        JobEntity job = jobRepository.findById(application.getJob().getId())
                .orElseThrow(() -> new RuntimeException("Огласот не е пронајден!"));
        application.setJob(job);

        // 2. Сними/Најди корисник
        User user = userRepository.findByEmail(application.getUser().getEmail())
                .orElseGet(() -> {
                    User newUser = application.getUser();
                    if (newUser.getPassword() == null) newUser.setPassword("pass123");
                    if (newUser.getRole() == null) newUser.setRole(User.Role.CANDIDATE);
                    return userRepository.save(newUser);
                });
        application.setUser(user);

        // 3. AI ПРЕСМЕТКА: Пресметај го процентот пред да снимиш
        Integer score = aiService.calculateMatchScore(application.getCoverLetter(), job.getDescription());
        application.setAiMatchScore(score);

        // 4. Постави системски вредности
        application.setAppliedAt(LocalDateTime.now());
        if (application.getStatus() == null) application.setStatus("PENDING");

        return applicationRepository.save(application);
    }

    // Земање апликации за конкретен оглас (За Employer Dashboard)
    public List<ApplicationEntity> getApplicationsForJob(Long jobId) {
        return applicationRepository.findByJob_IdOrderByAiMatchScoreDesc(jobId);
    }

    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<ApplicationEntity> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUser_Id(userId);
    }
}