package final_java.models;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelManager {
    private static HotelManager instance;
    private List<Customer> customers;
    private List<Room> rooms;
    private List<Booking> bookings;

    private HotelManager() {
        customers = new ArrayList<>();
        rooms = new ArrayList<>();
        bookings = new ArrayList<>();
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
        return new ArrayList<>(customers); // Return a copy to prevent external modification
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
            throw new IllegalArgumentException("Room is not available for the selected dates.");
        }
    }

    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public void checkOut(Booking booking) {
        bookings.remove(booking);
        booking.getRoom().setAvailable(true);
    }

    private boolean isRoomAvailable(Room room, LocalDate startDate, LocalDate endDate) {
        for (Booking booking : bookings) {
            if (booking.getRoom().equals(room)) {
                if (!(endDate.isBefore(booking.getStartDate()) || startDate.isAfter(booking.getEndDate()))) {
                    return false; // Date overlap detected
                }
            }
        }
        return true;
    }
}