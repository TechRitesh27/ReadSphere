package com.p99training.BookStoreSystem.service;

import com.opencsv.CSVReader;
import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.entity.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



@Service
public interface ReadCsvService {
    List<BooksResponseDTO> readBooks();
}
