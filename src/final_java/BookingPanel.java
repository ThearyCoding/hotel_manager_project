/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package final_java;

import final_java.models.Booking;
import final_java.models.Customer;
import final_java.models.HotelManager;
import final_java.models.Room;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LONGMENG
 */
public class BookingPanel extends javax.swing.JPanel {

    private JpanelLoader jpload = new JpanelLoader();
    private HotelManager hotelManager;
    private DefaultTableModel tableModel;
    private RoomPanel roomPanel;

    public BookingPanel(HotelManager hotelManager) {
        if (hotelManager == null) {
            throw new IllegalArgumentException("HotelManager cannot be null");
        }
        this.hotelManager = hotelManager;
        this.roomPanel = roomPanel; // Note: roomPanel is null unless passed; consider initializing if needed
        initComponents();
        populateCustomerComboBox();
        // Set default dates to tomorrow and two days from now
        dtCheckIn.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        dtCheckOut.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(2)));
        updateRoomComboBox();
        initializeBookingTable();
        refreshBookingTable();
        addDateChangeListeners();
        addRoomChangeListener();
        updateDepositAmount(); // Initial deposit calculation
        System.out.println("BookingPanel initialized with " + hotelManager.getRooms().size() + " rooms");
        for (Room room : hotelManager.getRooms()) {
            System.out.println("Room: " + room.getRoomNumber() + " - " + room.getType() + " ($" + room.getPrice() + ")");
        }
    }

    private void populateCustomerComboBox() {
        cbocustomer.removeAllItems();
        cbocustomer.addItem("Select Customer");
        for (Customer customer : hotelManager.getCustomers()) {
            cbocustomer.addItem(customer.getId() + " - " + customer.getName());
        }
        if (hotelManager.getCustomers().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers available. Please add customers via Customer panel.",
                    "No Customers", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateRoomComboBox() {
        cboroom.removeAllItems();
        cboroom.addItem("Select Room");
        LocalDate startDate = dtCheckIn.getDate() != null
                ? dtCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalDate endDate = dtCheckOut.getDate() != null
                ? dtCheckOut.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;

        List<Room> roomsToDisplay;
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            roomsToDisplay = hotelManager.getRooms();
            if (!roomsToDisplay.isEmpty()) {
                cboroom.addItem("Select valid dates to check availability");
            }
        } else {
            roomsToDisplay = hotelManager.getAvailableRooms(startDate, endDate);
            System.out.println("Available rooms for " + startDate + " to " + endDate + ": " + roomsToDisplay.size());
            for (Booking booking : hotelManager.getBookings()) {
                System.out.println("Booking: Room " + booking.getRoom().getRoomNumber() + ", "
                        + booking.getStartDate() + " to " + booking.getEndDate());
            }
        }

        for (Room room : roomsToDisplay) {
            cboroom.addItem(room.getRoomNumber() + " - " + room.getType() + " ($" + room.getPrice() + ")");
        }

        if (roomsToDisplay.isEmpty() && startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this,
                    "No rooms available for selected dates. Try different dates or check bookings in CheckOut panel.",
                    "No Rooms Available", JOptionPane.WARNING_MESSAGE);
        } else if (hotelManager.getRooms().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No rooms exist. Please add rooms via Room panel.",
                    "No Rooms", JOptionPane.WARNING_MESSAGE);
        }
        updateDepositAmount(); // Update deposit after room list changes
    }

    private void addDateChangeListeners() {
        PropertyChangeListener dateListener = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                updateRoomComboBox();
                updateDepositAmount();
            }
        };
        dtCheckIn.addPropertyChangeListener(dateListener);
        dtCheckOut.addPropertyChangeListener(dateListener);
    }

    private void addRoomChangeListener() {
        cboroom.addActionListener(evt -> updateDepositAmount());
    }

    private void updateDepositAmount() {
        String roomSelection = (String) cboroom.getSelectedItem();
        LocalDate startDate = dtCheckIn.getDate() != null
                ? dtCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalDate endDate = dtCheckOut.getDate() != null
                ? dtCheckOut.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;

        if (roomSelection == null || roomSelection.equals("Select Room")
                || roomSelection.equals("Select valid dates to check availability")
                || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            txtdepositAmount.setText("");
            return;
        }

        try {
            String roomNumber = roomSelection.split(" - ")[0];
            Room room = hotelManager.getRooms().stream()
                    .filter(r -> r.getRoomNumber().equals(roomNumber))
                    .findFirst()
                    .orElse(null);
            if (room == null) {
                txtdepositAmount.setText("");
                return;
            }

            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (days == 0) {
                days = 1; // Minimum 1 day for same-day bookings
            }
            double totalCost = room.getPrice() * days;
            double deposit = 0.2 * totalCost;
            txtdepositAmount.setText(String.format("%.2f", deposit));
        } catch (Exception e) {
            txtdepositAmount.setText("");
            System.out.println("Error calculating deposit: " + e.getMessage());
        }
    }

    private void initializeBookingTable() {
        tableModel = new DefaultTableModel(new Object[]{"Name", "Room Number", "Booking Date", "Check-Out Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(tableModel);
    }

    private void refreshBookingTable() {
        tableModel.setRowCount(0);
        for (Booking booking : hotelManager.getBookings()) {
            tableModel.addRow(new Object[]{
                booking.getCustomer().getName(),
                booking.getRoom().getRoomNumber(),
                booking.getStartDate().toString(),
                booking.getEndDate().toString(),
                booking.isCheckedIn() ? "Checked In" : "Booked"
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        panel_load = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        btnbooking = new javax.swing.JButton();
        cbocustomer = new javax.swing.JComboBox<>();
        cboroom = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtdepositAmount = new javax.swing.JTextField();
        dtCheckIn = new com.toedter.calendar.JDateChooser();
        dtCheckOut = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();

        jPanel7.setBackground(new java.awt.Color(0, 51, 153));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panel_load.setBackground(new java.awt.Color(0, 51, 255));
        panel_load.setLayout(null);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(0, 51, 153));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 540, -1));

        jLabel1.setFont(new java.awt.Font("Century Gothic", 0, 32)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        jLabel1.setText("Book a Room");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 2, 344, 60));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 62, 505, 10));

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("Select Customer:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 170, 30));

        btnbooking.setBackground(new java.awt.Color(0, 204, 0));
        btnbooking.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        btnbooking.setForeground(new java.awt.Color(255, 255, 255));
        btnbooking.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/add customer_1.png"))); // NOI18N
        btnbooking.setText("Booking");
        btnbooking.setBorder(null);
        btnbooking.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnbooking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbookingActionPerformed(evt);
            }
        });
        jPanel1.add(btnbooking, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 385, 510, 50));

        cbocustomer.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        cbocustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cbocustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 500, 33));

        cboroom.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        cboroom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cboroom, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 500, 33));

        jLabel8.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Select Available Room:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 220, 30));

        jLabel9.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(102, 102, 102));
        jLabel9.setText("Check-In Date:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 170, 30));

        jLabel10.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(102, 102, 102));
        jLabel10.setText("Check-Out Date:");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, 170, 30));

        jLabel11.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(102, 102, 102));
        jLabel11.setText("Deposit Amount");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, 170, 30));

        txtdepositAmount.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtdepositAmount.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.add(txtdepositAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 500, 33));
        jPanel1.add(dtCheckIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 500, 30));
        jPanel1.add(dtCheckOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 500, 30));

        panel_load.add(jPanel1);
        jPanel1.setBounds(16, 140, 540, 450);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel5.setBackground(new java.awt.Color(0, 51, 153));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 590, -1));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 0, 32)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Active Bookings");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, -1, 340, 60));
        jPanel2.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 550, 10));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Room Number", "Booking Date", "Check-Out Date", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 550, 340));

        panel_load.add(jPanel2);
        jPanel2.setBounds(580, 140, 590, 450);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSeparator1.setBackground(new java.awt.Color(0, 51, 153));
        jSeparator1.setForeground(new java.awt.Color(0, 51, 102));
        jPanel3.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 93, 1088, 20));

        jLabel3.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 102, 204));
        jLabel3.setText("Booing (20% Deposit)");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 6, 250, 90));

        jPanel18.setBackground(new java.awt.Color(255, 255, 255));
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel19.setBackground(new java.awt.Color(0, 136, 245));
        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel18.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 8, -1, -1));

        jPanel20.setBackground(new java.awt.Color(0, 171, 41));

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/bed1.png"))); // NOI18N

        jLabel21.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Room");
        jLabel21.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel21MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addGap(33, 33, 33))
        );

        jPanel18.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, 130, 63));

        jPanel21.setBackground(new java.awt.Color(204, 0, 0));

        jLabel22.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Check Out");
        jLabel22.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel22MouseClicked(evt);
            }
        });

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/check out 1.png"))); // NOI18N

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(33, 33, 33))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(jLabel22))
        );

        jPanel18.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, 130, 63));

        jPanel22.setBackground(new java.awt.Color(79, 0, 139));

        jLabel24.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Booking");
        jLabel24.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel24MouseClicked(evt);
            }
        });

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/booking 1_1.png"))); // NOI18N

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel24)
                .addContainerGap(37, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(jLabel24))
        );

        jPanel18.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 10, -1, 63));

        jPanel23.setBackground(new java.awt.Color(40, 189, 156));

        jLabel26.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("Income");
        jLabel26.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel26MouseClicked(evt);
            }
        });

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/income_1.png"))); // NOI18N

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel26)
                .addContainerGap(38, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(jLabel26))
        );

        jPanel18.add(jPanel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 10, -1, 63));

        jPanel24.setBackground(new java.awt.Color(255, 153, 0));

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/check in1.png"))); // NOI18N

        jLabel29.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("Check In");
        jLabel29.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel29.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel29MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel29)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(jLabel29))
        );

        jPanel18.add(jPanel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, -1, 63));

        jPanel25.setBackground(new java.awt.Color(0, 97, 234));

        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/final_java/icon/add customer_1.png"))); // NOI18N

        jLabel31.setFont(new java.awt.Font("Candara", 1, 16)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("Customer");
        jLabel31.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel31.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel31MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel25Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel31)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel25Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel31)
                .addGap(39, 39, 39))
        );

        jPanel18.add(jPanel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 130, 63));

        jPanel3.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, 840, 80));

        panel_load.add(jPanel3);
        jPanel3.setBounds(20, 10, 1150, 110);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(1272, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_load, javax.swing.GroupLayout.PREFERRED_SIZE, 1199, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(88, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(638, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_load, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(29, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnbookingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbookingActionPerformed
       

        try {
            // Get selected customer
            String customerSelection = (String) cbocustomer.getSelectedItem();
            if (customerSelection == null || customerSelection.equals("Select Customer")) {
                throw new IllegalArgumentException("Please select a customer.");
            }
            String customerId = customerSelection.split(" - ")[0];
            Customer customer = hotelManager.getCustomers().stream()
                    .filter(c -> c.getId().equals(customerId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

            // Get selected room
            String roomSelection = (String) cboroom.getSelectedItem();
            if (roomSelection == null || roomSelection.equals("Select Room")
                    || roomSelection.equals("Select valid dates to check availability")) {
                throw new IllegalArgumentException("Please select a room.");
            }
            String roomNumber = roomSelection.split(" - ")[0];
            Room room = hotelManager.getRooms().stream()
                    .filter(r -> r.getRoomNumber().equals(roomNumber))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Room not found."));

            // Get dates
            LocalDate startDate = dtCheckIn.getDate() != null
                    ? dtCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            LocalDate endDate = dtCheckOut.getDate() != null
                    ? dtCheckOut.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Invalid booking or check-out date.");
            }
            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Booking date must be today or in the future.");
            }

            // Verify room availability
            if (!hotelManager.getAvailableRooms(startDate, endDate).contains(room)) {
                throw new IllegalArgumentException("Selected room is not available for the chosen dates.");
            }

            // Get deposit (20% of room price per day)
            String depositStr = txtdepositAmount.getText().trim();
            double deposit;
            try {
                deposit = Double.parseDouble(depositStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid deposit amount.");
            }
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (days == 0) {
                days = 1; // Minimum 1 day for same-day bookings
            }
            double expectedDeposit = 0.2 * room.getPrice() * days;
            if (Math.abs(deposit - expectedDeposit) > 0.01) {
                throw new IllegalArgumentException("Deposit must be exactly 20% of total cost ($" + String.format("%.2f", expectedDeposit) + ").");
            }

            // Create booking
            String bookingId = "B" + (hotelManager.getBookings().size() + 1);
            Booking booking = new Booking(bookingId, customer, room, startDate, endDate, deposit);
            hotelManager.addBooking(booking); // Deposit recorded as income in addBooking

            JOptionPane.showMessageDialog(this, "Booking added successfully! Booking ID: " + bookingId);
            refreshBookingTable();
            populateCustomerComboBox();
            updateRoomComboBox();
            dtCheckIn.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            dtCheckOut.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(2)));
            txtdepositAmount.setText("");

            // Navigate to IncomePanel to show updated records
            IncomePanel inc = new IncomePanel(hotelManager);
            jpload.jPanelLoader(panel_load, inc);

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnbookingActionPerformed

    private void jLabel21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseClicked
        // TODO add your handling code here:
        RoomPanel rm = new RoomPanel(hotelManager);
        jpload.jPanelLoader(panel_load, rm);

    }//GEN-LAST:event_jLabel21MouseClicked

    private void jLabel22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseClicked
        // TODO add your handling code here:
        CheckOutPanel chkout = new CheckOutPanel(hotelManager);
        jpload.jPanelLoader(panel_load, chkout);

    }//GEN-LAST:event_jLabel22MouseClicked

    private void jLabel24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseClicked
        // TODO add your handling code here:
        BookingPanel bk = new BookingPanel(hotelManager);
        jpload.jPanelLoader(panel_load, bk);
    }//GEN-LAST:event_jLabel24MouseClicked

    private void jLabel26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseClicked
        // TODO add your handling code here:
        IncomePanel inc = new IncomePanel(hotelManager);
        jpload.jPanelLoader(panel_load, inc);
    }//GEN-LAST:event_jLabel26MouseClicked

    private void jLabel29MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel29MouseClicked
        // TODO add your handling code here:
        CheckInPanel chkin = new CheckInPanel(hotelManager);
        jpload.jPanelLoader(panel_load, chkin);
    }//GEN-LAST:event_jLabel29MouseClicked

    private void jLabel31MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel31MouseClicked
        // TODO add your handling code here:
        CustomerPanel cus = new CustomerPanel(hotelManager);
        jpload.jPanelLoader(panel_load, cus);

    }//GEN-LAST:event_jLabel31MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnbooking;
    private javax.swing.JComboBox<String> cbocustomer;
    private javax.swing.JComboBox<String> cboroom;
    private com.toedter.calendar.JDateChooser dtCheckIn;
    private com.toedter.calendar.JDateChooser dtCheckOut;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panel_load;
    private javax.swing.JTextField txtdepositAmount;
    // End of variables declaration//GEN-END:variables
}
