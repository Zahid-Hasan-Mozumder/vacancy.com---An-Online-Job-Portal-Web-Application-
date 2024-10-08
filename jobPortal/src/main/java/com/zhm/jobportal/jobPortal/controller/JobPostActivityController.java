package com.zhm.jobportal.jobPortal.controller;

import com.zhm.jobportal.jobPortal.entity.*;
import com.zhm.jobportal.jobPortal.service.JobPostActivityService;
import com.zhm.jobportal.jobPortal.service.JobSeekerApplyService;
import com.zhm.jobportal.jobPortal.service.JobSeekerSaveService;
import com.zhm.jobportal.jobPortal.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class JobPostActivityController {

    private UsersService usersService;
    private JobPostActivityService jobPostActivityService;
    private JobSeekerApplyService jobSeekerApplyService;
    private JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobPostActivityController(UsersService usersService,
                                     JobPostActivityService jobPostActivityService,
                                     JobSeekerApplyService jobSeekerApplyService,
                                     JobSeekerSaveService jobSeekerSaveService){
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model theModel,
                             @RequestParam(value = "job", required=false) String job,
                             @RequestParam(value = "location", required=false) String location,
                             @RequestParam(value = "partTime", required=false) String partTime,
                             @RequestParam(value = "fullTime", required=false) String fullTime,
                             @RequestParam(value = "freelance", required=false) String freelance,
                             @RequestParam(value = "remoteOnly", required=false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required=false) String officeOnly,
                             @RequestParam(value = "partialRemote", required=false) String partialRemote,
                             @RequestParam(value = "today", required=false) boolean today,
                             @RequestParam(value = "days7", required=false) boolean days7,
                             @RequestParam(value = "days30", required=false) boolean days30
    ){

        theModel.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        theModel.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        theModel.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        theModel.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        theModel.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        theModel.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        theModel.addAttribute("today", today);
        theModel.addAttribute("days7", days7);
        theModel.addAttribute("days30", days30);

        theModel.addAttribute("job", job);
        theModel.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if(days30){
            searchDate = LocalDate.now().minusDays(30);
        }
        else if(days7){
            searchDate = LocalDate.now().minusDays(7);
        }
        else if(today){
            searchDate = LocalDate.now();
        }
        else{
            dateSearchFlag = false;
        }

        if(partTime == null && fullTime == null && freelance == null){
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if(officeOnly == null && remoteOnly == null && partialRemote == null){
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }

        if(!dateSearchFlag && !remote && !type && !StringUtils.hasText(job) && !StringUtils.hasText(location)){
            jobPost = jobPostActivityService.getAll();
        }
        else{
            jobPost = jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                                                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
        }

        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUsername = authentication.getName();
            theModel.addAttribute("username", currentUsername);
            if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))){
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(((RecruiterProfile) currentUserProfile).getUserAccountId());
                theModel.addAttribute("jobPost", recruiterJobs);
            }
            else{
                List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidatesJobs((JobSeekerProfile) currentUserProfile);
                List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

                boolean exist;
                boolean saved;

                for(JobPostActivity jobActivity: jobPost){
                    exist = false;
                    saved = false;
                    for(JobSeekerApply jobSeekerApply : jobSeekerApplyList){
                        if(Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())){
                            jobActivity.setIsActive(true);
                            exist = true;
                            break;
                        }
                    }

                    for(JobSeekerSave jobSeekerSave : jobSeekerSaveList){
                        if(Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId())){
                            jobActivity.setIsSaved(true);
                            saved = true;
                            break;
                        }
                    }

                    if(!exist){
                        jobActivity.setIsActive(false);
                    }
                    if(!saved){
                        jobActivity.setIsSaved(false);
                    }

                    theModel.addAttribute("jobPost", jobPost);
                }

            }
        }
        theModel.addAttribute("user", currentUserProfile);
        return "dashboard";
    }

    @GetMapping("/dashboard/add")
    public String addJobs(Model theModel){
        theModel.addAttribute("jobPostActivity", new JobPostActivity());
        theModel.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model theModel){
        Users user = usersService.getCurrentUser();
        if(user != null){
            jobPostActivity.setPostedById(user);
        }
        jobPostActivity.setPostedDate(new Date());
        theModel.addAttribute("jobPostActivity", jobPostActivity);
        JobPostActivity saved = jobPostActivityService.addNew(jobPostActivity);
        return "redirect:/dashboard/";
    }

    @PostMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model theModel){
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        theModel.addAttribute("jobPostActivity", jobPostActivity);
        theModel.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @GetMapping("global-search/")
    public String globalSerach(Model theModel,
                               @RequestParam(value = "job", required=false) String job,
                               @RequestParam(value = "location", required=false) String location,
                               @RequestParam(value = "partTime", required=false) String partTime,
                               @RequestParam(value = "fullTime", required=false) String fullTime,
                               @RequestParam(value = "freelance", required=false) String freelance,
                               @RequestParam(value = "remoteOnly", required=false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required=false) String officeOnly,
                               @RequestParam(value = "partialRemote", required=false) String partialRemote,
                               @RequestParam(value = "today", required=false) boolean today,
                               @RequestParam(value = "days7", required=false) boolean days7,
                               @RequestParam(value = "days30", required=false) boolean days30
    ){
        theModel.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        theModel.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        theModel.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        theModel.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        theModel.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        theModel.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        theModel.addAttribute("today", today);
        theModel.addAttribute("days7", days7);
        theModel.addAttribute("days30", days30);

        theModel.addAttribute("job", job);
        theModel.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if(days30){
            searchDate = LocalDate.now().minusDays(30);
        }
        else if(days7){
            searchDate = LocalDate.now().minusDays(7);
        }
        else if(today){
            searchDate = LocalDate.now();
        }
        else{
            dateSearchFlag = false;
        }

        if(partTime == null && fullTime == null && freelance == null){
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if(officeOnly == null && remoteOnly == null && partialRemote == null){
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }

        if(!dateSearchFlag && !remote && !type && !StringUtils.hasText(job) && !StringUtils.hasText(location)){
            jobPost = jobPostActivityService.getAll();
        }
        else{
            jobPost = jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
        }

        theModel.addAttribute("jobPost", jobPost);
        return "global-search";
    }
}
