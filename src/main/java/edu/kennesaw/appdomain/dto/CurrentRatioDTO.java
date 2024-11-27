package edu.kennesaw.appdomain.dto;

public class CurrentRatioDTO {

    private double assets;
    private double liabilities;
    private double ratio;

    public CurrentRatioDTO(double assets, double liabilities) {
        this.assets = assets;
        this.liabilities = liabilities;
        this.ratio = assets/liabilities;
    }

    public CurrentRatioDTO() {}

    public void setAssets(double assets) {
        this.assets = assets;
    }

    public void setLiabilities(double liabilities) {
        this.liabilities = liabilities;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getAssets() {
        return assets;
    }

    public double getLiabilities() {
        return liabilities;
    }

    public double getRatio() {
        return ratio;
    }
}
