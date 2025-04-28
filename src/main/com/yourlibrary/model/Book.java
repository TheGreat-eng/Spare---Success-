package main.com.yourlibrary.model;

import java.sql.Timestamp;

public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private String description;
    private int quantity;
    private int availableQuantity;
    private Timestamp createdAt; // Optional
    private Timestamp updatedAt; // Optional

    // Constructors
    public Book() {
    }

    public Book(int bookId, String isbn, String title, String author, String genre, int publicationYear,
            String description, int quantity, int availableQuantity) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.description = description;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }

    // Getters and Setters cho tất cả các thuộc tính
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() { // Hữu ích cho debugging
        return "Book{" + "bookId=" + bookId + ", title='" + title + '\'' + ", author='" + author + '\'' + '}';
    }
}