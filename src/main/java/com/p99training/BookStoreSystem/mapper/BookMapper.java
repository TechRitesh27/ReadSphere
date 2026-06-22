package com.p99training.BookStoreSystem.mapper;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import org.springframework.stereotype.Component;

/**
 * Maps between raw CSV rows, Book entities, and BooksResponseDTO.
 * Single place to change if CSV structure or field names ever shift.
 */
@Component
public class BookMapper {

    // CSV column positions
    private static final int ID            = 0;
    private static final int TITLE         = 1;
    private static final int AUTHOR        = 2;
    private static final int CATEGORY      = 3;
    private static final int PUBLISHER     = 4;
    private static final int PRICE         = 5;
    private static final int QUANTITY      = 6;
    private static final int PUBLISHED_YEAR = 7;
    private static final int ISBN          = 8;
    private static final int LANGUAGE      = 9;

    public Book rowToEntity(String[] data) {
        return Book.builder()
                .id(Integer.parseInt(data[ID]))
                .title(data[TITLE])
                .author(data[AUTHOR])
                .category(data[CATEGORY])
                .publisher(data[PUBLISHER])
                .price(Double.parseDouble(data[PRICE]))
                .quantity(Integer.parseInt(data[QUANTITY]))
                .publishedYear(Integer.parseInt(data[PUBLISHED_YEAR]))
                .isbn(data[ISBN])
                .language(data[LANGUAGE])
                .build();
    }

    public BooksResponseDTO rowToDto(String[] data) {
        return BooksResponseDTO.builder()
                .id(Integer.parseInt(data[ID]))
                .title(data[TITLE])
                .author(data[AUTHOR])
                .category(data[CATEGORY])
                .publisher(data[PUBLISHER])
                .price(Double.parseDouble(data[PRICE]))
                .quantity(Integer.parseInt(data[QUANTITY]))
                .publishedYear(Integer.parseInt(data[PUBLISHED_YEAR]))
                .isbn(data[ISBN])
                .language(data[LANGUAGE])
                .build();
    }

    public BooksResponseDTO entityToDto(Book book) {
        return BooksResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .price(book.getPrice())
                .publisher(book.getPublisher())
                .quantity(book.getQuantity())
                .publishedYear(book.getPublishedYear())
                .language(book.getLanguage())
                .build();
    }
}
