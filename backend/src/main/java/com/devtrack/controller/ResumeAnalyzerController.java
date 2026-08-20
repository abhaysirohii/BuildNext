package com.devtrack.controller;

import com.devtrack.dto.*;
import com.devtrack.service.ResumeAnalyzerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume Analyzer")
public class ResumeAnalyzerController {

    private final ResumeAnalyzerService resumeAnalyzerService;

    public ResumeAnalyzerController(ResumeAnalyzerService resumeAnalyzerService) {
        this.resumeAnalyzerService = resumeAnalyzerService;
    }

    /** Feature 1: JD red-flag / missing-keyword scan against pasted text. */
    @PostMapping("/redflags")
    public ResponseEntity<RedFlagAnalyzeResponse> redFlags(@Valid @RequestBody RedFlagAnalyzeRequest request) {
        return ResponseEntity.ok(resumeAnalyzerService.analyzeRedFlags(
                request.getJobDescription(), request.getProjectDescription()));
    }

    /** Feature 2: description quality score + rewrite, against pasted text. */
    @PostMapping("/score")
    public ResponseEntity<DescriptionScoreResponse> score(@Valid @RequestBody DescriptionScoreRequest request) {
        return ResponseEntity.ok(resumeAnalyzerService.scoreDescription(
                request.getProjectDescription(), request.getJobDescription()));
    }

    /** Feature 3 (new): upload a resume PDF + a job description, get ATS score plus
     *  both of the above features run against the extracted resume text, in one call. */
    @PostMapping(value = "/analyze-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FullResumeAnalysisResponse> analyzePdf(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {
        return ResponseEntity.ok(resumeAnalyzerService.analyzeResumePdf(resume, jobDescription));
    }
}
