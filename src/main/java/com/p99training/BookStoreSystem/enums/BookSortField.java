package com.p99training.BookStoreSystem.enums;

/**
 * Allowed fields for sorting books.
 * Using an enum prevents clients from passing arbitrary strings,
 * which would cause a runtime error or expose internal field names.
 */
public enum BookSortField {
    ID,
    TITLE,
    AUTHOR,
    PRICE,
    PUBLISHED_YEAR,
    QUANTITY
}
