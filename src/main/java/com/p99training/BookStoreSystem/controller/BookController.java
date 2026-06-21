package com.p99training.BookStoreSystem.controller;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /books
    // Optional query params: category, language, minPrice, maxPrice

    @GetMapping
    public ResponseEntity<List<BooksResponseDTO>> getAllBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        return ResponseEntity.ok(
                bookService.getAllBooks(category, language, minPrice, maxPrice)
        );
    }

    // GET /books/{id}

    @GetMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> getBookById(@PathVariable int id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }


    // POST /books
    @PostMapping
    public ResponseEntity<BooksResponseDTO> addBook(@RequestBody BookRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.addBook(request));
    }

    // PUT /books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> updateBook(
            @PathVariable int id,
            @RequestBody BookRequestDTO request) {

        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    // DELETE /books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book with id " + id + " deleted successfully");
    }
}
