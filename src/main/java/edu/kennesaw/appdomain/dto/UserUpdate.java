package edu.kennesaw.appdomain.dto;

public class UserUpdate {

    private long Userid;

    private UserDTO user;

    public long getUserid() {
        return Userid;
    }

    public void setUserid(long userid) {
        Userid = userid;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

}
