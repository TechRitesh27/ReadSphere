package com.p99training.BookStoreSystem.service.impl;

import com.opencsv.CSVReader;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadCsvServiceImpl implements ReadCsvService {

    // Returns DTO list (used by ReportService)
    @Override
    public List<BooksResponseDTO> readBooks() {
        List<String[]> rows = loadRawRows();
        List<BooksResponseDTO> books = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] data = rows.get(i);
            validateRow(data, i);

            BooksResponseDTO dto = new BooksResponseDTO();
            dto.setTitle(data[1]);
            dto.setAuthor(data[2]);
            dto.setCategory(data[3]);
            dto.setPublisher(data[4]);
            dto.setPrice(Double.parseDouble(data[5]));
            dto.setQuantity(Integer.parseInt(data[6]));
            dto.setPublishedYear(Integer.parseInt(data[7]));
            dto.setIsbn(data[8]);
            dto.setLanguage(data[9]);

            books.add(dto);
        }

        return books;
    }

    // Returns Entity list (used by BookService to seed store)
    @Override
    public List<Book> readBooksAsEntities() {
        List<String[]> rows = loadRawRows();
        List<Book> books = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] data = rows.get(i);
            validateRow(data, i);

            Book book = new Book();
            book.setId(Integer.parseInt(data[0]));
            book.setTitle(data[1]);
            book.setAuthor(data[2]);
            book.setCategory(data[3]);
            book.setPublisher(data[4]);
            book.setPrice(Double.parseDouble(data[5]));
            book.setQuantity(Integer.parseInt(data[6]));
            book.setPublishedYear(Integer.parseInt(data[7]));
            book.setIsbn(data[8]);
            book.setLanguage(data[9]);

            books.add(book);
        }

        return books;
    }

    // Shared: reads CSV and returns raw row list

    private List<String[]> loadRawRows() {
        InputStream inputStream = getClass().getResourceAsStream("/books_catalog.csv");

        if (inputStream == null) {
            throw new RuntimeException("CSV file not found");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                throw new RuntimeException("CSV file is empty");
            }
            if (rows.size() == 1) {
                throw new RuntimeException("CSV file contains only header row");
            }

            return rows;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while reading CSV file", e);
        }
    }

    private void validateRow(String[] data, int rowIndex) {
        if (data.length < 10) {
            throw new RuntimeException("Invalid data at row: " + rowIndex);
        }
    }
}
