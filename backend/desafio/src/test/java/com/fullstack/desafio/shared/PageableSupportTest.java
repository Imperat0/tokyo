package com.fullstack.desafio.shared;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableSupportTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of("nome", "email", "createdAt");

    @Test
    void createsPagedRequestWithDefaultsAndAllowedRepeatedSorts() {
        var defaults = PageableSupport.create("0", "20", null, Sort.by("nome"), ALLOWED_FIELDS);
        assertEquals(20, defaults.getPageSize());
        assertEquals("nome", defaults.getSort().getOrderFor("nome").getProperty());

        var custom = PageableSupport.create(
                "2", "5", new String[]{"nome,asc", "email,desc"}, Sort.unsorted(), ALLOWED_FIELDS);
        assertEquals(2, custom.getPageNumber());
        assertEquals(5, custom.getPageSize());
        assertEquals(Sort.Direction.DESC, custom.getSort().getOrderFor("email").getDirection());
    }

    @Test
    void rejectsInvalidPageSizeAndOrderingValues() {
        assertThrows(InvalidPageRequestException.class,
                () -> PageableSupport.create("-1", "10", null, Sort.unsorted(), ALLOWED_FIELDS));
        assertThrows(InvalidPageRequestException.class,
                () -> PageableSupport.create("0", "101", null, Sort.unsorted(), ALLOWED_FIELDS));
        assertThrows(InvalidPageRequestException.class,
                () -> PageableSupport.create("0", "10", new String[]{"senha,asc"}, Sort.unsorted(), ALLOWED_FIELDS));
        assertThrows(InvalidPageRequestException.class,
                () -> PageableSupport.create("0", "10", new String[]{"nome,sideways"}, Sort.unsorted(), ALLOWED_FIELDS));
    }
}
