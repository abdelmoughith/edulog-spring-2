package pack.edulog.graphql;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pack.edulog.models.UML.Classroom;
import pack.edulog.services.ClassroomService;
import pack.edulog.services.CustomUserService;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ClassroomQL {
    private final ClassroomService classroomService;
    private final CustomUserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ClassroomQL.class);


    @QueryMapping
    public List<Classroom> getClassroomByStudent(@Argument String token) {
        try {
            List<Classroom> classroomList =
                    classroomService.getClassroomOfStudent(token);
            return classroomList;
        } catch (Exception e) {
            return null;
        }
    }
}



