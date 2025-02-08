package com.library;

import com.library.models.Book;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LibraryDataManager {
    private ObservableList<Book> books;
    private ObservableList<Member> members;
    private ObservableList<BorrowRecord> borrowRecords;
    private List<Runnable> memberUpdateListeners = new ArrayList<>();
    private List<Runnable> bookUpdateListeners = new ArrayList<>();

    private static LibraryDataManager instance;

    private LibraryDataManager() {
        books = FXCollections.observableArrayList();
        members = FXCollections.observableArrayList();
        borrowRecords = FXCollections.observableArrayList();
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
    }

    public static LibraryDataManager getInstance() {
        if (instance == null) {
            instance = new LibraryDataManager();
        }
        return instance;
    }

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

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return new ArrayList<>(borrowRecords);
    }

    public BorrowRecord createBorrowRecord(String bookId, String memberId) {
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

    public boolean returnBook(String borrowId) {
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
}