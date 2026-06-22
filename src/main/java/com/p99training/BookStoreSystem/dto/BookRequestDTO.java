package com.p99training.BookStoreSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class BookRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Category is required")
    private String category;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @NotBlank(message = "Publisher is required")
    private String publisher;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    @Min(value = 1000, message = "Published year seems invalid")
    private int publishedYear;

    @NotBlank(message = "Language is required")
    private String language;
}
