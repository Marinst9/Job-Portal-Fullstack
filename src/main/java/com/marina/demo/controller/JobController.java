package com.marina.demo.controller;

import com.marina.demo.dto.JobDTO;
import com.marina.demo.model.JobEntity;
import com.marina.demo.service.JobService;
import com.marina.demo.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin(origins = "*")
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
        return ResponseEntity.ok(jobService.saveJob(job));
    }

    @PostMapping("/match")
    public ResponseEntity<Map<Long, Integer>> matchCV(@RequestBody Map<String, String> payload) {
        String cvText = payload.get("cvText");
        List<JobEntity> jobs = jobService.findAllJobs();
        Map<Long, Integer> results = new HashMap<>();

        for (JobEntity job : jobs) {
            // Користиме Integer бидејќи така дефиниравме во AIService
            Integer score = aiService.calculateMatchScore(cvText, job.getDescription());
            results.put(job.getId(), score);
        }
        return ResponseEntity.ok(results);
    }

    private JobDTO convertToDTO(JobEntity job) {
        return new JobDTO(job.getId(), job.getTitle(), job.getCompanyName(), 
                          job.getLocation(), job.getSalary(), job.getDescription());
    }
}