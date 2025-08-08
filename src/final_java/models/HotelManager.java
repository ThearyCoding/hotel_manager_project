package final_java.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HotelManager {
    private static HotelManager instance;
    private List<Customer> customers;
    private List<Room> rooms;
    private List<Booking> bookings;
    private List<IncomeRecord> incomeRecords;

    private HotelManager() {
        customers = new ArrayList<>();
        rooms = new ArrayList<>();
        bookings = new ArrayList<>();
        incomeRecords = new ArrayList<>();
    }

    public static synchronized HotelManager getInstance() {
        if (instance == null) {
            instance = new HotelManager();
        }
        return instance;
    }

    // Customer management
    public void addCustomer(Customer customer) {
        if (customers.contains(customer)) {
            throw new IllegalArgumentException("Customer with ID " + customer.getId() + " already exists.");
        }
        customers.add(customer);
    }

    public void updateCustomer(Customer updatedCustomer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equals(updatedCustomer.getId())) {
                customers.set(i, updatedCustomer);
                return;
            }
        }
        throw new IllegalArgumentException("Customer with ID " + updatedCustomer.getId() + " not found.");
    }

    public void deleteCustomer(String customerId) {
        customers.removeIf(customer -> customer.getId().equals(customerId));
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    // Room management
    public void addRoom(Room room) {
        if (rooms.contains(room)) {
            throw new IllegalArgumentException("Room with number " + room.getRoomNumber() + " already exists.");
        }
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    public List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (isRoomAvailable(room, startDate, endDate)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    // Booking management
    public void addBooking(Booking booking) {
        if (isRoomAvailable(booking.getRoom(), booking.getStartDate(), booking.getEndDate())) {
            bookings.add(booking);
            booking.getRoom().setAvailable(false);
            if (booking.getDeposit() > 0) { // Only record deposit for pre-bookings
                addIncome(booking, booking.getDeposit(), "Booked");
            }
        } else {
            throw new IllegalArgumentException("Room " + booking.getRoom().getRoomNumber() + " is not available for the selected dates.");
        }
    }

    public Booking createCheckInBooking(Customer customer, Room room, LocalDate startDate, LocalDate endDate) {
        String bookingId = "B" + (bookings.size() + 1);
        Booking booking = new Booking(bookingId, customer, room, startDate, endDate, 0); // No deposit for check-in
        booking.setCheckedIn(true); // Mark as checked in
        addBooking(booking); // Will not create income record since deposit is 0
        return booking;
    }

    public void checkInBooking(Booking booking) {
        booking.setCheckedIn(true);
        // No income record created for check-in
    }

    public void checkOutBooking(Booking booking, double totalAmount) {
        // Remove existing "Booked" and "Checked In" records for this booking
        incomeRecords.removeIf(record ->
            record.getRoomNumber().equals(booking.getRoom().getRoomNumber()) &&
            record.getCustomerName().equals(booking.getCustomer().getName()) &&
            (record.getStatus().equals("Booked") || record.getStatus().equals("Checked In")));
        // Record total cost as a single "Checked Out" record
        addIncome(booking, totalAmount, "Checked Out");
        bookings.remove(booking);
        booking.getRoom().setAvailable(true);
    }

    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public Booking findBookingByRoom(Room room) {
        for (Booking booking : bookings) {
            if (booking.getRoom().equals(room)) {
                return booking;
            }
        }
        return null;
    }

    // Income management
    public void addIncome(Booking booking, double amount, String status) {
        IncomeRecord record = new IncomeRecord(
            booking.getCustomer().getName(),
            booking.getRoom().getRoomNumber(),
            booking.getRoom().getType(),
            amount,
            LocalDate.now(),
            status
        );
        incomeRecords.add(record);
    }

    public List<IncomeRecord> getIncomeRecords() {
        return new ArrayList<>(incomeRecords);
    }

    public double getTotalIncome() {
        return incomeRecords.stream().mapToDouble(IncomeRecord::getAmount).sum();
    }

    private boolean isRoomAvailable(Room room, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return false;
        }
        for (Booking booking : bookings) {
            if (booking.getRoom().equals(room)) {
                if (!(endDate.isBefore(booking.getStartDate()) || startDate.isAfter(booking.getEndDate()))) {
                    return false;
                }
            }
        }
        return true;
    }
}