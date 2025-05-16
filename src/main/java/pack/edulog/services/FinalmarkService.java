package pack.edulog.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.cloud.StorageServiceCloud;
import pack.edulog.config.JwtUtils;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.UML.FinalMark;
import pack.edulog.models.user.User;
import pack.edulog.repositories.ClassroomRepository;
import pack.edulog.repositories.FinalmarkRepository;
import pack.edulog.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinalmarkService {
    private final FinalmarkRepository finalmarkRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtil;
    private final StorageServiceCloud storageService;

    public FinalMark createFinalMark(
            Long classroomId,
            MultipartFile file,
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


        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Upload all files and collect URLs

        String url = storageService.saveFile(file);
        if (url == null) {
            throw new RuntimeException("Failed to upload file");
        }



        // Create assignment
        FinalMark finalMarkPost = new FinalMark();
        finalMarkPost.setCreatedAt(LocalDateTime.now());
        finalMarkPost.setUrlExcel(url);
        finalMarkPost.setClassroom(classroom);
        return finalmarkRepository.save(finalMarkPost);
    }

    public FinalMark getByClassroom(Long classroomId) {
        return finalmarkRepository.findByClassroomId(classroomId)
                .orElseThrow( () -> new RuntimeException("Classroom not found or no final marks submitted yet"));
    }
}
