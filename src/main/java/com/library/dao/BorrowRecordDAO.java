package com.library.dao;

import com.library.models.Book;
import com.library.models.BorrowRecord;
import com.library.models.Member;
import com.library.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {
    private BookDAO bookDAO;
    private MemberDAO memberDAO;

    public BorrowRecordDAO() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
    }

    /**
     * ดึงบันทึกการยืม-คืนทั้งหมดจากฐานข้อมูล
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        List<Object[]> rawData = new ArrayList<>(); // เก็บข้อมูลดิบ

        String query = "SELECT id, book_id, member_id, borrow_date, return_date, returned FROM borrow_records";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = new Object[6];
                row[0] = rs.getString("id");
                row[1] = rs.getInt("book_id");
                row[2] = rs.getInt("member_id");
                row[3] = rs.getTimestamp("borrow_date");
                row[4] = rs.getTimestamp("return_date");
                row[5] = rs.getBoolean("returned");
                rawData.add(row);
            }
            System.out.println("BorrowRecordDAO: Found " + rawData.size() + " raw records from database");
        } catch (SQLException e) {
            System.err.println("Error fetching borrow records: " + e.getMessage());
            e.printStackTrace();
            return records;
        }

        // สร้าง objects จากข้อมูลดิบ
        for (Object[] row : rawData) {
            String id = (String) row[0];
            int bookId = (int) row[1];
            int memberId = (int) row[2];
            Timestamp borrowTimestamp = (Timestamp) row[3];
            Timestamp returnTimestamp = (Timestamp) row[4];
            boolean returned = (boolean) row[5];

            Book book = bookDAO.getBookById(String.valueOf(bookId));
            Member member = memberDAO.getMemberById(String.valueOf(memberId));

            if (book != null && member != null) {
                BorrowRecord record = new BorrowRecord(book, member);
                record.setId(id);
                record.setBorrowDate(borrowTimestamp.toLocalDateTime());

                if (returnTimestamp != null) {
                    record.setReturnDate(returnTimestamp.toLocalDateTime());
                }

                record.setReturned(returned);
                records.add(record);
            }
        }
        System.out.println("BorrowRecordDAO: Converted " + records.size() + " valid BorrowRecord objects");
        return records;
    }

    /**
     * ดึงบันทึกการยืม-คืนตาม ID
     */
    public BorrowRecord getBorrowRecordById(String id) {
        String query = "SELECT id, book_id, member_id, borrow_date, return_date, returned FROM borrow_records WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createBorrowRecordFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrow record by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * เพิ่มบันทึกการยืมใหม่ในฐานข้อมูล
     */
    public boolean addBorrowRecord(BorrowRecord record) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // เพิ่มบันทึกการยืม
            String query = "INSERT INTO borrow_records (id, book_id, member_id, borrow_date, returned) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, record.getId());
                pstmt.setInt(2, Integer.parseInt(record.getBook().getId()));
                pstmt.setInt(3, Integer.parseInt(record.getMember().getId()));
                pstmt.setTimestamp(4, Timestamp.valueOf(record.getBorrowDate()));
                pstmt.setBoolean(5, record.isReturned());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    return false;
                }
            }

            // อัปเดตสถานะหนังสือ
            String bookQuery = "UPDATE books SET available = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(bookQuery)) {
                pstmt.setBoolean(1, false); // ไม่พร้อมให้ยืม
                pstmt.setInt(2, Integer.parseInt(record.getBook().getId()));

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    return false;
                }
            }

            // ถ้าทั้งสองคำสั่งสำเร็จ
            conn.commit();
            success = true;
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            System.err.println("Error adding borrow record: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    // ตรวจสอบว่า connection ยังเปิดอยู่หรือไม่ก่อนเรียก setAutoCommit
                    if (!conn.isClosed()) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    /**
     * บันทึกการคืนหนังสือ
     */
    public boolean returnBook(String recordId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. ดึงข้อมูล BorrowRecord และ book_id ก่อน
            String getRecordQuery = "SELECT book_id FROM borrow_records WHERE id = ?";
            int bookId = 0;

            try (PreparedStatement stmt = conn.prepareStatement(getRecordQuery)) {
                stmt.setString(1, recordId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                        System.out.println("Found book ID: " + bookId + " for borrow record: " + recordId);
                    } else {
                        System.out.println("No borrow record found with ID: " + recordId);
                        return false; // ไม่พบบันทึกการยืม
                    }
                }
            }

            // 2. อัปเดตบันทึกการยืม
            String updateRecordQuery = "UPDATE borrow_records SET return_date = ?, returned = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRecordQuery)) {
                LocalDateTime returnDate = LocalDateTime.now();
                pstmt.setTimestamp(1, Timestamp.valueOf(returnDate));
                pstmt.setBoolean(2, true);
                pstmt.setString(3, recordId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0 && bookId > 0) {
                    // 3. อัปเดตสถานะหนังสือ
                    String updateBookQuery = "UPDATE books SET available = ? WHERE id = ?";
                    try (PreparedStatement bookStmt = conn.prepareStatement(updateBookQuery)) {
                        bookStmt.setBoolean(1, true); // หนังสือพร้อมให้ยืม
                        bookStmt.setInt(2, bookId);

                        int bookRowsAffected = bookStmt.executeUpdate();
                        System.out.println("Updated book availability, rows affected: " + bookRowsAffected);


                        if (bookRowsAffected > 0) {
                            conn.commit();
                            return true;
                        }
                    }
                }

                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * ดึงบันทึกการยืม-คืนของสมาชิกคนใดคนหนึ่ง
     */
    public List<BorrowRecord> getBorrowRecordsByMember(String memberId) {
        List<BorrowRecord> records = new ArrayList<>();
        String query = "SELECT id, book_id, member_id, borrow_date, return_date, returned FROM borrow_records WHERE member_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Integer.parseInt(memberId)); // แก้เป็น setInt และแปลง String เป็น int

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRecord record = createBorrowRecordFromResultSet(rs);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrow records by member: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * ดึงบันทึกการยืม-คืนที่ยังไม่ได้คืน
     */
    public List<BorrowRecord> getActiveBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        List<Object[]> rawData = new ArrayList<>(); // เก็บข้อมูลดิบก่อนที่ ResultSet จะถูกปิด

        String query = "SELECT id, book_id, member_id, borrow_date, return_date, returned FROM borrow_records WHERE returned = false";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                BorrowRecord record = createBorrowRecordFromResultSet(rs);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active borrow records: " + e.getMessage());
            e.printStackTrace();
            return records;
        }
        for (Object[] row : rawData) {
            String id = (String) row[0];
            int bookId = (int) row[1];
            int memberId = (int) row[2];
            Timestamp borrowTimestamp = (Timestamp) row[3];
            Timestamp returnTimestamp = (Timestamp) row[4];
            boolean returned = (boolean) row[5];
            Book book = bookDAO.getBookById(String.valueOf(bookId));
            Member member = memberDAO.getMemberById(String.valueOf(memberId));

            if (book != null && member != null) {
                BorrowRecord record = new BorrowRecord(book, member);
                record.setId(id);
                record.setBorrowDate(borrowTimestamp.toLocalDateTime());

                if (returnTimestamp != null) {
                    record.setReturnDate(returnTimestamp.toLocalDateTime());
                }

                record.setReturned(returned);
                records.add(record);
            }
        }

        return records;
    }


    /**
     * ดึงบันทึกการยืม-คืนที่เกินกำหนดส่ง
     */
    public List<BorrowRecord> getOverdueBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();

        // สมมติว่ากำหนดส่งคือ 14 วัน
        String query = "SELECT id, book_id, member_id, borrow_date, return_date, returned FROM borrow_records " +
                "WHERE returned = false AND borrow_date < DATE_SUB(NOW(), INTERVAL 14 DAY)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                BorrowRecord record = createBorrowRecordFromResultSet(rs);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching overdue borrow records: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * สร้าง BorrowRecord จาก ResultSet
     */
    private BorrowRecord createBorrowRecordFromResultSet(ResultSet rs) throws SQLException {

        String id = rs.getString("id");
        int bookId = rs.getInt("book_id");
        int memberId = rs.getInt("member_id");
        Timestamp borrowTimestamp = rs.getTimestamp("borrow_date");
        Timestamp returnTimestamp = rs.getTimestamp("return_date");
        boolean returned = rs.getBoolean("returned");

        Book book = bookDAO.getBookById(String.valueOf(bookId));
        Member member = memberDAO.getMemberById(String.valueOf(memberId));

        if (book != null && member != null) {
            LocalDateTime borrowDate = borrowTimestamp.toLocalDateTime();

            BorrowRecord record = new BorrowRecord(book, member);
            record.setId(id);
            record.setBorrowDate(borrowDate);

            if (returnTimestamp != null) {
                record.setReturnDate(returnTimestamp.toLocalDateTime());
            }

            record.setReturned(returned);
            return record;
        }

        return null;
    }

    /**
     * สร้าง ID ใหม่สำหรับบันทึกการยืม
     */
    public String generateNewBorrowId() {
        String query = "SELECT MAX(SUBSTRING(id, 3)) AS max_id FROM borrow_records WHERE id LIKE 'BR%'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String maxIdStr = rs.getString("max_id");
                if (maxIdStr != null) {
                    int maxId = Integer.parseInt(maxIdStr);
                    return "BR" + String.format("%05d", maxId + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating new borrow record ID: " + e.getMessage());
            e.printStackTrace();
        }

        return "BR00001"; // ค่าเริ่มต้นหากยังไม่มีบันทึกเลย
    }

    /**
     * ดึงจำนวนหนังสือที่สมาชิกยืมไปแล้วตอนนี้
     */
    public int getCurrentBorrowCountByMember(String memberId) {
        String query = "SELECT COUNT(*) FROM borrow_records WHERE member_id = ? AND returned = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Integer.parseInt(memberId)); // แก้เป็น setInt และแปลง String เป็น int

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting current borrows: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * ลบบันทึกการยืม-คืน (ใช้เฉพาะกรณีพิเศษ)
     */
    public boolean deleteBorrowRecord(String id) {
        String query = "DELETE FROM borrow_records WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting borrow record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
}