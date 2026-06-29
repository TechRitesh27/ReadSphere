package com.p99training.BookStoreSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic wrapper returned by paginated endpoints.
 *
 * Example response:
 * {
 *   "data": [ ...books... ],
 *   "page": 0,
 *   "size": 10,
 *   "totalItems": 53,
 *   "totalPages": 6
 * }
 */
@Data
@Builder
public class PagedResponseDTO<T> {

    private List<T> data;       // the actual items for this page

    private int page;           // current page number (0-based)
    private int size;           // items per page requested
    private long totalItems;    // total matching items across all pages
    private int totalPages;     // how many pages exist in total
}
