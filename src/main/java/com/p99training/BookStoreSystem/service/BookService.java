package com.p99training.BookStoreSystem.service;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;

import java.util.List;

public interface BookService {

    // READ
    List<BooksResponseDTO> getAllBooks(String category, String language, Double minPrice, Double maxPrice);

    BooksResponseDTO getBookById(int id);

    // CREATE
    BooksResponseDTO addBook(BookRequestDTO request);

    // UPDATE
    BooksResponseDTO updateBook(int id, BookRequestDTO request);

    // DELETE
    void deleteBook(int id);
}