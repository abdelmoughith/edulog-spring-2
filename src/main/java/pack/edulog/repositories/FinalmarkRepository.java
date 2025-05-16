package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.edulog.models.UML.FinalMark;

import java.util.Optional;

public interface FinalmarkRepository extends JpaRepository<FinalMark, Long> {
    Optional<FinalMark> findByClassroomId(Long classroomId);
}
