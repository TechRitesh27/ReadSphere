package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.enums.BookSortField;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.PagedResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.exception.BookNotFoundException;
import com.p99training.BookStoreSystem.mapper.BookMapper;
import com.p99training.BookStoreSystem.service.BookService;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    // READ ALL — paginated + sorted + filtered
    // -------------------------------------------------
    @Override
    public PagedResponseDTO<BooksResponseDTO> getAllBooks(
            String category,
            String language,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            BookSortField sortBy,
            String sortDir) {

        log.debug("getAllBooks - page={}, size={}, sortBy={}, sortDir={}, category={}, language={}, minPrice={}, maxPrice={}",
                page, size, sortBy, sortDir, category, language, minPrice, maxPrice);

        // Step 1: filter
        List<BooksResponseDTO> filtered = bookStore.stream()
                .filter(b -> category == null || b.getCategory().equalsIgnoreCase(category))
                .filter(b -> language == null || b.getLanguage().equalsIgnoreCase(language))
                .filter(b -> minPrice == null || b.getPrice() >= minPrice)
                .filter(b -> maxPrice == null || b.getPrice() <= maxPrice)
                .map(bookMapper::entityToDto)
                .collect(Collectors.toList());

        long totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Step 2: sort
        Comparator<BooksResponseDTO> comparator = resolveComparator(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        // Step 3: paginate — skip pages before current, take 'size' items
        List<BooksResponseDTO> pageData = filtered.stream()
                .sorted(comparator)
                .skip((long) page * size)
                .limit(size)
                .toList();

        log.debug("getAllBooks - totalItems={}, totalPages={}, returning {} item(s) for page {}",
                totalItems, totalPages, pageData.size(), page);

        return PagedResponseDTO.<BooksResponseDTO>builder()
                .data(pageData)
                .page(page)
                .size(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build();
    }

    // -------------------------------------------------
    // READ ALL — flat list (used internally by ReportService)
    // -------------------------------------------------
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
                .map(bookMapper::entityToDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------
    // READ ONE by ID — result cached per id
    // -------------------------------------------------
    @Override
    @Cacheable(value = "books", key = "#id")
    public BooksResponseDTO getBookById(int id) {
        log.debug("Fetching book with id={} (cache miss — hitting store)", id);

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
    // CREATE — evict all cached books and report on any write
    // -------------------------------------------------
    @Override
    @Caching(evict = {
        @CacheEvict(value = "books", allEntries = true),
        @CacheEvict(value = "inventoryReport", allEntries = true)
    })
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
    // UPDATE — evict the specific id entry + report
    // -------------------------------------------------
    @Override
    @Caching(evict = {
        @CacheEvict(value = "books", key = "#id"),
        @CacheEvict(value = "inventoryReport", allEntries = true)
    })
    public BooksResponseDTO updateBook(int id, BookRequestDTO request) {
        log.debug("Updating book with id={}", id);

        Book existing = bookStore.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Update failed - book not found with id={}", id);
                    return new BookNotFoundException(id);
                });

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
    // DELETE — evict the specific id entry + report
    // -------------------------------------------------
    @Override
    @Caching(evict = {
        @CacheEvict(value = "books", key = "#id"),
        @CacheEvict(value = "inventoryReport", allEntries = true)
    })
    public void deleteBook(int id) {
        log.debug("Deleting book with id={}", id);

        boolean removed = bookStore.removeIf(b -> b.getId() == id);

        if (!removed) {
            log.warn("Delete failed - book not found with id={}", id);
            throw new BookNotFoundException(id);
        }

        log.info("Book deleted - id={}", id);
    }

    // -------------------------------------------------
    // Resolve comparator from sort field enum
    // -------------------------------------------------
    private Comparator<BooksResponseDTO> resolveComparator(BookSortField sortBy) {
        return switch (sortBy) {
            case TITLE         -> Comparator.comparing(BooksResponseDTO::getTitle, String.CASE_INSENSITIVE_ORDER);
            case AUTHOR        -> Comparator.comparing(BooksResponseDTO::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case PRICE         -> Comparator.comparingDouble(BooksResponseDTO::getPrice);
            case PUBLISHED_YEAR -> Comparator.comparingInt(BooksResponseDTO::getPublishedYear);
            case QUANTITY      -> Comparator.comparingInt(BooksResponseDTO::getQuantity);
            case ID            -> Comparator.comparingInt(BooksResponseDTO::getId);
        };
    }
}
