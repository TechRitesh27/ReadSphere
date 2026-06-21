package com.p99training.BookStoreSystem.service.impl;

import com.opencsv.CSVReader;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadCsvServiceImpl implements ReadCsvService {

    private static final Logger logger = LoggerFactory.getLogger(ReadCsvServiceImpl.class);

    // -------------------------------------------------------
    // Returns DTO list (used by ReportService)
    // -------------------------------------------------------
    @Override
    public List<BooksResponseDTO> readBooks() {
        logger.info("Reading books from CSV as DTOs...");

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

        logger.info("Successfully read {} books from CSV", books.size());

        return books;
    }

    // -------------------------------------------------------
    // Returns Entity list (used by BookService to seed store)
    // -------------------------------------------------------
    @Override
    public List<Book> readBooksAsEntities() {
        logger.info("Reading books from CSV as entities...");

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

        logger.info("Successfully read {} book entities from CSV", books.size());

        return books;
    }

    // -------------------------------------------------------
    // Shared: reads CSV and returns raw row list
    // -------------------------------------------------------
    private List<String[]> loadRawRows() {
        logger.debug("Loading raw rows from books_catalog.csv");

        InputStream inputStream = getClass().getResourceAsStream("/books_catalog.csv");

        if (inputStream == null) {
            logger.error("CSV file not found in resources: books_catalog.csv");
            throw new RuntimeException("CSV file not found");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                logger.error("CSV file is empty");
                throw new RuntimeException("CSV file is empty");
            }
            if (rows.size() == 1) {
                logger.error("CSV file contains only header row, no data found");
                throw new RuntimeException("CSV file contains only header row");
            }

            logger.debug("Loaded {} data rows from CSV (excluding header)", rows.size() - 1);

            return rows;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while reading CSV file: {}", e.getMessage());
            throw new RuntimeException("Unexpected error while reading CSV file", e);
        }
    }

    private void validateRow(String[] data, int rowIndex) {
        if (data.length < 10) {
            logger.warn("Invalid data at row {} - expected 10 columns, found {}", rowIndex, data.length);
            throw new RuntimeException("Invalid data at row: " + rowIndex);
        }
    }
}
