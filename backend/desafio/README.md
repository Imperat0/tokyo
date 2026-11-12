# API de Usuários e Endereços

API REST com Java 21, Spring Boot, Spring Security, JWT, JPA, PostgreSQL e Flyway. O cadastro e a atualização de endereços consultam o ViaCEP para validar o CEP e preencher logradouro, bairro, cidade e UF.

## Requisitos

- Java 21
- Docker Desktop (ou PostgreSQL 17 instalado localmente)
- Insomnia ou curl para testar as rotas

## Instalação e execução

Abra um terminal na pasta `desafio` do projeto (onde estão `pom.xml`, `mvnw.cmd` e `compose.yaml`).

### 1. Iniciar o PostgreSQL com Docker

```powershell
docker compose up -d postgres
docker compose ps
```

O Compose cria o banco `fullstack`, usuário `fullstack` e senha `fullstack` na porta `5432`. O volume `pgdata` mantém os dados entre reinicializações. Para parar o banco, use `docker compose down`; para apagar também os dados persistidos, use `docker compose down -v`.

### 2. Configurar e iniciar a API

No PowerShell, configure uma chave JWT própria (mínimo 32 bytes) e, se desejar, altere as credenciais do administrador inicial:

```powershell
$env:JWT_SECRET = "coloque-uma-chave-secreta-com-mais-de-32-bytes"
$env:ADMIN_EMAIL = "admin@teste.com"
$env:ADMIN_PASSWORD = "Admin@123"
.\mvnw.cmd spring-boot:run
```

Por padrão, a API fica em `http://localhost:8080`. Na primeira inicialização, Flyway cria as tabelas e um usuário `ADMIN`, se ainda não existir um registro com o e-mail configurado.

Também é possível executar o JAR:

```powershell
.\mvnw.cmd package
java -jar target\fullstack-0.0.1-SNAPSHOT.jar
```

### Execução publicada

Ative explicitamente o perfil `prod` e configure as variáveis obrigatórias. A aplicação falha ao iniciar se o segredo JWT ou as credenciais ADMIN de desenvolvimento forem usados; o cookie exige `Secure`.

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_URL = "jdbc:postgresql://servidor:5432/fullstack"
$env:DB_USER = "usuario-do-banco"
$env:DB_PASSWORD = "senha-do-banco"
$env:JWT_SECRET = "gere-um-segredo-aleatorio-com-ao-menos-32-bytes"
$env:ADMIN_NAME = "Administrador"
$env:ADMIN_EMAIL = "admin@empresa.com"
$env:ADMIN_PASSWORD = "senha"
$env:CORS_ALLOWED_ORIGINS = "https://app.empresa.com"
$env:COOKIE_SAME_SITE = "None"
.\mvnw.cmd spring-boot:run
```

O perfil `prod` exige `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` e `CORS_ALLOWED_ORIGINS`; rejeita credenciais de banco/admin e segredo JWT de desenvolvimento. `COOKIE_SAME_SITE` aceita `Strict`, `Lax` ou `None`. `None` requer HTTPS.

### Configurações disponíveis

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/fullstack` (somente local) | URL JDBC do PostgreSQL; obrigatória no perfil `prod` |
| `DB_NAME` | `fullstack` | Nome do banco criado pelo Compose |
| `DB_USER` | `fullstack` (somente local) | Usuário do banco; obrigatório no perfil `prod` |
| `DB_PASSWORD` | `fullstack` (somente local) | Senha do banco; obrigatória no perfil `prod` |
| `JWT_SECRET` | Chave de desenvolvimento do `application.yml` (somente local) | Segredo HS256; obrigatório no perfil `prod` |
| `JWT_EXPIRATION_MINUTES` | `120` | Validade do token |
| `ADMIN_NAME` | `Administrador` (somente local) | Nome do administrador inicial; obrigatório no perfil `prod` |
| `ADMIN_EMAIL` | `admin@teste.com` (somente local) | E-mail do administrador inicial; obrigatório no perfil `prod` |
| `ADMIN_PASSWORD` | `Admin@123` (somente local) | Senha do administrador inicial; obrigatória no perfil `prod` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origens web permitidas; obrigatório no perfil `prod` |
| `COOKIE_SAME_SITE` | `None` no perfil `prod` | Política SameSite do cookie; `None` exige HTTPS |
| `VIACEP_BASE_URL` | `https://viacep.com.br/ws` | URL base do ViaCEP |

