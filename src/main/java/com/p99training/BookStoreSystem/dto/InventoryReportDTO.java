package com.p99training.BookStoreSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class InventoryReportDTO {

    private int totalBooks;
    private int totalQuantity;
    private double totalInventoryValue;
    private Map<String, Integer> categoryWiseBookCount;
    private Map<String, Integer> languageWiseReport;
    private Map<String, Integer> publisherWiseReport;
    private Map<Integer, Integer> yearWisePublishedBooks;
}
