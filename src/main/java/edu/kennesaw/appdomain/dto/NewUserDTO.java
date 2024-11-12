package edu.kennesaw.appdomain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.types.UserType;

public class NewUserDTO {

    private Long userid;
    private String address;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private String username;

    @JsonProperty("user_security")
    private UserSecurityDTO user_security;

    @JsonProperty("user_date")
    private UserDateDTO user_date;

    public NewUserDTO() {}

    public NewUserDTO(User user) {
        this.userid = user.getUserid();
        this.address = user.getAddress();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userType = user.getUserType();
        this.username = user.getUsername();
        this.user_security = new UserSecurityDTO(user.getUserSecurity());
        this.user_date = new UserDateDTO(user.getUserDate());
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserSecurityDTO getUserSecurity() {
        return user_security;
    }

    public void setUserSecurity(UserSecurityDTO user_security) {
        this.user_security = user_security;
    }

    public UserDateDTO getUserDate() {
        return user_date;
    }

    public void setUserDate(UserDateDTO user_date) {
        this.user_date = user_date;
    }
}
