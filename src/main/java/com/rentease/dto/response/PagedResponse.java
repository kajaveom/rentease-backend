package com.rentease.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> data;
    private Pagination pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements, int totalPages) {
        return PagedResponse.<T>builder()
                .data(data)
                .pagination(Pagination.builder()
                        .page(page)
                        .size(size)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .build())
                .build();
    }
}
