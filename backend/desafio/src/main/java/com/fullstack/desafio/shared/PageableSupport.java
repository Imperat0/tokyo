package com.fullstack.desafio.shared;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PageableSupport {

    public static final int MAX_PAGE_SIZE = 100;

    private PageableSupport() {
    }

    public static Pageable create(
            String pageValue,
            String sizeValue,
            String[] sortValues,
            Sort defaultSort,
            Set<String> allowedSortFields) {
        int page = parseInteger(pageValue, "page");
        int size = parseInteger(sizeValue, "size");
        if (page < 0) {
            throw new InvalidPageRequestException("O parâmetro 'page' deve ser maior ou igual a zero.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPageRequestException(
                    "O parâmetro 'size' deve estar entre 1 e " + MAX_PAGE_SIZE + ".");
        }

        Sort sort = sortValues == null || sortValues.length == 0
                ? defaultSort
                : parseSort(sortValues, allowedSortFields);
        return PageRequest.of(page, size, sort);
    }

    private static int parseInteger(String value, String parameter) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidPageRequestException(
                    "O parâmetro '" + parameter + "' deve ser um número inteiro.");
        }
    }

    private static Sort parseSort(String[] values, Set<String> allowedSortFields) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String value : values) {
            String[] parts = value.split(",", -1);
            if (parts.length > 2 || parts[0].isBlank() || !allowedSortFields.contains(parts[0])) {
                throw new InvalidPageRequestException("Campo de ordenação inválido.");
            }

            Sort.Direction direction;
            try {
                direction = parts.length == 1
                        ? Sort.Direction.ASC
                        : Sort.Direction.fromString(parts[1]);
            } catch (IllegalArgumentException exception) {
                throw new InvalidPageRequestException("Direção de ordenação inválida.");
            }
            orders.add(new Sort.Order(direction, parts[0]));
        }
        return Sort.by(orders);
    }
}