O administrador é criado apenas se o e-mail ainda não existir. Alterar `ADMIN_PASSWORD` depois da criação não troca automaticamente a senha armazenada.

## Swagger e Insomnia

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Especificação OpenAPI em JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

No Swagger, consulte `GET /api/auth/csrf` e use **Authorize** para preencher `csrfTokenAuth`; o navegador mantém os cookies, incluindo o cookie HttpOnly após o login. Nas operações autenticadas de escrita, cookie de sessão e token CSRF são exigidos juntos. No Insomnia, mantenha os cookies retornados pelo login e envie o token CSRF nas operações não seguras.

## Capturas de tela

As imagens estão em `../../frontend/tokyo-front/screenshots/`. A galeria também está no [README do frontend](../../frontend/tokyo-front/README.md).

### Telas do usuário

![Login](../../frontend/tokyo-front/screenshots/auth-login.png)

![Cadastro de usuário](../../frontend/tokyo-front/screenshots/auth-register.png)

![Visão geral do usuário](../../frontend/tokyo-front/screenshots/user-overview.png)

![Endereços do usuário](../../frontend/tokyo-front/screenshots/user-addresses.png)

![Formulário de endereço](../../frontend/tokyo-front/screenshots/user-add-address.png)

![Edição de perfil](../../frontend/tokyo-front/screenshots/user-profile.png)

![Edição de endereço](../../frontend/tokyo-front/screenshots/user-edit-address.png)

![Mensagem de CEP não encontrado](../../frontend/tokyo-front/screenshots/user-invalid-cep.png)

### Painel administrativo

![Visão geral administrativa](../../frontend/tokyo-front/screenshots/admin-overview.png)

![Usuários no painel administrativo](../../frontend/tokyo-front/screenshots/admin-users.png)

![Endereços no painel administrativo](../../frontend/tokyo-front/screenshots/admin-addresses.png)

![Formulário para adicionar usuário](../../frontend/tokyo-front/screenshots/admin-add-user.png)

![Formulário para adicionar endereço](../../frontend/tokyo-front/screenshots/admin-add-address.png)

### Paginação administrativa

![Segunda página de usuários](../../frontend/tokyo-front/screenshots/admin-users-page-2.png)

![Segunda página de endereços](../../frontend/tokyo-front/screenshots/admin-addresses-page-2.png)

### Resposta da API no Swagger

`GET /api/usuarios/me` retornou `200 OK`:

![Resposta da API para o usuário autenticado](../../frontend/tokyo-front/screenshots/swagger-user-response.png)

### Persistência local no PostgreSQL

Uma consulta somente leitura ao banco local confirmou 17 usuários, 17 endereços e 16 usuários relacionados a um endereço. A captura mostra apenas os totais, sem expor dados pessoais.

![Totais de usuários e endereços persistidos no PostgreSQL local](../../frontend/tokyo-front/screenshots/local-database-persistence.png)

## Autenticação e permissões

Faça login com `POST /api/auth/login`. A API grava o JWT no cookie `ACCESS_TOKEN` com as flags `HttpOnly`, `SameSite` e, fora do desenvolvimento, `Secure`. O navegador envia o cookie automaticamente. Para `POST`, `PUT` e `DELETE`, obtenha um token CSRF em `GET /api/auth/csrf` e envie-o no header `X-XSRF-TOKEN`.

- **Sem autenticação:** login e cadastro público de usuário.
- **Usuário `USER`:** consulta e altera seu próprio perfil e seus próprios endereços.
- **Usuário `ADMIN`:** gerencia todos os usuários e endereços e pode listar recursos globalmente.
- O cadastro público sempre cria um usuário `USER`; o cliente não pode escolher o perfil.

As senhas são armazenadas com BCrypt. Tokens JWT expiram conforme `JWT_EXPIRATION_MINUTES`. Rotas não listadas explicitamente na configuração de segurança são negadas por padrão.

