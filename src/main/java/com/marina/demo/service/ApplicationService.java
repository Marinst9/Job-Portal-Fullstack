package com.marina.demo.service;

import com.marina.demo.exception.BadRequestException;
import com.marina.demo.exception.ConflictException;
import com.marina.demo.exception.ResourceNotFoundException;
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
    private final AIService aiService;

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
        if (application.getJob() == null || application.getJob().getId() == null) {
            throw new BadRequestException("Недостасува Job ID.");
        }
        if (application.getUser() == null || application.getUser().getId() == null) {
            throw new BadRequestException("Недостасува User ID.");
        }

        // 1. Постоечки оглас
        JobEntity job = jobRepository.findById(application.getJob().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Огласот не е пронајден."));

        // 2. Постоечки корисник — по id. Порано се бараше по email (кој frontend-от
        //    не го праќа) и се креираше/препишуваше корисник со null полиња.
        User user = userRepository.findById(application.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Корисникот не е пронајден."));

        if (user.getRole() != User.Role.CANDIDATE) {
            throw new BadRequestException("Само кандидати можат да аплицираат.");
        }
        if (applicationRepository.existsByJob_IdAndUser_Id(job.getId(), user.getId())) {
            throw new ConflictException("Веќе имате аплицирано за овој оглас.");
        }

        ApplicationEntity toSave = new ApplicationEntity();
        toSave.setJob(job);
        toSave.setUser(user);
        toSave.setCoverLetter(application.getCoverLetter());

        // 3. AI оценка (null ако моделот не врати валиден број)
        toSave.setAiMatchScore(aiService.calculateMatchScore(application.getCoverLetter(), job.getDescription()));

        // 4. Системски вредности — не ги земаме од клиентот
        toSave.setAppliedAt(LocalDateTime.now());
        toSave.setStatus("PENDING");

        return applicationRepository.save(toSave);
    }

    public List<ApplicationEntity> getApplicationsForJob(Long jobId) {
        return applicationRepository.findByJobIdRanked(jobId);
    }

    public List<ApplicationEntity> getApplicationsForEmployer(Long employerId) {
        return applicationRepository.findByEmployerIdRanked(employerId);
    }

    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<ApplicationEntity> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUser_Id(userId);
    }
}
