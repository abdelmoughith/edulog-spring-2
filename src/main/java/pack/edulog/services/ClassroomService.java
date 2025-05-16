package pack.edulog.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pack.edulog.config.JwtUtils;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.user.User;
import pack.edulog.repositories.ClassroomRepository;
import pack.edulog.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtil;

    public Classroom createClassroom(String name, String rawToken) {
        String token = rawToken.replace("Bearer ", "").trim();
        Long teacherId = jwtUtil.extractUserId(token);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (!teacher.hasRole("TEACHER") ) {
            throw new RuntimeException("Only teachers can own classrooms");
        }

        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    @Transactional
    public User addStudent(Long classroomId, Long studentId, String rawToken) {
        String token = rawToken.replace("Bearer ", "").trim();
        Long teacherId = jwtUtil.extractUserId(token);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        User realTeacher = classroomRepository.findTeacherByClassroomId(classroomId);

        if (!teacher.hasRole("TEACHER") || !realTeacher.getId().equals(teacher.getId()) ) {
            throw new RuntimeException("Only teachers of classroom can add students");
        }
        User student = userRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        if (!student.hasRole("STUDENT") || student.hasRole("TEACHER")) {
            throw new RuntimeException("Only students can be added");
        }
        Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new RuntimeException("Classroom not found"));
        classroom.addStudent(student);
        return student;
    }

    @Transactional
    public User joinClassroomByCode(String classroomCode, String rawToken) {
        String token = rawToken.replace("Bearer ", "").trim();
        Long studentId = jwtUtil.extractUserId(token);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.hasRole("STUDENT") || student.hasRole("TEACHER")) {
            throw new RuntimeException("Only students can join classrooms");
        }

        Classroom classroom = classroomRepository.findByCode(classroomCode)
                .orElseThrow(() -> new RuntimeException("Invalid classroom code"));


        if (classroom.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
            throw new RuntimeException("You are already enrolled in this classroom");
        }

        classroom.addStudent(student);
        classroomRepository.save(classroom); // Ensure update is saved

        return student;
    }

    public List<User> getStudentsInClassroom(Long classroomId) {
        return classroomRepository.findStudentsByClassroomIdAndRoleStudent(classroomId);
    }


    public Classroom getById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
    }
    public List<Classroom> getClassroomOfStudent(String rawToken) {
        String token = rawToken.replace("Bearer ", "").trim();
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        if (user.hasRole("STUDENT")) {
            return classroomRepository.findByStudentId(userId)
                    .orElseThrow( () -> new RuntimeException("Something went wrong"));
        } else if (user.hasRole("TEACHER")) {
            return classroomRepository.findByTeacherId(userId)
                    .orElseThrow( () -> new RuntimeException("Something went wrong"));
        } else {
            throw new RuntimeException("Only students can join classrooms");
        }


    }
}

