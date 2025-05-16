package pack.edulog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.models.UML.Assignment;
import pack.edulog.services.AssignmentService;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final ObjectMapper objectMapper;


    @PostMapping(path = "/create/{classroomId}", consumes = "multipart/form-data")
    public ResponseEntity<?> createAssignmentWithFiles(
            @PathVariable("classroomId") Long classroomId,
            @RequestParam("assignment") String request,
            @RequestParam(value = "file", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token
    ) throws JsonProcessingException {
        Assignment assignment = objectMapper.readValue(request, Assignment.class);

        try {
            return ResponseEntity.ok(assignmentService.createAssignmentWithFiles(classroomId, assignment, files, token));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all assignments for a classroom
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<?> getAssignmentsByClassroom(@PathVariable Long classroomId) {
        try {
            return new ResponseEntity<>(assignmentService.getByClassroom(classroomId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}