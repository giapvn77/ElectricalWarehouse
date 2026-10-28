package com.electricalwarehouse.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PagedResult<T> of(Page<E> page, Function<E, T> mapper) {
        return new PagedResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
