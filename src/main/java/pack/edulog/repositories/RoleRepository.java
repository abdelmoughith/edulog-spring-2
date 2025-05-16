package pack.edulog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.edulog.models.user.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
