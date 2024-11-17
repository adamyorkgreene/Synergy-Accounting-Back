package edu.kennesaw.appdomain.dto;

import java.util.List;

public class IncomeStatementDTO {

    private List<AccountResponseDTO> revenue;
    private List<AccountResponseDTO> expenses;
    private double totalRevenue;
    private double totalExpenses;
    private double netIncome;

    public IncomeStatementDTO(List<AccountResponseDTO> revenue, List<AccountResponseDTO> expenses, double totalRevenue, double totalExpenses, double netIncome) {
        this.revenue = revenue;
        this.expenses = expenses;
        this.totalRevenue = totalRevenue;
        this.totalExpenses = totalExpenses;
        this.netIncome = netIncome;
    }

    public List<AccountResponseDTO> getRevenue() {
        return revenue;
    }

    public void setRevenue(List<AccountResponseDTO> revenue) {
        this.revenue = revenue;
    }

    public List<AccountResponseDTO> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<AccountResponseDTO> expenses) {
        this.expenses = expenses;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

}
