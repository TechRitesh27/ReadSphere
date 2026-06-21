package com.p99training.BookStoreSystem.service.impl;

import com.opencsv.CSVReader;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadCsvServiceImpl implements ReadCsvService {

    @Override
    public List<BooksResponseDTO> readBooks() {

        List<BooksResponseDTO> books = new ArrayList<>();

        try {

            // Check file exists
            InputStream inputStream =
                    getClass().getResourceAsStream("/books_catalog.csv");

            if (inputStream == null) {
                throw new RuntimeException("CSV file not found");
            }

            CSVReader reader =
                    new CSVReader(new InputStreamReader(inputStream));

            List<String[]> rows = reader.readAll();

            // Check empty file
            if (rows.isEmpty()) {
                throw new RuntimeException("CSV file is empty");
            }

            // Check only header exists
            if (rows.size() == 1) {
                throw new RuntimeException(
                        "CSV file contains only header row"
                );
            }

            // Skip Header Row
            for (int i = 1; i < rows.size(); i++) {

                String[] data = rows.get(i);

                // Validate column count
                if (data.length < 10) {
                    throw new RuntimeException(
                            "Invalid data at row: " + i
                    );
                }

                BooksResponseDTO dto =
                        new BooksResponseDTO();

                dto.setTitle(data[1]);
                dto.setAuthor(data[2]);
                dto.setCategory(data[3]);
                dto.setPublisher(data[4]);
                dto.setPrice(Double.parseDouble(data[5]));
                dto.setQuantity(Integer.parseInt(data[6]));
                dto.setPublishedYear(
                        Integer.parseInt(data[7])
                );
                dto.setIsbn(data[8]);
                dto.setLanguage(data[9]);

                books.add(dto);
            }

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Invalid number format in CSV file",
                    e
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            // Handle unexpected exceptions
            throw new RuntimeException(
                    "Unexpected error while reading CSV file",
                    e
            );
        }

        return books;
    }
}