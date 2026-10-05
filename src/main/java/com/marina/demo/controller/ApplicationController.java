package com.marina.demo.controller;

import com.marina.demo.model.ApplicationEntity;
import com.marina.demo.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    // Грешките (400/404/409/500) ги мапира GlobalExceptionHandler.
    @PostMapping
    public ResponseEntity<ApplicationEntity> apply(@RequestBody ApplicationEntity application) {
        ApplicationEntity saved = service.applyForJob(application);
        log.info("Нова апликација id={} за оглас id={}", saved.getId(), saved.getJob().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Апликации за еден оглас
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationEntity>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.getApplicationsForJob(jobId));
    }

    // Апликации само за огласите на овој работодавач (Employer Dashboard)
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<List<ApplicationEntity>> getByEmployer(@PathVariable Long employerId) {
        return ResponseEntity.ok(service.getApplicationsForEmployer(employerId));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationEntity>> getAll() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationEntity>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getApplicationsByUser(userId));
    }
}
