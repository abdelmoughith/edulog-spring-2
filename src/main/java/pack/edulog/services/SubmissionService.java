package pack.edulog.services;


import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.cloud.StorageServiceCloud;
import pack.edulog.config.JwtUtils;
import pack.edulog.models.DTO.GradeSubmissionRequest;
import pack.edulog.models.UML.Assignment;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.UML.Submission;
import pack.edulog.models.user.User;
import pack.edulog.repositories.AssignmentRepository;
import pack.edulog.repositories.ClassroomRepository;
import pack.edulog.repositories.SubmissionRepository;
import pack.edulog.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtil;
    private final StorageServiceCloud storageService;

    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("submission not found"));
    }

    @Transactional
    public Submission createSubmissionWithFile(
            Long assignementId,
            MultipartFile file,
            String rawToken
    ) throws IOException {
        String token = rawToken.replace("Bearer ", "").trim();
        Long studentId = jwtUtil.extractUserId(token);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Assignment assignment = assignmentRepository.findById(assignementId)
                .orElseThrow( () -> new RuntimeException("Assignment not found"));
        Classroom classroom = classroomRepository.findById(assignment.getClassroom().getId())
                .orElseThrow( () -> new RuntimeException("Classroom not found"));

        List<User> realStudents = classroomRepository.findStudentsByClassroomIdAndRoleStudent(classroom.getId());
        boolean studentExists = realStudents.stream()
                .anyMatch(u -> u.getId().equals(studentId));
        if (!studentExists){
            throw new RuntimeException("Only Students of this classroom can create assignments");
        }

        if (!student.hasRole("STUDENT") ) {
            throw new RuntimeException("Only Students can create assignments");
        }
        String url = storageService.saveFile(file);



        // Create assignment
        Submission finalSubmission = new Submission();
        finalSubmission.setUrlSubmission(url);
        finalSubmission.setStatus(false);
        finalSubmission.setStudent(student);
        finalSubmission.setAssignment(assignment);

        return submissionRepository.save(finalSubmission);
    }
    public Boolean submitSubmission(Long submissionId, String rawToken) {
        String token = rawToken.replace("Bearer ", "").trim();
        Long studentId = jwtUtil.extractUserId(token);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow( () -> new RuntimeException("Submission not found"));

        if (!student.hasRole("STUDENT") ) {
            throw new RuntimeException("Only Students can create assignments");
        }

        if (!submission.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Only Student owner can update assignments");
        }
        submission.publish();
        submissionRepository.save(submission);
        return true;
    }
    // exel methods
    public void assignGrades(GradeSubmissionRequest request, String rawToken) {
        // owner Teacher
        String token = rawToken.replace("Bearer ", "").trim();
        Long teacherId = jwtUtil.extractUserId(token);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Only teachers can assign assignments");
        }
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow( () -> new RuntimeException("Assignment not found"));
        if (!teacherId.equals(assignment.getClassroom().getTeacher().getId())) {
            throw new RuntimeException("Only teacher owner can assign assignments");
        }
        // logic
        List<Submission> submissions = submissionRepository.findAllByAssignment(assignment);

        Map<Long, Double> gradeMap = request.getGrades().stream()
                .collect(Collectors.toMap(
                        GradeSubmissionRequest.StudentGrade::getStudentId,
                        GradeSubmissionRequest.StudentGrade::getGrade
                ));

        for (Submission submission : submissions) {
            submission.setGrade(gradeMap.get(submission.getStudent().getId()));
        }

        submissionRepository.saveAll(submissions);
    }

    public void assignGradesFromExcel(Long assignmentId, String rawToken, MultipartFile file) {
        // Extract teacher
        String token = rawToken.replace("Bearer ", "").trim();
        Long teacherId = jwtUtil.extractUserId(token);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!teacher.hasRole("TEACHER")) {
            throw new RuntimeException("Only teachers can assign grades");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!teacherId.equals(assignment.getClassroom().getTeacher().getId())) {
            throw new RuntimeException("You are not the owner of this classroom");
        }

        // Parse Excel file
        Map<Long, Double> gradeMap = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header
                Cell studentIdCell = row.getCell(0);
                Cell gradeCell = row.getCell(3);

                if (studentIdCell != null && gradeCell != null) {
                    long studentId = (long) studentIdCell.getNumericCellValue();
                    double grade = gradeCell.getNumericCellValue();
                    gradeMap.put(studentId, grade);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }

        // Apply grades
        List<Submission> submissions = submissionRepository.findAllByAssignment(assignment);
        for (Submission submission : submissions) {
            Long studentId = submission.getStudent().getId();
            if (gradeMap.containsKey(studentId)) {
                submission.setGrade(gradeMap.get(studentId));
            }
        }

        submissionRepository.saveAll(submissions);
    }

}


