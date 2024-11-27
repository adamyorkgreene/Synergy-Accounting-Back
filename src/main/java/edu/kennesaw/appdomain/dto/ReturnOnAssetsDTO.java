package edu.kennesaw.appdomain.dto;

public class ReturnOnAssetsDTO {

    private double netIncome;
    private double totalAssets;
    private double ratio;

    public ReturnOnAssetsDTO(double netIncome, double totalAssets) {
        this.netIncome = netIncome;
        this.totalAssets = totalAssets;
        this.ratio = (netIncome/totalAssets);
    }

    public ReturnOnAssetsDTO() {}

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

    public void setTotalAssets(double totalAssets) {
        this.totalAssets = totalAssets;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public double getRatio() {
        return ratio;
    }
}
