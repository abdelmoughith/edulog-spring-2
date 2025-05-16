package pack.edulog.models.UML;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pack.edulog.models.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "classrooms")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int studentNumber;

    private String profileImage;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Teacher reference (Many classrooms -> One teacher)
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    // Students (One classroom -> Many students)
    @OneToMany
    @JoinTable(
            name = "classroom_students",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students = new ArrayList<>();


    @Column(length = 20, unique = true)
    private String code;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (code == null || code.isEmpty()) {
            this.code = generateRandomCode(6);
        }
    }
    // Helper: Regenerate code manually
    public void regenerateCode(int length) {
        this.code = generateRandomCode(length);
    }

    // Initialize student count after loading from DB
    @PostLoad
    private void updateStudentCountAfterLoad() {
        this.studentNumber = this.students.size();
    }
    // 🔁 Random code generator (Alpha-Numeric)
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }


    // Helper method to add a student
    public void addStudent(User student) {
        if (this.students == null) {
            this.students = new ArrayList<>();
        }
        this.students.add(student);
        this.studentNumber = this.students.size();
    }

    // Helper method to remove a student
    public void removeStudent(User student) {
        if (this.students != null && !this.students.isEmpty()) {
            this.students.remove(student);
            this.studentNumber = this.students.size();
        }
    }
}