package com.marina.demo.repository;

import com.marina.demo.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    List<ApplicationEntity> findByUser_Id(Long userId);

    // Апликации за еден оглас, најдобар AI Match прв; неоценетите (null) на крај.
    // PostgreSQL по default ги става NULL вредностите ПРВИ при DESC.
    @Query("select a from ApplicationEntity a where a.job.id = :jobId order by a.aiMatchScore desc nulls last")
    List<ApplicationEntity> findByJobIdRanked(@Param("jobId") Long jobId);

    // Апликации за сите огласи на еден работодавач (Employer Dashboard)
    @Query("select a from ApplicationEntity a where a.job.employer.id = :employerId order by a.aiMatchScore desc nulls last")
    List<ApplicationEntity> findByEmployerIdRanked(@Param("employerId") Long employerId);

    boolean existsByJob_IdAndUser_Id(Long jobId, Long userId);
}
