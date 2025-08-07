package final_java.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelManager {

    private static HotelManager instance;
    private List<Customer> customers;
    private List<Room> rooms;
    private List<Booking> bookings;
    private List<IncomeRecord> incomeRecords; 

    // New class to store income details
    public static class IncomeRecord {

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

    public Booking findBookingByRoom(Room room, LocalDate date) {
        for (Booking booking : bookings) {
            if (booking.getRoom().equals(room)
                    && (date.isEqual(booking.getStartDate()) || date.isEqual(booking.getEndDate())
                    || (date.isAfter(booking.getStartDate()) && date.isBefore(booking.getEndDate())))) {
                return booking;
            }
        }
        return null;
    }

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
        } else {
            throw new IllegalArgumentException("Room " + booking.getRoom().getRoomNumber() + " is not available for the selected dates.");
        }
    }

    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public void checkOutBooking(Booking booking) {
        bookings.remove(booking);
        booking.getRoom().setAvailable(true);
    }

//    public Booking findBookingByRoom(Room room, LocalDate date) {
//        for (Booking booking : bookings) {
//            if (booking.getRoom().equals(room)
//                    && (date.isEqual(booking.getStartDate()) || date.isEqual(booking.getEndDate())
//                    || (date.isAfter(booking.getStartDate()) && date.isBefore(booking.getEndDate())))) {
//                return booking;
//            }
//        }
//        return null;
//    }

    // Income management
    public void addIncome(Booking booking, double amount) {
        IncomeRecord record = new IncomeRecord(
                booking.getCustomer().getName(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getType(), // Assumes Room has getType()
                amount,
                LocalDate.now()
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
