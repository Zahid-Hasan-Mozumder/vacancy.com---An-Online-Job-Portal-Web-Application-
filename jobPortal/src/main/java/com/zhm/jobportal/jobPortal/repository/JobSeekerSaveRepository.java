package com.zhm.jobportal.jobPortal.repository;

import com.zhm.jobportal.jobPortal.entity.JobPostActivity;
import com.zhm.jobportal.jobPortal.entity.JobSeekerProfile;
import com.zhm.jobportal.jobPortal.entity.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSeekerSaveRepository extends JpaRepository<JobSeekerSave, Integer> {

    List<JobSeekerSave> findByUserId(JobSeekerProfile userAccountId);

    List<JobSeekerSave> findByJob(JobPostActivity job);
}
