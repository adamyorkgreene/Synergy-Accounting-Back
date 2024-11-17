package edu.kennesaw.appdomain.dto;

import java.util.List;

public class BalanceSheetDTO {

    private List<AccountResponseDTO> assets;
    private List<AccountResponseDTO> liabilities;
    private List<AccountResponseDTO> equity;
    private double totalAssets;
    private double totalLiabilities;
    private double totalEquity;

    public BalanceSheetDTO(List<AccountResponseDTO> assets, List<AccountResponseDTO> liabilities,
                           List<AccountResponseDTO> equity, double totalAssets, double totalLiabilities,
                           double totalEquity) {
        this.assets = assets;
        this.liabilities = liabilities;
        this.equity = equity;
        this.totalAssets = totalAssets;
        this.totalLiabilities = totalLiabilities;
        this.totalEquity = totalEquity;
    }

    public void setAssets(List<AccountResponseDTO> assets) {
        this.assets = assets;
    }

    public void setLiabilities(List<AccountResponseDTO> liabilities) {
        this.liabilities = liabilities;
    }

    public void setEquity(List<AccountResponseDTO> equity) {
        this.equity = equity;
    }

    public List<AccountResponseDTO> getAssets() {
        return assets;
    }

    public List<AccountResponseDTO> getLiabilities() {
        return liabilities;
    }

    public List<AccountResponseDTO> getEquity() {
        return equity;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(double totalAssets) {
        this.totalAssets = totalAssets;
    }

    public double getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(double totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public double getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(double totalEquity) {
        this.totalEquity = totalEquity;
    }

}
