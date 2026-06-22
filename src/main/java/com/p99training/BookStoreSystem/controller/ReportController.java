package com.p99training.BookStoreSystem.controller;

import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportDTO> getInventoryReport() {
        log.info("GET /reports/inventory - generating inventory report");
        InventoryReportDTO report = reportService.generateInventoryReport();
        log.info("GET /reports/inventory - report generated successfully");
        return ResponseEntity.ok(report);
    }
}
