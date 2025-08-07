package final_java.models;
import java.time.LocalDate;

public class Booking {
    private String id;
    private Customer customer;
    private Room room;
    private LocalDate startDate;
    private LocalDate endDate;
    private double deposit;
    private boolean checkedIn; 

    public Booking(String id, Customer customer, Room room, LocalDate startDate, LocalDate endDate, double deposit) {
        this.id = id;
        this.customer = customer;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deposit = deposit;
        this.checkedIn = false; 
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getDeposit() {
        return deposit;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}