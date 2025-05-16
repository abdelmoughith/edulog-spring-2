package pack.edulog.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pack.edulog.models.UML.Assignment;
import pack.edulog.services.AssignmentService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AssignmentQL {
    private final AssignmentService assignmentService;

    @QueryMapping
    public List<Assignment> getAssignmentsByClassroom(@Argument Long classroomId) {
        try {
            return assignmentService.getByClassroom(classroomId);
        } catch (Exception e) {
            return null;
        }

    }
}
