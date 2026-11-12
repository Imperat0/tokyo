package com.fullstack.desafio;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.fullstack.desafio.endereco.Endereco;
import com.fullstack.desafio.endereco.EnderecoRepository;
import com.fullstack.desafio.endereco.viacep.ViaCepResponse;
import com.fullstack.desafio.endereco.viacep.ViaCepService;
import com.fullstack.desafio.usuario.Perfil;
import com.fullstack.desafio.usuario.Usuario;
import com.fullstack.desafio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
class DesafioApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @MockitoBean
    private ViaCepService viaCepService;

    @Test
    void contextLoads() {
    }

    @Test
    void repositoriesPersistAndQueryUsersAndAddresses() {
        Usuario usuario = new Usuario();
        usuario.setNome("Repository Test");
        usuario.setEmail("repo-" + UUID.randomUUID() + "@example.com");
        usuario.setSenha("bcrypt-hash");
        usuario.setPerfil(Perfil.USER);
        usuario = usuarioRepository.saveAndFlush(usuario);

        assertTrue(usuarioRepository.findByEmail(usuario.getEmail()).isPresent());

        Endereco endereco = new Endereco();
        endereco.setRua("Rua dos Testes");
        endereco.setNumero("42");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01001000");
        endereco.setUsuario(usuario);
        endereco = enderecoRepository.saveAndFlush(endereco);

        assertTrue(enderecoRepository.existsByIdAndUsuario_Email(endereco.getId(), usuario.getEmail()));
        assertEquals(1, enderecoRepository.findByUsuarioId(
                usuario.getId(), org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void publicRegistrationLoginAndProtectedEndpointsWork() throws Exception {
        String email = "integration-" + UUID.randomUUID() + "@example.com";
        CsrfSession registrationCsrf = csrfSession();
        ResponseEntity<String> created = restTemplate.postForEntity("/api/usuarios", json("""
                {"nome":"Integration User","email":"%s","senha":"senha-segura-123"}
                """.formatted(email), registrationCsrf), String.class);
        assertEquals(201, created.getStatusCode().value());
        JsonNode user = objectMapper.readTree(created.getBody());
        long userId = user.get("id").asLong();

        ResponseEntity<String> anonymous = restTemplate.getForEntity("/api/usuarios/{id}", String.class, userId);
        assertEquals(401, anonymous.getStatusCode().value());
        assertProblem(anonymous, 401, "Não autenticado");

        CsrfSession userSession = login(email, "senha-segura-123");
        ResponseEntity<String> currentUser = restTemplate.exchange(
                "/api/usuarios/me", HttpMethod.GET, authenticated(userSession), String.class);
        assertEquals(200, currentUser.getStatusCode().value());
        assertEquals(email, objectMapper.readTree(currentUser.getBody()).get("email").asText());

        ResponseEntity<String> ownProfile = restTemplate.exchange(
                "/api/usuarios/{id}", HttpMethod.GET, authenticated(userSession), String.class, userId);
        assertEquals(200, ownProfile.getStatusCode().value());

        ResponseEntity<String> userList = restTemplate.exchange(
                "/api/usuarios", HttpMethod.GET, authenticated(userSession), String.class);
        assertEquals(403, userList.getStatusCode().value());
        assertProblem(userList, 403, "Acesso negado");

        CsrfSession otherCsrf = csrfSession();
        String otherEmail = "other-" + UUID.randomUUID() + "@example.com";
        ResponseEntity<String> otherCreated = restTemplate.postForEntity("/api/usuarios", json("""
                {"nome":"Other User","email":"%s","senha":"senha-segura-123"}
                """.formatted(otherEmail), otherCsrf), String.class);
        long otherId = objectMapper.readTree(otherCreated.getBody()).get("id").asLong();
        ResponseEntity<String> otherProfile = restTemplate.exchange(
                "/api/usuarios/{id}", HttpMethod.GET, authenticated(userSession), String.class, otherId);
        assertEquals(403, otherProfile.getStatusCode().value());
        assertProblem(otherProfile, 403, "Acesso negado");
    }

    @Test
    void malformedJsonAndInvalidPathParameterReturnProblemDetails() throws Exception {
        CsrfSession csrf = csrfSession();
        ResponseEntity<String> malformed = restTemplate.exchange(
                "/api/usuarios", HttpMethod.POST, json("{", csrf), String.class);
        assertEquals(400, malformed.getStatusCode().value());
        assertProblem(malformed, 400, "Requisição malformada");

        CsrfSession admin = loginAsDefaultAdmin();
        ResponseEntity<String> invalidPath = restTemplate.exchange(
                "/api/usuarios/not-a-number", HttpMethod.GET, authenticated(admin), String.class);
        assertEquals(400, invalidPath.getStatusCode().value());
        assertProblem(invalidPath, 400, "Parâmetro inválido");
    }

    @Test
    void invalidLoginReturnsStandardUnauthorizedProblem() throws Exception {
        CsrfSession csrf = csrfSession();
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/login", HttpMethod.POST, json("""
                        {"email":"missing@example.com","senha":"incorrect"}
                        """, csrf), String.class);

        assertEquals(401, response.getStatusCode().value());
        assertProblem(response, 401, "Falha na autenticação");
    }

    @Test
    void addressCrudAndOwnershipAreEnforced() throws Exception {
        String email = "address-" + UUID.randomUUID() + "@example.com";
        CsrfSession registrationCsrf = csrfSession();
        ResponseEntity<String> createdUser = restTemplate.postForEntity("/api/usuarios", json("""
                {"nome":"Address Owner","email":"%s","senha":"senha-segura-123"}
                """.formatted(email), registrationCsrf), String.class);
        long userId = objectMapper.readTree(createdUser.getBody()).get("id").asLong();
        CsrfSession owner = login(email, "senha-segura-123");

        when(viaCepService.buscar("01001000")).thenReturn(
                new ViaCepResponse("01001000", "Praça da Sé", "Sé", "São Paulo", "SP", null));
        ResponseEntity<String> createdAddress = restTemplate.exchange(
                "/api/usuarios/{id}/enderecos",
                HttpMethod.POST,
                json("""
                        {"numero":"10","complemento":"","cep":"01001000"}
                        """, owner),
                String.class,
                userId);
        assertEquals(201, createdAddress.getStatusCode().value());
        long addressId = objectMapper.readTree(createdAddress.getBody()).get("id").asLong();

        CsrfSession other = loginAsNewUser();
        ResponseEntity<String> forbidden = restTemplate.exchange(
                "/api/enderecos/{id}", HttpMethod.GET, authenticated(other), String.class, addressId);
        assertEquals(403, forbidden.getStatusCode().value());

        ResponseEntity<String> updated = restTemplate.exchange(
                "/api/enderecos/{id}",
                HttpMethod.PUT,
                json("""
                        {"numero":"12","complemento":"Apto 2","cep":"01001000"}
                        """, owner),
                String.class,
                addressId);
        assertEquals(200, updated.getStatusCode().value());
        assertEquals("12", objectMapper.readTree(updated.getBody()).get("numero").asText());

        ResponseEntity<String> page = restTemplate.exchange(
                "/api/usuarios/{id}/enderecos?page=0&size=10&sort=createdAt,desc",
                HttpMethod.GET,
                authenticated(owner),
                String.class,
                userId);
        assertEquals(200, page.getStatusCode().value());
        assertEquals(1, objectMapper.readTree(page.getBody()).get("totalElements").asInt());

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/enderecos/{id}", HttpMethod.DELETE, authenticated(owner), Void.class, addressId);
        assertEquals(204, deleted.getStatusCode().value());
    }

    @Test
    void paginationRejectsInvalidSizeAndSortWithProblemDetails() throws Exception {
        CsrfSession admin = loginAsDefaultAdmin();
        for (String query : List.of("?size=101", "?page=-1", "?sort=senha,asc", "?sort=nome,sideways")) {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/usuarios" + query, HttpMethod.GET, authenticated(admin), String.class);
            assertEquals(400, response.getStatusCode().value(), query);
            assertProblem(response, 400, "Paginação ou ordenação inválida");
        }

        ResponseEntity<String> ordered = restTemplate.exchange(
                "/api/usuarios?page=0&size=100&sort=email,asc&sort=createdAt,desc",
                HttpMethod.GET,
                authenticated(admin),
                String.class);
        assertEquals(200, ordered.getStatusCode().value());
        assertEquals(100, objectMapper.readTree(ordered.getBody()).get("size").asInt());
        List<String> emails = new ArrayList<>();
        objectMapper.readTree(ordered.getBody()).get("content")
                .forEach(item -> emails.add(item.get("email").asText()));
        List<String> sortedEmails = new ArrayList<>(emails);
        sortedEmails.sort(String::compareTo);
        assertEquals(sortedEmails, emails);
    }

    @Test
    void routesOutsideApiAndExplicitPublicRoutesAreNotImplicitlyExposed() throws Exception {
        CsrfSession admin = loginAsDefaultAdmin();
        ResponseEntity<String> unknown = restTemplate.exchange(
                "/future-route", HttpMethod.GET, authenticated(admin), String.class);
        assertEquals(403, unknown.getStatusCode().value());
        assertProblem(unknown, 403, "Acesso negado");

        HttpHeaders missingCsrfHeaders = new HttpHeaders();
        missingCsrfHeaders.add(HttpHeaders.COOKIE, admin.cookieHeader());
        ResponseEntity<String> missingCsrf = restTemplate.exchange(
                "/api/auth/logout", HttpMethod.POST, new HttpEntity<>(missingCsrfHeaders),
                String.class);
        assertEquals(403, missingCsrf.getStatusCode().value());
        assertProblem(missingCsrf, 403, "Acesso negado");

        ResponseEntity<Void> logout = restTemplate.exchange(
                "/api/auth/logout", HttpMethod.POST, json("{}", admin), Void.class);
        assertEquals(204, logout.getStatusCode().value());
        assertTrue(logout.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));
    }

    private CsrfSession loginAsDefaultAdmin() throws Exception {
        return login("admin@teste.com", "Admin@123");
    }

    private CsrfSession loginAsNewUser() throws Exception {
        String email = "other-" + UUID.randomUUID() + "@example.com";
        CsrfSession registrationCsrf = csrfSession();
        restTemplate.postForEntity("/api/usuarios", json("""
                {"nome":"Other User","email":"%s","senha":"senha-segura-123"}
                """.formatted(email), registrationCsrf), String.class);
        return login(email, "senha-segura-123");
    }

    private CsrfSession login(String email, String password) throws Exception {
        CsrfSession csrf = csrfSession();
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                json("""
                        {"email":"%s","senha":"%s"}
                        """.formatted(email, password), csrf),
                Void.class);
        assertEquals(204, response.getStatusCode().value());
        String authCookie = cookiePair(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        assertNotNull(authCookie);
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=Lax"));
        return new CsrfSession(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    private CsrfSession csrfSession() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/csrf", String.class);
        assertEquals(200, response.getStatusCode().value());
        String csrfCookie = cookiePair(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        assertNotNull(csrfCookie);
        assertFalse(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly"));
        String csrfToken = objectMapper.readTree(response.getBody()).get("token").asText();
        return new CsrfSession(null, csrfCookie, csrfToken);
    }

    private HttpEntity<Void> authenticated(CsrfSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, session.cookieHeader());
        headers.set("X-XSRF-TOKEN", session.csrfToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> json(String body, CsrfSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, session.cookieHeader());
        headers.set("X-XSRF-TOKEN", session.csrfToken());
        return new HttpEntity<>(body, headers);
    }

    private String cookiePair(String setCookie) {
        return setCookie == null ? null : setCookie.split(";", 2)[0];
    }

    private record CsrfSession(String authCookie, String csrfCookie, String csrfToken) {
        private String cookieHeader() {
            return Stream.of(authCookie, csrfCookie)
                    .filter(value -> value != null && !value.isBlank())
                    .collect(Collectors.joining("; "));
        }
    }

    private void assertProblem(ResponseEntity<String> response, int status, String title) throws Exception {
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        JsonNode problem = objectMapper.readTree(response.getBody());
        assertEquals(status, problem.get("status").asInt());
        assertEquals(title, problem.get("title").asText());
    }
}
