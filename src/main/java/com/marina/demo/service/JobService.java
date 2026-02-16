package com.marina.demo.service;

import com.marina.demo.dto.JobDTO;
import com.marina.demo.model.JobEntity;
import com.marina.demo.repository.JobRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository repository;

    public JobService(JobRepository repository) {
        this.repository = repository;
    }

    public List<JobEntity> findAllJobs() {
        return repository.findAll();
    }

    // НОВО: Најди огласи само за еден работодавач
    public List<JobEntity> findJobsByEmployer(Long employerId) {
        return repository.findByEmployer_Id(employerId);
    }

    public Optional<JobDTO> findJobById(@NonNull Long id) {
        return repository.findById(Objects.requireNonNull(id, "id")).map(this::convertToDTO);
    }

    @NonNull
    public JobEntity saveJob(@NonNull JobEntity job) {
        return Objects.requireNonNull(repository.save(Objects.requireNonNull(job, "job")), "saved job");
    }

    public void deleteJob(@NonNull Long id) {
        repository.deleteById(Objects.requireNonNull(id, "id"));
    }

    private JobDTO convertToDTO(JobEntity job) {
        return new JobDTO(
            job.getId(),
            job.getTitle() != null ? job.getTitle() : "",
            job.getCompanyName() != null ? job.getCompanyName() : "",
            job.getLocation() != null ? job.getLocation() : "",
            job.getSalary(),
            job.getDescription() != null ? job.getDescription() : ""
        );
    }
}