package com.marina.demo.controller;

import com.marina.demo.dto.JobDTO;
import com.marina.demo.exception.BadRequestException;
import com.marina.demo.model.JobEntity;
import com.marina.demo.service.JobService;
import com.marina.demo.service.AIService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin(origins = "http://localhost:5173")
public class JobController {

    private final JobService jobService;
    private final AIService aiService;

    public JobController(JobService jobService, AIService aiService) {
        this.jobService = jobService;
        this.aiService = aiService;
    }

    @GetMapping
    public ResponseEntity<List<JobDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.findAllJobs().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    // НОВО: Пребарување огласи по работодавач (за Dashboard)
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<List<JobEntity>> getJobsByEmployer(@PathVariable Long employerId) {
        return ResponseEntity.ok(jobService.findJobsByEmployer(employerId));
    }

    @PostMapping
    public ResponseEntity<JobEntity> createJob(@RequestBody JobEntity job) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(job));
    }

    @PostMapping("/match")
    public ResponseEntity<Map<Long, Integer>> matchCV(@RequestBody Map<String, String> payload) {
        String cvText = payload.get("cvText");
        if (cvText == null || cvText.isBlank()) {
            throw new BadRequestException("cvText е задолжително.");
        }
        List<JobEntity> jobs = jobService.findAllJobs();
        Map<Long, Integer> results = new HashMap<>();

        // Секвенцијално: по еден LLM повик за секој оглас. Доволно за мал број огласи;
        // за повеќе — кеш по (hash(CV), jobId) и асинхрона обработка.
        for (JobEntity job : jobs) {
            // null = моделот не врати валидна оценка
            results.put(job.getId(), aiService.calculateMatchScore(cvText, job.getDescription()));
        }
        return ResponseEntity.ok(results);
    }

    private JobDTO convertToDTO(JobEntity job) {
        return new JobDTO(job.getId(), job.getTitle(), job.getCompanyName(), 
                          job.getLocation(), job.getSalary(), job.getDescription());
    }
}