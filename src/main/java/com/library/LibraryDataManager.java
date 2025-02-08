package com;

import com.library.models.Book;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryDataManager {
    private List<Book> books;
    private List<Member> members;
    private List<BorrowRecord> borrowRecords;
    private static LibraryDataManager instance;

    private LibraryDataManager() {
        books = new ArrayList<>();
        members = new ArrayList<>();
        borrowRecords = new ArrayList<>();
        // เพิ่มข้อมูลตัวอย่าง
        addSampleData();
    }

    public static LibraryDataManager getInstance() {
        if (instance == null) {
            instance = new LibraryDataManager();
        }
        return instance;
    }

    private void addSampleData() {
        // เพิ่มหนังสือตัวอย่าง
        books.add(new Book("B001", "Java Programming", "John Smith", "Programming"));
        books.add(new Book("B002", "Python Basics", "Jane Doe", "Programming"));
        books.add(new Book("B003", "Database Design", "Mike Johnson", "Database"));

        // เพิ่มสมาชิกตัวอย่าง
        members.add(new Member("M001", "สมชาย ใจดี", "081-234-5678"));
        members.add(new Member("M002", "สมหญิง รักการอ่าน", "089-876-5432"));
    }

    // Book Management
    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(String bookId) {
        books.removeIf(book -> book.getId().equals(bookId));
    }

    public Book findBookById(String id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Book> findBooksByTitle(String title) {
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return new ArrayList<>(books);
        }
        return books.stream()
                .filter(book -> book.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    // Member Management
    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(String memberId) {
        members.removeIf(member -> member.getId().equals(memberId));
    }

    public Member findMemberById(String id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    // Borrow-Return Management
    public BorrowRecord createBorrowRecord(String bookId, String memberId) {
        Book book = findBookById(bookId);
        Member member = findMemberById(memberId);

        if (book != null && member != null && book.isAvailable()) {
            BorrowRecord record = new BorrowRecord(book, member);
            borrowRecords.add(record);
            book.setAvailable(false);
            member.addBorrowRecord(record);
            return record;
        }
        return null;
    }

    public boolean returnBook(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(br -> br.getId().equals(borrowId))
                .findFirst()
                .orElse(null);

        if (record != null && !record.isReturned()) {
            record.returnBook();
            record.getBook().setAvailable(true);
            return true;
        }
        return false;
    }

    public List<BorrowRecord> getBorrowHistory(String memberId) {
        return borrowRecords.stream()
                .filter(record -> record.getMember().getId().equals(memberId))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }
    public List<BorrowRecord> getAllBorrowRecords() {
        return new ArrayList<>(borrowRecords);
    }
}