package com.p99training.BookStoreSystem.controller;

import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/inventory")
    public InventoryReportDTO getInventoryReport() {

        return reportService.generateInventoryReport();
    }
}
