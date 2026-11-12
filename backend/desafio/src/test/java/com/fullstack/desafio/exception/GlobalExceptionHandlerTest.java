package com.fullstack.desafio.exception;

import com.fullstack.desafio.shared.InvalidPageRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void malformedJsonReturnsProblemDetail() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Requisição malformada"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void invalidPathParameterReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/test/items/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro inválido"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void missingRequiredParameterReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/test/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro obrigatório ausente"))
                .andExpect(jsonPath("$.detail").value("O parâmetro obrigatório 'q' não foi informado."));
    }

    @Test
    void invalidPaginationReturnsStandardProblemDetail() throws Exception {
        mockMvc.perform(get("/test/page").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Paginação ou ordenação inválida"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @RestController
    static class TestController {

        @PostMapping("/test/body")
        void body(@org.springframework.web.bind.annotation.RequestBody TestBody body) {
        }

        @GetMapping("/test/items/{id}")
        long item(@PathVariable Long id) {
            return id;
        }

        @GetMapping("/test/search")
        String search(@RequestParam("q") String query) {
            return query;
        }

        @GetMapping("/test/page")
        void page(@RequestParam("page") int page) {
            if (page < 0) throw new InvalidPageRequestException("Página inválida.");
        }
    }

    record TestBody(String name) {
    }
}
