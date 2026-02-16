package com.marina.demo.controller;

import com.marina.demo.model.ApplicationEntity;
import com.marina.demo.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody ApplicationEntity application) {
        try {
            log.info("Нова апликација за оглас ID: {}", application.getJob().getId());
            
            if (application.getJob() == null || application.getJob().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Недостасува Job ID"));
            }

            // Сервисот сега автоматски прави AI Match и зачувува во база
            ApplicationEntity saved = service.applyForJob(application);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Грешка при аплицирање: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // НОВО: Endpoint за работодавачот да ги види сите апликации за неговиот оглас
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationEntity>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.getApplicationsForJob(jobId));
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