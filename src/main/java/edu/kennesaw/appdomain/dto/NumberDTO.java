package edu.kennesaw.appdomain.dto;

public class NumberDTO {

    private double number;

    public NumberDTO(double number) {
        this.number = number;
    }

    public NumberDTO() {}

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }

}
