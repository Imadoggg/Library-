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

    // User related methods
    public boolean login(String username, String password) {
        boolean isValid = userDAO.validateCredentials(username, password);
        if (isValid) {
            currentUser = userDAO.getUserByUsername(username);
            return true;
        }
        return false;
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
                notifyBookListeners();
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
            notifyBookListeners();
        }
        return result;
    }
    /**
     * อัปเดตข้อมูลผู้ใช้
     * @param user ข้อมูลผู้ใช้ที่ต้องการอัปเดต
     * @return true ถ้าอัปเดตสำเร็จ, false ถ้าไม่สำเร็จ
     */
    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    /**
     * ตรวจสอบข้อมูลเข้าสู่ระบบ
     * @param username ชื่อผู้ใช้
     * @param password รหัสผ่าน
     * @return true ถ้าข้อมูลถูกต้อง, false ถ้าไม่ถูกต้อง
     */
    public boolean validateCredentials(String username, String password) {
        return userDAO.validateCredentials(username, password);
    }
    /**
     * เปลี่ยนรหัสผ่านของผู้ใช้
     * @param username ชื่อผู้ใช้
     * @param newPassword รหัสผ่านใหม่
     * @return true ถ้าเปลี่ยนสำเร็จ, false ถ้าไม่สำเร็จ
     */

    public boolean changePassword(String username, String newPassword) {
        return userDAO.changePassword(username, newPassword);
    }
}