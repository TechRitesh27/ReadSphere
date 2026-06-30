package com.p99training.BookStoreSystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.enums.BookSortField;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.PagedResponseDTO;
import com.p99training.BookStoreSystem.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "CRUD operations for books — filter, paginate, sort")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
        summary = "Get all books",
        description = "Returns a paginated, sorted, and optionally filtered list of books."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Books returned successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDTO<BooksResponseDTO>> getAllBooks(
            @Parameter(description = "Filter by category (e.g. Programming, Fiction)")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by language (e.g. English, Hindi)")
            @RequestParam(required = false) String language,

            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) Double maxPrice,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", schema = @Schema(implementation = BookSortField.class))
            @RequestParam(defaultValue = "TITLE") BookSortField sortBy,

            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /books - page={}, size={}, sortBy={}, sortDir={}, category={}, language={}, minPrice={}, maxPrice={}",
                page, size, sortBy, sortDir, category, language, minPrice, maxPrice);

        PagedResponseDTO<BooksResponseDTO> result =
                bookService.getAllBooks(category, language, minPrice, maxPrice, page, size, sortBy, sortDir);

        log.info("GET /books - page {}/{}, {} item(s) returned",
                page, result.getTotalPages(), result.getData().size());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get book by ID", description = "Returns a single book by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book found"),
        @ApiResponse(responseCode = "404", description = "Book not found",
                content = @Content(schema = @Schema(example = "{\"error\": \"Book not found with id: 99\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> getBookById(
            @Parameter(description = "ID of the book", example = "1")
            @PathVariable int id) {

        log.info("GET /books/{} - fetching by id", id);
        BooksResponseDTO result = bookService.getBookById(id);
        log.info("GET /books/{} - found book: {}", id, result.getTitle());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Add a new book", description = "Creates a new book entry in the store.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
                content = @Content(schema = @Schema(example = "{\"error\": \"Validation failed\", \"details\": [\"title: Title is required\"]}")))
    })
    @PostMapping
    public ResponseEntity<BooksResponseDTO> addBook(
            @Valid @RequestBody BookRequestDTO request) {

        log.info("POST /books - adding new book: title={}, author={}", request.getTitle(), request.getAuthor());
        BooksResponseDTO created = bookService.addBook(request);
        log.info("POST /books - book added successfully: id={}, title={}", created.getId(), created.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a book", description = "Updates all fields of an existing book.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book updated successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BooksResponseDTO> updateBook(
            @Parameter(description = "ID of the book to update", example = "1")
            @PathVariable int id,
            @Valid @RequestBody BookRequestDTO request) {

        log.info("PUT /books/{} - updating book", id);
        BooksResponseDTO updated = bookService.updateBook(id, request);
        log.info("PUT /books/{} - book updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Delete a book",
        description = "Deletes a book by ID. **Requires admin credentials** (HTTP Basic Auth)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "401", description = "No credentials provided"),
        @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @SecurityRequirement(name = "basicAuth")   // shows the 🔒 lock icon on this endpoint only
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(
            @Parameter(description = "ID of the book to delete", example = "1")
            @PathVariable int id) {

        log.info("DELETE /books/{} - deleting book", id);
        bookService.deleteBook(id);
        log.info("DELETE /books/{} - book deleted successfully", id);
        return ResponseEntity.ok("Book with id " + id + " deleted successfully");
    }
}
