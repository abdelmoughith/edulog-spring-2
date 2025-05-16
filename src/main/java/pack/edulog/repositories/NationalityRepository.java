package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.edulog.models.user.Nationality;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {
}
