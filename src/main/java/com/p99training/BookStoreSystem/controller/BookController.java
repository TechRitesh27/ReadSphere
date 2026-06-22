package com.p99training.BookStoreSystem.controller;

import jakarta.validation.Valid;
import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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

        log.info("GET /books - filters: category={}, language={}, minPrice={}, maxPrice={}",
                category, language, minPrice, maxPrice);

        List<BooksResponseDTO> result = bookService.getAllBooks(category, language, minPrice, maxPrice);

        log.info("GET /books - returning {} book(s)" , result.size());

        return ResponseEntity.ok(result);
    }

    // GET /books/{id}

    @GetMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> getBookById(@PathVariable int id) {

        log.info("GET /books/{} - fetching by id", id);

        BooksResponseDTO result = bookService.getBookById(id);

        log.info("GET /books/{} - found book: {}", id, result.getTitle());

        return ResponseEntity.ok(result);
    }


    // POST /books
    @PostMapping
    public ResponseEntity<BooksResponseDTO> addBook(@Valid @RequestBody BookRequestDTO request) {

        log.info("POST /books - adding new book : title={}, author={}", request.getTitle(), request.getAuthor());

        BooksResponseDTO created = bookService.addBook(request);

        log.info("POST /books book added successfully: title={}", created.getTitle());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // PUT /books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> updateBook(
            @PathVariable int id,
            @Valid @RequestBody BookRequestDTO request) {

        log.info("PUT /books/{} - updating book with title={}", id, request.getTitle());

        BooksResponseDTO updated = bookService.updateBook(id, request);

        log.info("PUT /books/{} - book updated successfully", id);

        return ResponseEntity.ok(updated);
    }

    // DELETE /books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {

        log.info("DELETE /books/{} - deleting book", id);
        bookService.deleteBook(id);
        log.info("DELETE /books/{} - book deleted successfully", id);
        return ResponseEntity.ok("Book with id " + id + " deleted successfully");
    }
}
