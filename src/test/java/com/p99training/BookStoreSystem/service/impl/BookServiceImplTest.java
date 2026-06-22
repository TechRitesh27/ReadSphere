package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BookRequestDTO;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.exception.BookNotFoundException;
import com.p99training.BookStoreSystem.mapper.BookMapper;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private ReadCsvService readCsvService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    // sample data reused across all tests
    private Book book;
    private BooksResponseDTO response;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1).title("Clean Code").author("Robert Martin")
                .isbn("123").category("Programming").price(45.0)
                .publisher("Prentice Hall").quantity(10)
                .publishedYear(2008).language("English")
                .build();

        response = BooksResponseDTO.builder()
                .id(1).title("Clean Code").author("Robert Martin")
                .build();

        // seed the in-memory store with our sample book
        when(readCsvService.readBooksAsEntities()).thenReturn(List.of(book));
        bookService.init();
    }

    @Test
    void getAll_returnsAllBooks() {
        when(bookMapper.entityToDto(book)).thenReturn(response);

        List<BooksResponseDTO> result = bookService.getAllBooks(null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void getById_existingId_returnsBook() {
        when(bookMapper.entityToDto(book)).thenReturn(response);

        BooksResponseDTO result = bookService.getBookById(1);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_wrongId_throwsException() {
        assertThatThrownBy(() -> bookService.getBookById(99))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void addBook_savesAndReturnsBook() {
        when(bookMapper.entityToDto(any(Book.class))).thenReturn(response);

        BooksResponseDTO result = bookService.addBook(buildRequest());

        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void updateBook_wrongId_throwsException() {
        assertThatThrownBy(() -> bookService.updateBook(99, buildRequest()))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void deleteBook_existingId_removesBook() {
        bookService.deleteBook(1);

        // store should be empty now
        assertThat(bookService.getAllBooks(null, null, null, null)).isEmpty();
    }

    @Test
    void deleteBook_wrongId_throwsException() {
        assertThatThrownBy(() -> bookService.deleteBook(99))
                .isInstanceOf(BookNotFoundException.class);
    }

    // builds a minimal request — only what the service needs
    private BookRequestDTO buildRequest() {
        BookRequestDTO req = new BookRequestDTO();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        req.setIsbn("123");
        req.setCategory("Programming");
        req.setPrice(45.0);
        req.setPublisher("Prentice Hall");
        req.setQuantity(10);
        req.setPublishedYear(2008);
        req.setLanguage("English");
        return req;
    }
}
