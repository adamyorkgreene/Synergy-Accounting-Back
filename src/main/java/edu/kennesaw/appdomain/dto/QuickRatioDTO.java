package edu.kennesaw.appdomain.dto;

public class QuickRatioDTO {

    private double assets;
    private double liabilities;
    private double inventory;
    private double ratio;

    public QuickRatioDTO(double assets, double liabilities, double inventory) {
        this.assets = assets;
        this.liabilities = liabilities;
        this.inventory = inventory;
        this.ratio = (assets - inventory)/liabilities;
    }

    public QuickRatioDTO() {}

    public void setAssets(double assets) {
        this.assets = assets;
    }

    public void setLiabilities(double liabilities) {
        this.liabilities = liabilities;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public void setInventory(double inventory) {
        this.inventory = inventory;
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

    public double getInventory() {
        return inventory;
    }
}
