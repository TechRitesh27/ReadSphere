package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.service.BookService;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final ReadCsvService readCsvService;

    // In-memory store — seeded from CSV at startup
    private final List<Book> bookStore = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger();

    public BookServiceImpl(ReadCsvService readCsvService) {
        this.readCsvService = readCsvService;
    }

    // Seed the store once when the app starts
    @PostConstruct
    public void init() {
        List<Book> books = readCsvService.readBooksAsEntities();
        bookStore.addAll(books);

        // Start ID counter after the last CSV id
        int maxId = books.stream().mapToInt(Book::getId).max().orElse(0);
        idCounter.set(maxId + 1);
    }

    // READ ALL — with optional Stream filtering
    @Override
    public List<BooksResponseDTO> getAllBooks(
            String category,
            String language,
            Double minPrice,
            Double maxPrice) {

        return bookStore.stream()
                .filter(b -> category == null || b.getCategory().equalsIgnoreCase(category))
                .filter(b -> language == null || b.getLanguage().equalsIgnoreCase(language))
                .filter(b -> minPrice == null || b.getPrice() >= minPrice)
                .filter(b -> maxPrice == null || b.getPrice() <= maxPrice)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ ONE by ID
    @Override
    public BooksResponseDTO getBookById(int id) {
        return bookStore.stream()
                .filter(b -> b.getId() == id)
                .map(this::toResponseDTO)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    // CREATE

    @Override
    public BooksResponseDTO addBook(BookRequestDTO request) {
        Book book = new Book();
        book.setId(idCounter.getAndIncrement());
        mapRequestToBook(request, book);
        bookStore.add(book);
        return toResponseDTO(book);
    }

    // UPDATE
    @Override
    public BooksResponseDTO updateBook(int id, BookRequestDTO request) {
        Book book = bookStore.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        mapRequestToBook(request, book);
        return toResponseDTO(book);
    }

    // DELETE
    @Override
    public void deleteBook(int id) {
        boolean removed = bookStore.removeIf(b -> b.getId() == id);
        if (!removed) {
            throw new RuntimeException("Book not found with id: " + id);
        }
    }

    // Helpers
    private void mapRequestToBook(BookRequestDTO request, Book book) {
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setPrice(request.getPrice());
        book.setPublisher(request.getPublisher());
        book.setQuantity(request.getQuantity());
        book.setPublishedYear(request.getPublishedYear());
        book.setLanguage(request.getLanguage());
    }

    private BooksResponseDTO toResponseDTO(Book book) {
        BooksResponseDTO dto = new BooksResponseDTO();
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setPrice(book.getPrice());
        dto.setPublisher(book.getPublisher());
        dto.setQuantity(book.getQuantity());
        dto.setPublishedYear(book.getPublishedYear());
        dto.setLanguage(book.getLanguage());
        return dto;
    }
}