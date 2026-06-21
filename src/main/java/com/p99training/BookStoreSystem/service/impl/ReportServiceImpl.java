package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.ReadCsvService;
import com.p99training.BookStoreSystem.service.ReportService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        int totalBooks = books.size();

        int totalQuantity = 0;

        double totalInventoryValue = 0;

        Map<String, Integer> categoryWiseBookCount = new HashMap<>();

        Map<String, Integer> languageWiseReport = new HashMap<>();

        Map<String, Integer> publisherWiseReport = new HashMap<>();

        Map<Integer, Integer> yearWisePublishedBooks = new HashMap<>();

        for (BooksResponseDTO book : books) {

            // Total Quantity
            totalQuantity += book.getQuantity();

            // Total Inventory Value
            totalInventoryValue +=
                    (book.getPrice() * book.getQuantity());

            // Category Wise Count
            categoryWiseBookCount.put(
                    book.getCategory(),
                    categoryWiseBookCount.getOrDefault(
                            book.getCategory(), 0
                    ) + 1
            );

            // Language Wise Report
            languageWiseReport.put(
                    book.getLanguage(),
                    languageWiseReport.getOrDefault(
                            book.getLanguage(), 0
                    ) + 1
            );

            // Publisher Wise Report
            publisherWiseReport.put(
                    book.getPublisher(),
                    publisherWiseReport.getOrDefault(
                            book.getPublisher(), 0
                    ) + 1
            );

            // Year Wise Published Books
            yearWisePublishedBooks.put(
                    book.getPublishedYear(),
                    yearWisePublishedBooks.getOrDefault(
                            book.getPublishedYear(), 0
                    ) + 1
            );
        }

        report.setTotalBooks(totalBooks);
        report.setTotalQuantity(totalQuantity);
        report.setTotalInventoryValue(totalInventoryValue);

        report.setCategoryWiseBookCount(categoryWiseBookCount);

        report.setLanguageWiseReport(languageWiseReport);

        report.setPublisherWiseReport(publisherWiseReport);

        report.setYearWisePublishedBooks(yearWisePublishedBooks);

        return report;
    }
}