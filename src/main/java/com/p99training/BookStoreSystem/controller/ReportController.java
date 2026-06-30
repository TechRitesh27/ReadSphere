package com.p99training.BookStoreSystem.controller;

import com.p99training.BookStoreSystem.dto.InventoryReportDTO;
import com.p99training.BookStoreSystem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Inventory analytics and reporting")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
        summary = "Get inventory report",
        description = """
                Returns a full inventory summary including:
                - Total books and total quantity
                - Total inventory value (price × quantity)
                - Breakdown by category, language, publisher, and published year
                
                Result is cached — only recomputed when books are added, updated, or deleted.
                """
    )
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportDTO> getInventoryReport() {
        log.info("GET /reports/inventory - generating inventory report");
        InventoryReportDTO report = reportService.generateInventoryReport();
        log.info("GET /reports/inventory - report generated successfully");
        return ResponseEntity.ok(report);
    }
}
