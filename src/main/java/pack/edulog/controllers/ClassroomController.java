package pack.edulog.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.user.User;
import pack.edulog.services.ClassroomService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    // Create a new classroom
    @PostMapping
    public ResponseEntity<?> createClassroom(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String token) {
        try {
            Classroom classroom = classroomService.createClassroom(requestBody.get("name"), token);
            return new ResponseEntity<>(classroom, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/{classroomId}/students/{studentId}")
    public ResponseEntity<?> addStudent(
            @PathVariable("classroomId") Long classroomId,
            @PathVariable("studentId") Long studentId,
            @RequestHeader("Authorization") String rawToken) {

        try {
            User addedStudent = classroomService.addStudent(classroomId, studentId, rawToken);
            return ResponseEntity.ok(addedStudent);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinClassroom(
            @RequestBody String classroomCode,
            @RequestHeader("Authorization") String rawToken) {

        try {
            System.out.println("the code is : "+ classroomCode);
            User joinedStudent = classroomService.joinClassroomByCode(classroomCode, rawToken);
            return ResponseEntity.ok(joinedStudent);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get classroom by ID
    @GetMapping("/{id}")
    public Classroom getClassroomById(@PathVariable Long id) {
        return classroomService.getById(id);
    }

    // Get all students in a classroom
    @GetMapping("/{classroomId}/students")
    public List<User> getStudentsInClassroom(@PathVariable Long classroomId) {
        return classroomService.getStudentsInClassroom(classroomId);
    }
    @GetMapping("/all")
    public ResponseEntity<?> getStudentClassroom(
            @RequestHeader("Authorization") String rawToken
    ) {
        try {
            List<Classroom> classroomList =
                    classroomService.getClassroomOfStudent(rawToken);
            return new ResponseEntity<>(classroomList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
