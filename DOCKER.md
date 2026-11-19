# Ambiente Docker — Sprint 4

Este arquivo descreve o ambiente integrado da Sprint 4: frontend Angular, backend Spring Boot, MySQL 8.4, armazenamento persistente dos PDFs criptografados e Mailpit para testes locais de e-mail.

## Pré-requisitos

- Docker Engine 29+ com Docker Compose 5+
- Uma cópia local do repositório

## Configuração segura

Na raiz do projeto, crie o arquivo local de variáveis e preencha valores próprios. Ele é ignorado pelo Git.

```bash
cp .env.example .env
openssl rand -base64 32
```

Use o resultado do segundo comando em `FILE_ENCRYPTION_KEY_BASE64`. A chave deve decodificar para **exatos 32 bytes** e não pode ser reutilizada em produção.

Defina também senhas fortes e distintas para `MYSQL_APP_PASSWORD` e `MYSQL_ROOT_PASSWORD`.

## Subir o ambiente local

```bash
docker compose --env-file .env up --build -d
docker compose ps
```

Serviços disponíveis:

| Serviço | Endereço | Finalidade |
|---|---|---|
| Aplicação Angular | http://localhost:8080 | Interface do Sistema Acervo |
| Mailpit | http://localhost:8025 | Caixa de e-mail de testes local |
| API Spring Boot | rede interna `backend:8080` | API, Flyway e armazenamento de PDFs |
| MySQL | rede interna `mysql:3306` | Persistência de dados |

A porta do MySQL não é publicada no host. Os dados do banco e os PDFs criptografados são mantidos nos volumes nomeados `mysql_acervo` e `arquivos_acervo`.

## Verificação

```bash
docker compose logs --tail=100 backend
docker compose exec mysql mysql -u acervo_app -p acervo
docker compose down
```

O backend aguarda o health check do MySQL e executa a migration Flyway antes de atender requisições. O Mailpit substitui o SMTP externo apenas no ambiente local: as propriedades SMTP do backend são sobrescritas no Compose, sem registrar credenciais de e-mail.

## Imagens da Sprint 4

O Compose usa as tags abaixo, prontas para publicação na conta Docker Hub indicada pelo usuário:

```text
fabiom1992/acervo-backend:sprint-4
fabiom1992/acervo-frontend:sprint-4
```

Após autenticar o Docker Hub com a conta proprietária, publique as imagens com:

```bash
docker login
docker compose --env-file .env build
docker compose --env-file .env push
```

A publicação cria ou atualiza dois repositórios no Docker Hub — um para backend e outro para frontend. Não publique `mysql`, `mailpit`, volumes, arquivos `.env` ou PDFs de usuários.
