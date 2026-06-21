package com.p99training.BookStoreSystem.controller;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookController {

    @Autowired
    private ReadCsvService readcsvservice;

    @GetMapping("/books")
    public List<BooksResponseDTO> getBooks() {
        return readcsvservice.readBooks();
    }
}
