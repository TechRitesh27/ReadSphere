package com.p99training.BookStoreSystem.service;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;

import java.util.List;

public interface ReadCsvService {

    List<BooksResponseDTO> readBooks();

    List<Book> readBooksAsEntities();
}
