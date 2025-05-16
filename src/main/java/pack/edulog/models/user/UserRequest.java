package pack.edulog.models.user;

import java.time.LocalDate;

public class UserRequest {
    public String username;
    public String email;
    public LocalDate birthday;
    public String firstName;
    public String lastName;
    public String school;
    public String registrationNumber;
    public String phone;
    public String address;
    public String className;
    public Long nationality; // This is just an ID
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
