package edu.kennesaw.appdomain.dto;

public class ReturnOnEquityDTO {

    private double netIncome;
    private double totalEquity;
    private double ratio;

    public ReturnOnEquityDTO(double netIncome, double totalEquity) {
        this.netIncome = netIncome;
        this.totalEquity = totalEquity;
        this.ratio = (netIncome/totalEquity);
    }

    public ReturnOnEquityDTO() {}

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

    public void setTotalEquity(double totalEquity) {
        this.totalEquity = totalEquity;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public double getTotalEquity() {
        return totalEquity;
    }

    public double getRatio() {
        return ratio;
    }
}
