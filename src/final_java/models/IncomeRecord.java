package final_java.models;

import java.time.LocalDate;

public class IncomeRecord {
    private String customerName;
    private String roomNumber;
    private String roomType;
    private double amount;
    private LocalDate date;
    private String status; // New field for status (Booked, Checked In, Checked Out)

    public IncomeRecord(String customerName, String roomNumber, String roomType, double amount, LocalDate date, String status) {
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}