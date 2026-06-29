package com.p99training.BookStoreSystem.service;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.enums.BookSortField;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.PagedResponseDTO;

import java.util.List;

public interface BookService {

    // READ — paginated + sorted + filtered
    PagedResponseDTO<BooksResponseDTO> getAllBooks(
            String category,
            String language,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            BookSortField sortBy,
            String sortDir
    );

    // Internal use (ReportService needs the full list without pagination)
    List<BooksResponseDTO> getAllBooks(String category, String language, Double minPrice, Double maxPrice);

    BooksResponseDTO getBookById(int id);

    // CREATE
    BooksResponseDTO addBook(BookRequestDTO request);

    // UPDATE
    BooksResponseDTO updateBook(int id, BookRequestDTO request);

    // DELETE
    void deleteBook(int id);
}
