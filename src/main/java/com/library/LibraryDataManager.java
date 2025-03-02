package com.library;

import com.library.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LibraryDataManager {
    private ObservableList<Book> books;
    private ObservableList<Member> members;
    private ObservableList<BorrowRecord> borrowRecords;
    private ObservableList<User> users;
    private User currentUser; // เพิ่มตัวแปรนี้
    private List<Runnable> memberUpdateListeners = new ArrayList<>();
    private List<Runnable> bookUpdateListeners = new ArrayList<>();

    private static LibraryDataManager instance;

    private LibraryDataManager() {
        books = FXCollections.observableArrayList();
        members = FXCollections.observableArrayList();
        borrowRecords = FXCollections.observableArrayList();
        users = FXCollections.observableArrayList();
        addSampleData();
    }

    private void addSampleData() {
        // Add sample books
        books.addAll(
                new Book("B001", "Java Programming", "John Smith", "Programming"),
                new Book("B002", "Python Basics", "Jane Doe", "Programming"),
                new Book("B003", "Data Structures", "Mike Wilson", "Computer Science"),
                new Book("B004", "Web Development", "Sarah Brown", "Programming"),
                new Book("B005", "Database Design", "Tom Davis", "Database")
        );

        // Add sample members
        members.addAll(
                new Member("M001", "John Doe", "123-456-7890"),
                new Member("M002", "Jane Smith", "098-765-4321"),
                new Member("M003", "Bob Wilson", "111-222-3333")
        );

        // เพิ่มผู้ใช้ตัวอย่าง
        users.add(new User("admin", "admin", UserRole.ADMIN, "ผู้ดูแลระบบ", "admin@library.com"));
        users.add(new User("user", "user", UserRole.USER, "ผู้ใช้ทั่วไป", "user@library.com"));
    }

    public static LibraryDataManager getInstance() {
        if (instance == null) {
            instance = new LibraryDataManager();
        }
        return instance;
    }

    // [เมธอดอื่นๆ คงเหมือนเดิม]

    public boolean login(String username, String password) {
        User user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            currentUser = user;
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

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<Book> getAllBooks() {
        if (books == null) {
            books = FXCollections.observableArrayList(); // สร้างใหม่ถ้าเป็น null
            System.err.println("Warning: books collection was null, creating a new one");
        }
        return new ArrayList<>(books);
    }

    public List<Member> getAllMembers() {
        if (members == null) {
            members = FXCollections.observableArrayList();
            System.err.println("Warning: members collection was null, creating a new one");
        }
        return new ArrayList<>(members);
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        if (borrowRecords == null) {
            borrowRecords = FXCollections.observableArrayList();
            System.err.println("Warning: borrowRecords collection was null, creating a new one");
        }
        return new ArrayList<>(borrowRecords);
    }

    // เพิ่มเมธอด addMemberUpdateListener และ addBookUpdateListener
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

    public void addMember(Member member) {
        members.add(member);
        notifyMemberListeners();
    }

    public void addBook(Book book) {
        books.add(book);
        notifyBookListeners();
    }

    // เพิ่มเมธอด createBorrowRecord
    public BorrowRecord createBorrowRecord(String bookId, String memberId) {
        if (books == null || members == null || borrowRecords == null) {
            System.err.println("Error: Collections not initialized properly");
            return null;
        }

        Book book = findBookById(bookId);
        Member member = findMemberById(memberId);

        if (book != null && member != null && book.isAvailable()) {
            BorrowRecord record = new BorrowRecord(book, member);
            book.setAvailable(false);
            member.addBorrowRecord(record);
            borrowRecords.add(record);
            notifyBookListeners();
            return record;
        }
        return null;
    }

    // เพิ่มเมธอด returnBook
    public boolean returnBook(String borrowId) {
        if (borrowRecords == null) {
            System.err.println("Error: borrowRecords collection is null");
            return false;
        }

        BorrowRecord record = borrowRecords.stream()
                .filter(br -> br.getId().equals(borrowId))
                .findFirst()
                .orElse(null);

        if (record != null && !record.isReturned()) {
            record.returnBook();
            notifyBookListeners();
            return true;
        }
        return false;
    }

    // เพิ่มเมธอด findBookById และ findMemberById
    public Book findBookById(String id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Member findMemberById(String id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // เพิ่มเมธอด findBooksByTitle และ findBooksByCategory
    public List<Book> findBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>(books);
        }
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase()
                        .contains(title.toLowerCase().trim()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByCategory(String category) {
        if (category == null || category.trim().isEmpty() || category.equals("ทั้งหมด") || category.equals("All")) {
            return new ArrayList<>(books);
        }
        return books.stream()
                .filter(book -> book.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    // เพิ่มเมธอด reset
    public void reset() {
        // สร้างคอลเลกชันใหม่ทั้งหมด
        books = FXCollections.observableArrayList();
        members = FXCollections.observableArrayList();
        borrowRecords = FXCollections.observableArrayList();
        users = FXCollections.observableArrayList();

        // เพิ่มข้อมูลตัวอย่างใหม่
        addSampleData();

        System.out.println("LibraryDataManager has been reset");
    }


    public void updateUser(User user) {
        // ตรวจสอบว่ามีผู้ใช้นี้อยู่ในระบบหรือไม่
        User existingUser = users.stream()
                .filter(u -> u.getUsername().equals(user.getUsername()))
                .findFirst()
                .orElse(null);

        if (existingUser != null) {
            // อัพเดทข้อมูล
            existingUser.setName(user.getName());
            existingUser.setPassword(user.getPassword());
            System.out.println("อัพเดทข้อมูลผู้ใช้ " + user.getUsername() + " เรียบร้อยแล้ว");
        } else {
            System.err.println("ไม่พบผู้ใช้ " + user.getUsername() + " ในระบบ");
        }
    }
}