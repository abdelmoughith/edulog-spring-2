package pack.edulog.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.cloud.StorageServiceCloud;
import pack.edulog.config.JwtUtils;
import pack.edulog.models.UML.Assignment;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.user.User;
import pack.edulog.repositories.AssignmentRepository;
import pack.edulog.repositories.ClassroomRepository;
import pack.edulog.repositories.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtil;
    private final StorageServiceCloud storageService;

    public List<Assignment> getByClassroom(Long classroomId) {
        return assignmentRepository.findByClassroomId(classroomId)
                .orElseThrow( () -> new RuntimeException("Classroom not found"));
    }

    @Transactional
    public Assignment createAssignmentWithFiles(
            Long classroomId,
            Assignment assignment,
            List<MultipartFile> files,
            String rawToken
    ) throws IOException {
        String token = rawToken.replace("Bearer ", "").trim();
        Long teacherId = jwtUtil.extractUserId(token);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        User realTeacher = classroomRepository.findTeacherByClassroomId(classroomId);

        if (!teacher.hasRole("TEACHER") || !realTeacher.getId().equals(teacher.getId())) {
            throw new RuntimeException("Only teachers of this classroom can create assignments");
        }

        if (files.size() > 3) {
            throw new RuntimeException("Maximum 3 files allowed");
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Upload all files and collect URLs
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = storageService.saveFile(file);
            if (url == null) {
                throw new RuntimeException("Failed to upload file");
            }
            fileUrls.add(url);
        }


        // Create assignment
        Assignment finalAssignment = new Assignment();
        finalAssignment.setTitle(assignment.getTitle());
        finalAssignment.setDescription(assignment.getDescription());
        finalAssignment.setDeadline(assignment.getDeadline());
        finalAssignment.setUrlAssignment(fileUrls);
        finalAssignment.setTotalGrades(null);
        finalAssignment.setClassroom(classroom);
        return assignmentRepository.save(finalAssignment);
    }
}

