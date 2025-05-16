package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.edulog.models.UML.Assignment;
import pack.edulog.models.UML.Submission;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findAllByAssignmentIdAndStudentIdIn(Long assignmentId, List<Long> studentIds);
    List<Submission> findAllByAssignment(Assignment assignment);
}