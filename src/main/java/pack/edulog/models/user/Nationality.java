package pack.edulog.models.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Nationality {
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
}

