package com.p99training.BookStoreSystem.service.impl;

import com.opencsv.CSVReader;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.mapper.BookMapper;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
public class ReadCsvServiceImpl implements ReadCsvService {

    private final BookMapper bookMapper;

    @Value("${csv.file.path}")
    private String csvFilePath;

    public ReadCsvServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    // Returns DTO list
    @Override
    public List<BooksResponseDTO> readBooks() {
        log.info("Reading books from CSV as DTOs...");
        List<String[]> rows = loadRawRows();
        List<BooksResponseDTO> books = rows.stream()
                .map(bookMapper::rowToDto)
                .toList();
        log.info("Successfully read {} books from CSV", books.size());
        return books;
    }
    // Returns Entity list (used by BookService to seed store)
    @Override
    public List<Book> readBooksAsEntities() {
        log.info("Reading books from CSV as entities...");
        List<String[]> rows = loadRawRows();
        List<Book> books = rows.stream()
                .map(bookMapper::rowToEntity)
                .toList();
        log.info("Successfully read {} book entities from CSV", books.size());
        return books;
    }

    // -------------------------------------------------------
    // Loads CSV, skips header, validates each row
    // -------------------------------------------------------
    private List<String[]> loadRawRows() {
        log.debug("Loading raw rows from file");

        InputStream inputStream = getClass().getResourceAsStream("/" + csvFilePath);
        if (inputStream == null) {
            log.error("CSV file not found in resources");
            throw new RuntimeException("CSV file not found");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                throw new RuntimeException("CSV file is empty");
            }
            if (allRows.size() == 1) {
                throw new RuntimeException("CSV file contains only header row");
            }

            // Skip header row, validate data rows
            List<String[]> dataRows = allRows.subList(1, allRows.size());
            for (int i = 0; i < dataRows.size(); i++) {
                validateRow(dataRows.get(i), i + 1);
            }

            log.debug("Loaded {} data rows from CSV (excluding header)", dataRows.size());
            return dataRows;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while reading CSV file: {}", e.getMessage());
            throw new RuntimeException("Unexpected error while reading CSV file", e);
        }
    }

    private void validateRow(String[] data, int rowIndex) {
        if (data.length < 10) {
            log.warn("Invalid data at row {} - expected 10 columns, found {}", rowIndex, data.length);
            throw new RuntimeException("Invalid data at row: " + rowIndex);
        }
    }
}
