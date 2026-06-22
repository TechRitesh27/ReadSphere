package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.exception.BookNotFoundException;
import com.p99training.BookStoreSystem.mapper.BookMapper;
import com.p99training.BookStoreSystem.service.BookService;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final ReadCsvService readCsvService;
    private final BookMapper bookMapper;

    // Thread-safe in-memory store — seeded from CSV at startup
    private final List<Book> bookStore = new CopyOnWriteArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger();

    public BookServiceImpl(ReadCsvService readCsvService, BookMapper bookMapper) {
        this.readCsvService = readCsvService;
        this.bookMapper = bookMapper;
    }

    // -------------------------------------------------
    // Seed the store once when the app starts
    // -------------------------------------------------
    @PostConstruct
    public void init() {
        log.info("Initializing book store from CSV...");

        List<Book> books = readCsvService.readBooksAsEntities();
        bookStore.addAll(books);

        int maxId = books.stream().mapToInt(Book::getId).max().orElse(0);
        idCounter.set(maxId + 1);

        log.info("Book store initialized with {} books. Next ID starts at {}",
                bookStore.size(), idCounter.get());
    }

    // -------------------------------------------------
    // READ ALL — with optional Stream filtering
    // -------------------------------------------------
    @Override
    public List<BooksResponseDTO> getAllBooks(
            String category,
            String language,
            Double minPrice,
            Double maxPrice) {

        log.debug("Fetching all books with filters - category={}, language={}, minPrice={}, maxPrice={}",
                category, language, minPrice, maxPrice);

        List<BooksResponseDTO> result = bookStore.stream()
                .filter(b -> category == null || b.getCategory().equalsIgnoreCase(category))
                .filter(b -> language == null || b.getLanguage().equalsIgnoreCase(language))
                .filter(b -> minPrice == null || b.getPrice() >= minPrice)
                .filter(b -> maxPrice == null || b.getPrice() <= maxPrice)
                .map(bookMapper::entityToDto)
                .collect(Collectors.toList());

        log.debug("getAllBooks - found {} book(s) matching filters", result.size());

        return result;
    }

    // -------------------------------------------------
    // READ ONE by ID
    // -------------------------------------------------
    @Override
    public BooksResponseDTO getBookById(int id) {
        log.debug("Fetching book with id={}", id);

        return bookStore.stream()
                .filter(b -> b.getId() == id)
                .map(bookMapper::entityToDto)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Book not found with id={}", id);
                    return new BookNotFoundException(id);
                });
    }

    // -------------------------------------------------
    // CREATE
    // -------------------------------------------------
    @Override
    public BooksResponseDTO addBook(BookRequestDTO request) {
        Book book = Book.builder()
                .id(idCounter.getAndIncrement())
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .price(request.getPrice())
                .publisher(request.getPublisher())
                .quantity(request.getQuantity())
                .publishedYear(request.getPublishedYear())
                .language(request.getLanguage())
                .build();

        bookStore.add(book);

        log.info("Book added - id={}, title={}, author={}", book.getId(), book.getTitle(), book.getAuthor());

        return bookMapper.entityToDto(book);
    }

    // -------------------------------------------------
    // UPDATE — fetch existing, rebuild with new values
    // -------------------------------------------------
    @Override
    public BooksResponseDTO updateBook(int id, BookRequestDTO request) {
        log.debug("Updating book with id={}", id);

        Book existing = bookStore.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Update failed - book not found with id={}", id);
                    return new BookNotFoundException(id);
                });

        // Rebuild with same id, all fields from request
        Book updated = Book.builder()
                .id(existing.getId())
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .price(request.getPrice())
                .publisher(request.getPublisher())
                .quantity(request.getQuantity())
                .publishedYear(request.getPublishedYear())
                .language(request.getLanguage())
                .build();

        bookStore.replaceAll(b -> b.getId() == id ? updated : b);

        log.info("Book updated - id={}, new title={}", id, updated.getTitle());

        return bookMapper.entityToDto(updated);
    }

    // -------------------------------------------------
    // DELETE
    // -------------------------------------------------
    @Override
    public void deleteBook(int id) {
        log.debug("Deleting book with id={}", id);

        boolean removed = bookStore.removeIf(b -> b.getId() == id);

        if (!removed) {
            log.warn("Delete failed - book not found with id={}", id);
            throw new BookNotFoundException(id);
        }

        log.info("Book deleted - id={}", id);
    }
}
