package edu.kennesaw.appdomain.dto;

import edu.kennesaw.appdomain.entity.UserDate;

import java.util.Date;

public class UserDateDTO {

    private Date birthday;
    private Date joinDate;
    private Date tempLeaveStart;
    private Date tempLeaveEnd;

    public UserDateDTO() {}

    public UserDateDTO(UserDate userDate) {
        this.birthday = userDate.getBirthday();
        this.joinDate = userDate.getJoinDate();
        this.tempLeaveStart = userDate.getTempLeaveStart();
        this.tempLeaveEnd = userDate.getTempLeaveEnd();
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Date getTempLeaveStart() {
        return tempLeaveStart;
    }

    public void setTempLeaveStart(Date tempLeaveStart) {
        this.tempLeaveStart = tempLeaveStart;
    }

    public Date getTempLeaveEnd() {
        return tempLeaveEnd;
    }

    public void setTempLeaveEnd(Date tempLeaveEnd) {
        this.tempLeaveEnd = tempLeaveEnd;
    }
}
