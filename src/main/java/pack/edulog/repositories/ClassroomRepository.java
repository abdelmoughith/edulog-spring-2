package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pack.edulog.models.UML.Classroom;
import pack.edulog.models.user.User;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // TEACHER
    @Query("SELECT c.teacher FROM Classroom c WHERE c.id = :classroomId")
    User findTeacherByClassroomId(@Param("classroomId") Long classroomId);

    // STUDENT
    @Query("SELECT s FROM Classroom c JOIN c.students s JOIN s.roles r WHERE c.id = :classroomId AND r.name = 'STUDENT'")
    List<User> findStudentsByClassroomIdAndRoleStudent(@Param("classroomId") Long classroomId);

    Optional<Classroom> findByCode(String code);

    @Query("SELECT c FROM Classroom c JOIN c.students s WHERE s.id = :studentId")
    Optional<List<Classroom>> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT c FROM Classroom c WHERE c.teacher.id = :teacherId")
    Optional<List<Classroom>> findByTeacherId(@Param("teacherId") Long teacherId);


}