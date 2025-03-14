package com.library.dao;

import com.library.models.Book;
import com.library.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT id, title, author, category, available FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");
                boolean available = rs.getBoolean("available");

                Book book = new Book(id, title, author, category);
                book.setAvailable(available);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public Book getBookById(String id) {
        String query = "SELECT id, title, author, category, available FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String category = rs.getString("category");
                    boolean available = rs.getBoolean("available");

                    Book book = new Book(id, title, author, category);
                    book.setAvailable(available);
                    return book;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean addBook(Book book) {
        String query = "INSERT INTO books (title, author, category, available) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getCategory());
            pstmt.setBoolean(4, book.isAvailable());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // AUTO INCREMENT
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        book.setId(String.valueOf(generatedId));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBookAvailability(String bookId, boolean available, Connection existingConn) {
        String query = "UPDATE books SET available = ? WHERE id = ?";
        boolean useExistingConnection = (existingConn != null);

        try (Connection conn = useExistingConnection ? existingConn : DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, available);
            pstmt.setInt(2, Integer.parseInt(bookId));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book availability: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
    public List<Book> findBooksByTitle(String title) {
        List<Book> books = new ArrayList<>();
        String query = "SELECT id, title, author, category, available FROM books WHERE title LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + title + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String bookTitle = rs.getString("title");
                    String author = rs.getString("author");
                    String category = rs.getString("category");
                    boolean available = rs.getBoolean("available");

                    Book book = new Book(id, bookTitle, author, category);
                    book.setAvailable(available);
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching books by title: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public List<Book> findBooksByCategory(String category) {
        List<Book> books = new ArrayList<>();
        String query = "SELECT id, title, author, category, available FROM books WHERE category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String bookCategory = rs.getString("category");
                    boolean available = rs.getBoolean("available");

                    Book book = new Book(id, title, author, bookCategory);
                    book.setAvailable(available);
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching books by category: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public String generateNewBookId() {
        String query = "SELECT MAX(SUBSTRING(id, 2)) AS max_id FROM books WHERE id LIKE 'B%'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String maxIdStr = rs.getString("max_id");
                if (maxIdStr != null) {
                    int maxId = Integer.parseInt(maxIdStr);
                    return "B" + String.format("%03d", maxId + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating new book ID: " + e.getMessage());
            e.printStackTrace();
        }

        return "B001";
    }
}