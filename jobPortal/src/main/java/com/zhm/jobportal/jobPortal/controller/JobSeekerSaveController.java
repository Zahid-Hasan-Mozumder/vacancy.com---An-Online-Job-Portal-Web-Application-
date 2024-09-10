package com.zhm.jobportal.jobPortal.controller;

import com.zhm.jobportal.jobPortal.entity.JobPostActivity;
import com.zhm.jobportal.jobPortal.entity.JobSeekerProfile;
import com.zhm.jobportal.jobPortal.entity.JobSeekerSave;
import com.zhm.jobportal.jobPortal.entity.Users;
import com.zhm.jobportal.jobPortal.service.JobPostActivityService;
import com.zhm.jobportal.jobPortal.service.JobSeekerProfileService;
import com.zhm.jobportal.jobPortal.service.JobSeekerSaveService;
import com.zhm.jobportal.jobPortal.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerSaveController {

    private UsersService usersService;
    private JobSeekerProfileService jobSeekerProfileService;
    private JobPostActivityService jobPostActivityService;
    private JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobSeekerSaveController(UsersService usersService, JobSeekerProfileService jobSeekerProfileService, JobPostActivityService jobPostActivityService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @PostMapping("job-details/save/{id}")
    public String save(@PathVariable("id") int id, JobSeekerSave jobSeekerSave){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
            if(seekerProfile.isPresent() && jobPostActivity != null){
                jobSeekerSave.setJob(jobPostActivity);
                jobSeekerSave.setUserId(seekerProfile.get());
            }
            else{
                throw new RuntimeException("user not found");
            }
            jobSeekerSaveService.addNew(jobSeekerSave);
        }
        return "redirect:/dashboard/";
    }

    @GetMapping("saved-jobs/")
    public String savedJobs(Model theModel){
        List<JobPostActivity> jobPost = new ArrayList<>();
        Object currentUserProfile = usersService.getCurrentUserProfile();

        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);
        for(JobSeekerSave jobSeekerSave : jobSeekerSaveList){
            jobPost.add(jobSeekerSave.getJob());
        }

        theModel.addAttribute("jobPost", jobPost);
        theModel.addAttribute("user", currentUserProfile);

        return "saved-jobs";
    }
}
