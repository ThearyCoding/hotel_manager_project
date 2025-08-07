
import java.time.LocalDate;

public class IncomeRecord {

    private String customerName;
    private String roomNumber;
    private String roomType;
    private double amount;
    private LocalDate checkOutDate;

    public IncomeRecord(String customerName, String roomNumber, String roomType, double amount, LocalDate checkOutDate) {
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.amount = amount;
        this.checkOutDate = checkOutDate;
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

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }
}
