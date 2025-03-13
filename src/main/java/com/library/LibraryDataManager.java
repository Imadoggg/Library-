package com.library;

import com.library.dao.BookDAO;
import com.library.dao.BorrowRecordDAO;
import com.library.dao.MemberDAO;
import com.library.dao.UserDAO;
import com.library.models.Book;
import com.library.models.BorrowRecord;
import com.library.models.Member;
import com.library.models.User;
import com.library.models.UserRole;
import com.library.utils.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibraryDataManager {
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private BorrowRecordDAO borrowRecordDAO;
    private UserDAO userDAO;

    private User currentUser;
    private List<Runnable> memberUpdateListeners = new ArrayList<>();
    private List<Runnable> bookUpdateListeners = new ArrayList<>();

    private static LibraryDataManager instance;

    private LibraryDataManager() {
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        borrowRecordDAO = new BorrowRecordDAO();
        userDAO = new UserDAO();
    }

    public static LibraryDataManager getInstance() {
        if (instance == null) {
            instance = new LibraryDataManager();
        }
        return instance;
    }
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordDAO.getAllBorrowRecords();
    }

    // Member update listeners
    public void addMemberUpdateListener(Runnable listener) {
        memberUpdateListeners.add(listener);
    }

    public void addBookUpdateListener(Runnable listener) {
        bookUpdateListeners.add(listener);
    }

    protected void notifyMemberListeners() {
        memberUpdateListeners.forEach(Runnable::run);
    }

    protected void notifyBookListeners() {
        bookUpdateListeners.forEach(Runnable::run);
    }

    // Book related methods
    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public boolean addBook(Book book) {
        boolean result = bookDAO.addBook(book);
        if (result) {
            notifyBookListeners();
        }
        return result;
    }

    public Book findBookById(String id) {
        return bookDAO.getBookById(id);
    }

    public List<Book> findBooksByTitle(String title) {
        return bookDAO.findBooksByTitle(title);
    }

    public List<Book> findBooksByCategory(String category) {
        if (category == null || category.trim().isEmpty() ||
                category.equals("ทั้งหมด") || category.equals("All")) {
            return getAllBooks();
        }
        return bookDAO.findBooksByCategory(category);
    }

    // Member related methods
    public List<Member> getAllMembers() {
        return memberDAO.getAllMembers();
    }

    public boolean addMember(Member member) {
        boolean result = memberDAO.addMember(member);
        if (result) {
            notifyMemberListeners();
        }
        return result;
    }

    public Member findMemberById(String id) {
        return memberDAO.getMemberById(id);
    }

    // Borrow record related methods - เพิ่มเติมตามความเหมาะสม

    public boolean login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // สำคัญ: ต้องดึงข้อมูลผู้ใช้และกำหนดค่า currentUser
                this.currentUser = userDAO.getUserByUsername(username);
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    // Application shutdown
    public void shutdown() {
        DatabaseConnection.closeConnection();
    }
    /**
     * ดึงบันทึกการยืม-คืนทั้งหมด
     */

    /**
     * ดึงบันทึกการยืม-คืนที่ยังไม่ได้คืน
     */
    public List<BorrowRecord> getActiveBorrowRecords() {
        return borrowRecordDAO.getActiveBorrowRecords();
    }

    /**
     * ดึงบันทึกการยืม-คืนของสมาชิกคนใดคนหนึ่ง
     */
    public List<BorrowRecord> getBorrowRecordsByMember(String memberId) {
        return borrowRecordDAO.getBorrowRecordsByMember(memberId);
    }

    /**
     * ดึงบันทึกการยืม-คืนที่เกินกำหนด
     */
    public List<BorrowRecord> getOverdueBorrowRecords() {
        return borrowRecordDAO.getOverdueBorrowRecords();
    }
    /**
     * สร้างบันทึกการยืมหนังสือใหม่
     * @param bookId รหัสหนังสือ
     * @param memberId รหัสสมาชิก
     * @return บันทึกการยืมหนังสือที่สร้างใหม่ หรือ null ถ้าไม่สำเร็จ
     */
    public BorrowRecord createBorrowRecord(String bookId, String memberId) {
        Book book = bookDAO.getBookById(bookId);
        Member member = memberDAO.getMemberById(memberId);

        if (book != null && member != null && book.isAvailable()) {
            BorrowRecord record = new BorrowRecord(book, member);
            record.setId(borrowRecordDAO.generateNewBorrowId());

            boolean success = borrowRecordDAO.addBorrowRecord(record);
            if (success) {
                notifyBookListeners(); // แจ้งเตือนผู้ฟัง
                return record;
            }
        }
        return null;
    }
    /**
     * บันทึกการคืนหนังสือ
     * @param borrowId รหัสของบันทึกการยืม
     * @return true ถ้าคืนสำเร็จ, false ถ้าไม่สำเร็จ
     */
    public boolean returnBook(String borrowId) {
        boolean result = borrowRecordDAO.returnBook(borrowId);
        if (result) {
            notifyBookListeners(); // แจ้งเตือนผู้ฟัง
        }
        return result;
    }

}