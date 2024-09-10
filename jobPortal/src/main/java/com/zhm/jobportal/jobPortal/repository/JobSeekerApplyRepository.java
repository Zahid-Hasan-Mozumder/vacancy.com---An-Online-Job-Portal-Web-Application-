package com.zhm.jobportal.jobPortal.repository;

import com.zhm.jobportal.jobPortal.entity.JobPostActivity;
import com.zhm.jobportal.jobPortal.entity.JobSeekerApply;
import com.zhm.jobportal.jobPortal.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply, Integer> {

    List<JobSeekerApply> findByUserId(JobSeekerProfile userId);
    List<JobSeekerApply> findByJob(JobPostActivity job);

}
