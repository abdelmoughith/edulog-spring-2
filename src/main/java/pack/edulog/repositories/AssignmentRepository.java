package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pack.edulog.models.UML.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a WHERE a.classroom.id = :classroomId ORDER BY a.created DESC")
    Optional<List<Assignment>> findByClassroomId(@Param("classroomId") Long classroomId);


}