## Endpoints

Todos os caminhos são relativos a `http://localhost:8080`. O cliente deve manter os cookies de sessão e enviar o token CSRF nos métodos `POST`, `PUT` e `DELETE`.

### Autenticação

#### `POST /api/auth/login` — público

Request:

```json
{
  "email": "admin@teste.com",
  "senha": "Admin@123"
}
```

Resposta `204 No Content` com `Set-Cookie: ACCESS_TOKEN=...; HttpOnly; SameSite=...`.
Antes do login, consulte `GET /api/auth/csrf`; a resposta contém o token CSRF e define o cookie `XSRF-TOKEN`.

```powershell
$csrf = Invoke-RestMethod -Uri http://localhost:8080/api/auth/csrf -SessionVariable session
$headers = @{ "X-XSRF-TOKEN" = $csrf.token }
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -WebSession $session -Headers $headers -ContentType "application/json" `
  -Body '{"email":"admin@teste.com","senha":"Admin@123"}'
```

Credenciais incorretas retornam `401 Unauthorized`.

#### `GET /api/auth/csrf` — público

Define o cookie `XSRF-TOKEN` e retorna `{ "token": "..." }`. Envie esse token no header `X-XSRF-TOKEN` em toda operação `POST`, `PUT` ou `DELETE`.

#### `POST /api/auth/logout` — público com CSRF

Encerra a sessão removendo o cookie `ACCESS_TOKEN`. Envie o cookie CSRF e o header `X-XSRF-TOKEN`.

Exemplo de chamada autenticada no PowerShell (a sessão mantém os cookies):

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/usuarios/1 -WebSession $session
```

### Usuários

#### `POST /api/usuarios` — público com CSRF

Registra sempre um usuário `USER`; não aceite nem envie `perfil`.

```json
{
  "nome": "Ana Silva",
  "email": "ana@example.com",
  "senha": "MinhaSenha123"
}
```

Resposta `201 Created`: objeto com `id`, `nome`, `email`, `perfil`, `createdAt` e `updatedAt`. A senha nunca é retornada. E-mail duplicado retorna `409 Conflict`; dados inválidos retornam `400 Bad Request`.

Exemplo de cadastro:

```powershell
$csrf = Invoke-RestMethod -Uri http://localhost:8080/api/auth/csrf -SessionVariable registrationSession
$registrationHeaders = @{ "X-XSRF-TOKEN" = $csrf.token }
$body = @{
  nome = "Ana Silva"
  email = "ana@example.com"
  senha = "MinhaSenha123"
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/usuarios `
  -WebSession $registrationSession -Headers $registrationHeaders `
  -ContentType "application/json" -Body $body
```

#### `GET /api/usuarios?page=0&size=20&sort=nome,asc` — somente ADMIN

Lista usuários paginados. `size` deve estar entre 1 e 100. Campos de ordenação aceitos: `nome`, `email` e `createdAt`. Para ordenar por vários campos, repita `sort`, por exemplo `?sort=nome,asc&sort=email,desc`. Entradas inválidas retornam `400 application/problem+json`.

