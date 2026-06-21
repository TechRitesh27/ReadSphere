package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import com.p99training.BookStoreSystem.service.ReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReadCsvService readCsvService;

    public ReportServiceImpl(ReadCsvService readCsvService) {
        this.readCsvService = readCsvService;
    }

    @Override
    public InventoryReportDTO generateInventoryReport() {

        List<BooksResponseDTO> books = readCsvService.readBooks();

        InventoryReportDTO report = new InventoryReportDTO();

        // Basic aggregations using Streams
        report.setTotalBooks(books.size());

        report.setTotalQuantity(
                books.stream()
                        .mapToInt(BooksResponseDTO::getQuantity)
                        .sum()
        );

        report.setTotalInventoryValue(
                books.stream()
                        .mapToDouble(b -> b.getPrice() * b.getQuantity())
                        .sum()
        );

        // Grouping by Category
        report.setCategoryWiseBookCount(
                books.stream()
                        .collect(Collectors.groupingBy(
                                BooksResponseDTO::getCategory,
                                Collectors.summingInt(b -> 1)
                        ))
        );

        // Grouping by Language
        report.setLanguageWiseReport(
                books.stream()
                        .collect(Collectors.groupingBy(
                                BooksResponseDTO::getLanguage,
                                Collectors.summingInt(b -> 1)
                        ))
        );

        // Grouping by Publisher
        report.setPublisherWiseReport(
                books.stream()
                        .collect(Collectors.groupingBy(
                                BooksResponseDTO::getPublisher,
                                Collectors.summingInt(b -> 1)
                        ))
        );

        // Grouping by Published Year
        report.setYearWisePublishedBooks(
                books.stream()
                        .collect(Collectors.groupingBy(
                                BooksResponseDTO::getPublishedYear,
                                Collectors.summingInt(b -> 1)
                        ))
        );

        return report;
    }
}
