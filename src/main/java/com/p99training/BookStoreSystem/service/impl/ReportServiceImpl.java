package com.p99training.BookStoreSystem.service.impl;

import com.p99training.BookStoreSystem.dto.BooksResponseDTO;
import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.BookService;
import com.p99training.BookStoreSystem.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final BookService bookService;

    public ReportServiceImpl(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    @Cacheable(value = "inventoryReport")  // cached — only recomputed when evicted by a write
    public InventoryReportDTO generateInventoryReport() {
        log.info("Generating inventory report (cache miss — recomputing)...");

        List<BooksResponseDTO> books = bookService.getAllBooks(null, null, null, null);

        InventoryReportDTO report = InventoryReportDTO.builder()
                .totalBooks(books.size())
                .totalQuantity(
                        books.stream().mapToInt(BooksResponseDTO::getQuantity).sum()
                )
                .totalInventoryValue(
                        books.stream().mapToDouble(b -> b.getPrice() * b.getQuantity()).sum()
                )
                .categoryWiseBookCount(
                        books.stream().collect(Collectors.groupingBy(
                                BooksResponseDTO::getCategory, Collectors.summingInt(b -> 1)))
                )
                .languageWiseReport(
                        books.stream().collect(Collectors.groupingBy(
                                BooksResponseDTO::getLanguage, Collectors.summingInt(b -> 1)))
                )
                .publisherWiseReport(
                        books.stream().collect(Collectors.groupingBy(
                                BooksResponseDTO::getPublisher, Collectors.summingInt(b -> 1)))
                )
                .yearWisePublishedBooks(
                        books.stream().collect(Collectors.groupingBy(
                                BooksResponseDTO::getPublishedYear, Collectors.summingInt(b -> 1)))
                )
                .build();

        log.info("Inventory report generated - totalBooks={}, totalQuantity={}, totalValue={}",
                report.getTotalBooks(), report.getTotalQuantity(), report.getTotalInventoryValue());

        return report;
    }
}
