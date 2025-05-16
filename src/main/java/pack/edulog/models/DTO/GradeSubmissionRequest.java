package pack.edulog.models.DTO;

import java.util.List;

public class GradeSubmissionRequest {
    private Long assignmentId;
    private List<StudentGrade> grades;

    // Getters and Setters

    public static class StudentGrade {
        private Long studentId;
        private Double grade;

        // Getters and Setters

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public Double getGrade() {
            return grade;
        }

        public void setGrade(Double grade) {
            this.grade = grade;
        }
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public List<StudentGrade> getGrades() {
        return grades;
    }

    public void setGrades(List<StudentGrade> grades) {
        this.grades = grades;
    }
}