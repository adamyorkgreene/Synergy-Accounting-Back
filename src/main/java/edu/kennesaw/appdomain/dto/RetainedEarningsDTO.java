package edu.kennesaw.appdomain.dto;

import java.util.List;

public class RetainedEarningsDTO {
    private List<Row> rows; // Dynamic rows for the statement

    public RetainedEarningsDTO(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public static class Row {
        private String description;
        private double amount;

        public Row(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}