Resposta `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Ana Silva",
      "email": "ana@example.com",
      "perfil": "USER",
      "createdAt": "2026-01-10T12:00:00Z",
      "updatedAt": "2026-01-10T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

#### `GET /api/usuarios/me` — autenticado

Retorna o perfil associado ao e-mail presente no token JWT. A resposta tem o mesmo formato de `GET /api/usuarios/{id}` e permite que o frontend identifique a conta autenticada sem expor a listagem de usuários.

#### `GET /api/usuarios/{id}` — ADMIN ou o próprio usuário

Retorna o perfil (`200 OK`); `404 Not Found` se não existir.

#### `PUT /api/usuarios/{id}` — ADMIN ou o próprio usuário

`nome` e `email` são obrigatórios. Omita `senha` ou envie `null` para manter a senha atual.

```json
{
  "nome": "Ana Souza",
  "email": "ana.souza@example.com",
  "senha": "NovaSenha123"
}
```

Resposta `200 OK` com o perfil atualizado. E-mail já utilizado retorna `409 Conflict`.

#### `DELETE /api/usuarios/{id}` — ADMIN ou o próprio usuário

Remove o usuário e seus endereços associados. Resposta `204 No Content`.

### Endereços

O corpo abaixo é usado para criar e atualizar endereços:

```json
{
  "rua": "Praça da Sé",
  "numero": "100",
  "complemento": "Apto 12",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "estado": "SP",
  "cep": "01001-000"
}
```

`numero` e `cep` são obrigatórios; `complemento` é opcional. O CEP pode ser enviado com ou sem hífen. O ViaCEP preenche `rua`, `bairro`, `cidade` e `estado`, independentemente dos valores enviados nesses campos. CEP inexistente retorna `400`; falha do ViaCEP retorna `503 Service Unavailable`.

#### `POST /api/usuarios/{usuarioId}/enderecos` — ADMIN ou dono do usuário

Cria o endereço para o usuário indicado. Resposta `201 Created` com objeto que inclui `usuarioId`, `createdAt` e `updatedAt`.

Exemplo de criação autenticada:

```powershell
$address = @{
  numero = "100"
  complemento = "Apto 12"
  cep = "01001-000"
} | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/usuarios/1/enderecos `
  -WebSession $session -Headers $headers -ContentType "application/json" -Body $address
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "rua": "Praça da Sé",
  "numero": "100",
  "complemento": "Apto 12",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "estado": "SP",
  "cep": "01001000",
  "usuarioId": 1,
  "createdAt": "2026-01-10T12:00:00Z",
  "updatedAt": "2026-01-10T12:00:00Z"
}
```

#### `GET /api/usuarios/{usuarioId}/enderecos?page=0&size=20&sort=createdAt,desc` — ADMIN ou dono do usuário

Lista os endereços do usuário de forma paginada. Ordenação padrão: `createdAt,desc`; `size` deve estar entre 1 e 100. Campos aceitos: `id`, `rua`, `numero`, `bairro`, `cidade`, `estado`, `cep` e `createdAt`.

#### `GET /api/enderecos?page=0&size=20&sort=createdAt,desc` — somente ADMIN

Lista todos os endereços de forma paginada. `size` deve estar entre 1 e 100; campos de ordenação aceitos são `id`, `rua`, `numero`, `bairro`, `cidade`, `estado`, `cep` e `createdAt`.

#### `GET /api/enderecos/{id}` — ADMIN ou dono do endereço

Retorna o endereço; `404 Not Found` se não existir.

#### `PUT /api/enderecos/{id}` — ADMIN ou dono do endereço

Atualiza os dados usando o mesmo contrato JSON do cadastro e consulta o CEP novamente. Resposta `200 OK`.

#### `DELETE /api/enderecos/{id}` — ADMIN ou dono do endereço

Remove o endereço. Resposta `204 No Content`.

## Respostas de erro

Erros são retornados como `application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Acesso negado",
  "status": 403,
  "detail": "Você não tem permissão para acessar este recurso.",
  "instance": "/api/usuarios"
}
```

| Status | Significado |
|---|---|
| `400 Bad Request` | JSON malformado, parâmetro inválido, CEP não encontrado ou campos inválidos |
| `401 Unauthorized` | Token ausente/inválido ou credenciais de login incorretas |
| `403 Forbidden` | Usuário sem permissão ou falha na validação CSRF |
| `404 Not Found` | Usuário ou endereço não encontrado |
| `409 Conflict` | E-mail duplicado ou conflito de integridade |
| `503 Service Unavailable` | ViaCEP indisponível ou resposta inesperada |

## Testes

Testes unitários:

```powershell
.\mvnw.cmd "-Dtest=GlobalExceptionHandlerTest,PageableSupportTest,ProductionSecurityValidatorTest,UsuarioServiceTest,EnderecoServiceTest,ViaCepServiceTest" test
```

Os testes de integração cobrem `/api/usuarios/me`, isolamento entre usuários, CRUD de endereços, CSRF, autorização e paginação/ordenação. Eles usam PostgreSQL via Testcontainers; sem Docker, a classe de integração é ignorada, então execute-a em CI com Docker disponível:

```powershell
.\mvnw.cmd test
```
