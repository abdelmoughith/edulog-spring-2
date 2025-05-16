package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.edulog.models.UML.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<List<Course>> findByClassroomId(Long classroomId);
}
