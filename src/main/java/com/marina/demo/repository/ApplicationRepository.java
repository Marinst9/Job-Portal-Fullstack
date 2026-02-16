package com.marina.demo.repository;

import com.marina.demo.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    List<ApplicationEntity> findByUser_Id(Long userId);
    
    // НОВО: Земање апликации за оглас сортирани по најдобар AI Match
    List<ApplicationEntity> findByJob_IdOrderByAiMatchScoreDesc(Long jobId);
}