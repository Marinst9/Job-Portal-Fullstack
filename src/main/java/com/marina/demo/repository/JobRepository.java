package com.marina.demo.repository;

import com.marina.demo.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
    // НОВО: Најди ги сите огласи објавени од конкретен работодавач
    List<JobEntity> findByEmployer_Id(Long employerId);
}