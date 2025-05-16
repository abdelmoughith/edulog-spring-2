package pack.edulog.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.models.DTO.GradeSubmissionRequest;
import pack.edulog.services.SubmissionService;

@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final ObjectMapper objectMapper;


    @PostMapping(path = "/create/{assignmentId}", consumes = "multipart/form-data")
    public ResponseEntity<?> createAssignmentWithFiles(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String token
    ) {

        try {
            return ResponseEntity.ok(submissionService.createSubmissionWithFile(assignmentId, file, token));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping(path = "/update/{submissionId}")
    public ResponseEntity<Boolean> updateSubmission(
            @RequestHeader("Authorization") String token,
            @PathVariable Long submissionId
    ) {
        try {
            return new ResponseEntity<>(submissionService.submitSubmission(submissionId, token), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PutMapping(path = "/update-marks")
    public ResponseEntity<String> uploadGrades(
            @RequestBody GradeSubmissionRequest request,
            @RequestHeader("Authorization") String token
            ) {
        try {
            submissionService.assignGrades(request, token);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PutMapping("/upload-grades/{assignmentId}")
    public ResponseEntity<String> uploadGradesFromExcel(
            @RequestHeader("Authorization") String token,
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file) {
        try {
            submissionService.assignGradesFromExcel(assignmentId, token, file);
            return new ResponseEntity<>("Grades updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update grades: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    // Get all assignments for a classroom
    @GetMapping("/get/{classroomId}")
    public ResponseEntity<?> getAssignmentsByClassroom(@PathVariable Long classroomId) {
        try {
            return new ResponseEntity<>(submissionService.getSubmissionById(classroomId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
