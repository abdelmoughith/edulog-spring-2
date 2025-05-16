package pack.edulog.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.cloud.StorageServiceCloud;
import pack.edulog.config.JwtUtils;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.UML.Course;
import pack.edulog.models.user.User;
import pack.edulog.repositories.ClassroomRepository;
import pack.edulog.repositories.CourseRepository;
import pack.edulog.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtil;
    private final StorageServiceCloud storageService;

    public List<Course> getByClassroom(Long classroomId) {
        return courseRepository.findByClassroomId(classroomId)
                .orElseThrow( () -> new RuntimeException("Classroom not found"));
    }

    @Transactional
    public Course createCourseWithFiles(
            Long classroomId,
            Course course,
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

        if (files != null && files.size() > 3) {
            throw new RuntimeException("Maximum 3 files allowed");
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Upload all files and collect URLs
        List<String> fileUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                String url = storageService.saveFile(file);
                if (url == null) {
                    throw new RuntimeException("Failed to upload file");
                }
                fileUrls.add(url);
            }
        } else {
            throw new RuntimeException("You must upload at least one file");
        }


        // Create assignment
        Course finalCourse = new Course();
        finalCourse.setTitle(course.getTitle());
        finalCourse.setDescription(course.getDescription());
        finalCourse.setUrlCourse(fileUrls);
        finalCourse.setCreatedAt(LocalDateTime.now());
        finalCourse.setClassroom(classroom);


        return courseRepository.save(finalCourse);
    }
}


