# Tokyo Front

Aplicação Angular para gerenciamento de usuários e endereços, integrada à API Spring Boot do desafio.

## Pré-requisitos

- Node.js e npm
- API executando em `http://localhost:8080`
- PostgreSQL configurado para a API

## Executar localmente

1. Inicie o backend conforme as instruções em `../../backend/desafio/README.md`.
2. Na pasta `frontend/tokyo-front`, instale as dependências e inicie o Angular:

   ```powershell
   npm install
   npm start
   ```

3. Acesse `http://localhost:4200`.

A URL base da API está definida em `src/environments/environment.ts`. Para usar outra URL, ajuste `apiUrl`. O backend precisa permitir a origem do frontend por meio de `CORS_ALLOWED_ORIGINS`.

## Funcionalidades

- Login JWT e cadastro público de usuários.
- Autenticação por cookie HttpOnly com proteção CSRF; o token não é persistido em `localStorage`.
- Painel administrativo com listagem, ordenação e paginação de usuários e endereços.
- Seleção paginada de usuários ao cadastrar endereços e painel com os cadastros mais recentes.
- Edição de perfil, cadastro e gerenciamento de endereços.
- Ao alterar o próprio e-mail, é necessário entrar novamente para renovar a sessão.
- Consulta e validação de CEP pelo ViaCEP durante a operação de salvar endereço na API.
- Mensagens de carregamento, sucesso e erro.

Usuários comuns acessam apenas o próprio perfil e seus endereços; administradores podem gerenciar os dados de todos os usuários.

## Capturas de tela

Telas do painel administrativo:

### Visão geral

![Painel administrativo com resumo de usuários e endereços](./screenshots/admin-overview.png)

### Usuários

![Listagem administrativa de usuários](./screenshots/admin-users.png)

### Endereços

![Listagem administrativa de endereços](./screenshots/admin-addresses.png)

### Formulários administrativos

![Formulário para adicionar usuário](./screenshots/admin-add-user.png)

![Formulário administrativo para adicionar endereço](./screenshots/admin-add-address.png)

### Paginação administrativa

![Segunda página da listagem de usuários](./screenshots/admin-users-page-2.png)

![Segunda página da listagem de endereços](./screenshots/admin-addresses-page-2.png)

### Login e cadastro

![Tela de login](./screenshots/auth-login.png)

![Tela de cadastro de usuário](./screenshots/auth-register.png)

### Área do usuário

![Visão geral da conta de usuário](./screenshots/user-overview.png)

![Endereços da conta de usuário](./screenshots/user-addresses.png)

![Formulário para adicionar endereço](./screenshots/user-add-address.png)

![Edição do perfil do usuário](./screenshots/user-profile.png)

![Edição de endereço do usuário](./screenshots/user-edit-address.png)

![Mensagem de erro ao informar CEP não encontrado](./screenshots/user-invalid-cep.png)

### Resposta da API no Swagger

A consulta autenticada `GET /api/usuarios/me` foi executada pelo Swagger UI e retornou `200 OK`.

![Resposta JSON do perfil autenticado no Swagger](./screenshots/swagger-user-response.png)

### Persistência local no PostgreSQL

Uma consulta somente leitura ao banco local confirmou 17 usuários, 17 endereços e 16 usuários relacionados a um endereço. A captura mostra apenas os totais, sem expor dados pessoais.

![Totais de usuários e endereços persistidos no PostgreSQL local](./screenshots/local-database-persistence.png)

## Componentes principais

- `components/auth-page`: login e cadastro.
- `components/sidebar`: navegação do painel e ações da conta.
- `components/overview`: resumo e acessos rápidos.
- `components/usuarios-list` e `components/enderecos-list`: listagens, ordenação e paginação.
- `components/usuario-form-dialog` e `components/endereco-form-dialog`: formulários reutilizáveis de criação e edição.

## Verificação

```powershell
npm test -- --watch=false
npm run build
```
