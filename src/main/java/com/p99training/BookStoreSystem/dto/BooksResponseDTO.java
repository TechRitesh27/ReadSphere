package com.p99training.BookStoreSystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BooksResponseDTO {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private double price;
    private String publisher;
    private int quantity;
    private int publishedYear;
    private String language;
}
