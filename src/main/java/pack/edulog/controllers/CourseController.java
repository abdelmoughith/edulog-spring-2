package pack.edulog.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.models.UML.Course;
import pack.edulog.services.CourseService;

import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;


    @PostMapping(path = "/create/{classroomId}", consumes = "multipart/form-data")
    public ResponseEntity<?> createAssignmentWithFiles(
            @PathVariable("classroomId") Long classroomId,
            @RequestParam("course") String request,
            @RequestParam(value = "file", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token
    ) throws JsonProcessingException {
        Course assignment = objectMapper.readValue(request, Course.class);

        try {
            return ResponseEntity.ok(courseService.createCourseWithFiles(classroomId, assignment, files, token));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all assignments for a classroom
    @GetMapping("/course/{classroomId}")
    public ResponseEntity<?> getCoursesByClassroom(@PathVariable Long classroomId) {
        try {
            return new ResponseEntity<>(courseService.getByClassroom(classroomId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
