# Kyofuse

[English](#english) | [Português](#português)

---

# English

## Overview

Kyofuse is a CS2 social platform where players can create competitive profiles, publish posts, find squads, send team invites and interact with the Counter-Strike 2 community.

This is a full stack project built as a learning and portfolio application.

The goal is to create a niche social network for CS2 players, focused on player profiles, squads, feed posts, team invites and community interaction.

The project starts as a modular monolith and may evolve with real-time features, search, messaging, AI, observability and external integrations.

## Main Features

### V1 Scope

- User registration and authentication
- JWT-based login
- Gamer profile for CS2 players
- Feed posts
- Comments
- Reactions
- Player search
- Teams and squads
- Team invites
- Persistent notifications

### Future Scope

- Real-time notifications with SSE or WebSocket
- Chat
- Steam integration
- FACEIT integration
- Redis cache
- Full-text search with Elasticsearch
- Messaging with RabbitMQ or Kafka
- AI-based player recommendations
- AI moderation
- Observability with Prometheus, Grafana and OpenTelemetry
- CI/CD pipeline
- Production deployment

## Tech Stack

### Backend

Planned backend stack:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Flyway
- Bean Validation
- Swagger/OpenAPI
- JUnit 5
- Mockito
- Testcontainers

### Frontend

- Angular
- Tailwind CSS
- TypeScript

### Infrastructure

Planned infrastructure stack:

- Docker
- Docker Compose
- PostgreSQL

## Project Structure

kyofuse/
├── api/                 # Spring Boot backend
├── app/                 # Angular frontend
├── docs/                # Project documentation
├── docker-compose.yml   # Local infrastructure
├── README.md
├── LICENSE
└── .gitignore

## Architecture

Kyofuse starts as a modular monolith.

Initial backend modules:

auth
users
profiles
posts
comments
reactions
teams
invites
notifications
moderation
shared
infrastructure

The initial architecture prioritizes:

- clear module boundaries
- simple local development
- maintainable code organization
- future evolution without premature microservices
- real business rules instead of only CRUD endpoints

## Getting Started

### Prerequisites

Make sure you have installed:

- Java 21+
- Node.js LTS
- npm
- Docker
- Docker Compose
- Git

## Running Locally

### 1. Clone the repository

git clone https://github.com/YOUR_USERNAME/kyofuse.git
cd kyofuse

### 2. Install frontend dependencies

cd app
npm install

### 3. Run the frontend

npm start

The frontend should run at:

http://localhost:4200

### 4. Backend

The backend setup is planned/in progress.

When available, it should run at:

http://localhost:8080

### 5. Local services

Docker Compose support is planned.

When available, local services should be started with:

docker compose up -d

## API Documentation

Swagger/OpenAPI documentation is planned.

When available, it should be accessible at:

http://localhost:8080/swagger-ui/index.html

## Environment Variables

The backend may use the following environment variables in local development:

DB_HOST=localhost
DB_PORT=5432
DB_NAME=kyofuse
DB_USER=kyofuse
DB_PASSWORD=local-password
JWT_SECRET=local-development-secret

Important:

These values are examples for local development only.
Do not use local credentials or example secrets in production.

For local development, default values may be provided through application.yml or an .env file.

## Database

Kyofuse is planned to use PostgreSQL as its main database.

Database migrations are planned to be handled with Flyway.

Initial planned tables:

users
gamer_profiles
posts
comments
reactions
teams
team_members
team_invites
notifications
reports

## Git Workflow

The project uses a simple branch strategy:

main
develop
feature/*
fix/*
chore/*

Recommended flow:

feature/* -> develop -> main

## Commit Pattern

This project follows Conventional Commits.

Examples:

chore: initialize kyofuse monorepo
chore(app): initialize Angular project with Tailwind
chore(api): initialize Spring Boot project
feat(auth): implement user registration
feat(auth): implement JWT login
feat(profile): create gamer profile
feat(feed): create post endpoint
fix(invites): prevent duplicated pending invite
docs(readme): update setup instructions

## Roadmap

### Phase 0 — Project Setup

- Create monorepo structure
- Configure Angular frontend
- Configure Spring Boot backend
- Configure Docker Compose
- Configure PostgreSQL
- Configure Flyway
- Configure Swagger/OpenAPI

### Phase 1 — Auth and Users

- User registration
- Login
- Password hashing
- JWT generation
- Protected routes
- Current user endpoint

### Phase 2 — Gamer Profile

- Create gamer profile
- Edit profile
- Public profile
- Player search
- CS2 role, rank and map preferences

### Phase 3 — Feed

- Create posts
- List feed
- Edit posts
- Delete posts
- Comments
- Reactions

### Phase 4 — Teams and Invites

- Create teams
- List teams
- Team members
- Send invites
- Accept invites
- Decline invites

### Phase 5 — Notifications

- Persistent notifications
- Unread notification count
- Mark notification as read
- Notification events for invites and interactions

### Phase 6 — Quality

- Unit tests
- Integration tests
- Testcontainers
- API documentation
- Technical documentation
- README improvements

## Security Notes

This repository must not include:

- real database credentials
- production JWT secrets
- API keys
- private tokens
- personal access tokens
- cloud provider credentials
- private environment files
- sensitive user data

Use environment variables or ignored local configuration files for sensitive values.

## License

This project is licensed under the MIT License.

## Status

In development.

Current stage:

- Monorepo structure created
- Angular frontend initialized
- Tailwind CSS configured
- Backend setup in progress
- Local infrastructure setup planned

---

# Português

## Visão geral

Kyofuse é uma plataforma social para jogadores de CS2 criarem perfis competitivos, publicarem posts, encontrarem squads, enviarem convites para times e interagirem com a comunidade de Counter-Strike 2.

Este é um projeto full stack desenvolvido como aplicação de estudo e portfólio.

O objetivo é criar uma rede social nichada para jogadores de CS2, com foco em perfis de jogadores, squads, feed de publicações, convites para times e interação entre a comunidade.

O projeto começa como um monólito modular e pode evoluir futuramente com recursos em tempo real, busca, mensageria, IA, observabilidade e integrações externas.

## Funcionalidades principais

### Escopo da V1

- Cadastro e autenticação de usuários
- Login com JWT
- Perfil gamer para jogadores de CS2
- Publicações no feed
- Comentários
- Reações
- Busca de jogadores
- Times e squads
- Convites para times
- Notificações persistidas

### Escopo futuro

- Notificações em tempo real com SSE ou WebSocket
- Chat
- Integração com Steam
- Integração com FACEIT
- Cache com Redis
- Busca full-text com Elasticsearch
- Mensageria com RabbitMQ ou Kafka
- Recomendações de jogadores com IA
- Moderação com IA
- Observabilidade com Prometheus, Grafana e OpenTelemetry
- Pipeline de CI/CD
- Deploy em produção

## Stack técnica

### Backend

Stack planejada para o backend:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Flyway
- Bean Validation
- Swagger/OpenAPI
- JUnit 5
- Mockito
- Testcontainers

### Frontend

- Angular
- Tailwind CSS
- TypeScript

### Infraestrutura

Stack planejada para infraestrutura:

- Docker
- Docker Compose
- PostgreSQL

## Estrutura do projeto

kyofuse/
├── api/                 # Backend Spring Boot
├── app/                 # Frontend Angular
├── docs/                # Documentação do projeto
├── docker-compose.yml   # Infraestrutura local
├── README.md
├── LICENSE
└── .gitignore

## Arquitetura

O Kyofuse começa como um monólito modular.

Módulos iniciais do backend:

auth
users
profiles
posts
comments
reactions
teams
invites
notifications
moderation
shared
infrastructure

A arquitetura inicial prioriza:

- fronteiras claras entre módulos
- desenvolvimento local simples
- organização de código sustentável
- evolução futura sem microsserviços prematuros
- regras de negócio reais em vez de apenas endpoints CRUD

## Como começar

### Pré-requisitos

Certifique-se de ter instalado:

- Java 21+
- Node.js LTS
- npm
- Docker
- Docker Compose
- Git

## Rodando localmente

### 1. Clone o repositório

git clone https://github.com/YOUR_USERNAME/kyofuse.git
cd kyofuse

### 2. Instale as dependências do frontend

cd app
npm install

### 3. Rode o frontend

npm start

O frontend deve rodar em:

http://localhost:4200

### 4. Backend

A configuração do backend está planejada/em andamento.

Quando disponível, ele deverá rodar em:

http://localhost:8080

### 5. Serviços locais

O suporte a Docker Compose está planejado.

Quando disponível, os serviços locais deverão ser iniciados com:

docker compose up -d

## Documentação da API

A documentação com Swagger/OpenAPI está planejada.

Quando disponível, deverá ser acessada em:

http://localhost:8080/swagger-ui/index.html

## Variáveis de ambiente

O backend poderá usar as seguintes variáveis de ambiente em desenvolvimento local:

DB_HOST=localhost
DB_PORT=5432
DB_NAME=kyofuse
DB_USER=kyofuse
DB_PASSWORD=local-password
JWT_SECRET=local-development-secret

Importante:

Esses valores são exemplos apenas para desenvolvimento local.
Não use credenciais locais ou secrets de exemplo em produção.

Para desenvolvimento local, valores padrão poderão ser definidos por application.yml ou arquivo .env.

## Banco de dados

O Kyofuse está planejado para usar PostgreSQL como banco de dados principal.

As migrations do banco estão planejadas para serem gerenciadas com Flyway.

Tabelas iniciais planejadas:

users
gamer_profiles
posts
comments
reactions
teams
team_members
team_invites
notifications
reports

## Fluxo de Git

O projeto usa uma estratégia simples de branches:

main
develop
feature/*
fix/*
chore/*

Fluxo recomendado:

feature/* -> develop -> main

## Padrão de commits

Este projeto segue Conventional Commits.

Exemplos:

chore: initialize kyofuse monorepo
chore(app): initialize Angular project with Tailwind
chore(api): initialize Spring Boot project
feat(auth): implement user registration
feat(auth): implement JWT login
feat(profile): create gamer profile
feat(feed): create post endpoint
fix(invites): prevent duplicated pending invite
docs(readme): update setup instructions

## Roadmap

### Fase 0 — Setup do projeto

- Criar estrutura de monorepo
- Configurar frontend Angular
- Configurar backend Spring Boot
- Configurar Docker Compose
- Configurar PostgreSQL
- Configurar Flyway
- Configurar Swagger/OpenAPI

### Fase 1 — Auth e usuários

- Cadastro de usuário
- Login
- Hash de senha
- Geração de JWT
- Rotas protegidas
- Endpoint de usuário atual

### Fase 2 — Perfil gamer

- Criar perfil gamer
- Editar perfil
- Perfil público
- Busca de jogadores
- Função, rank e mapas preferidos de CS2

### Fase 3 — Feed

- Criar posts
- Listar feed
- Editar posts
- Deletar posts
- Comentários
- Reações

### Fase 4 — Times e convites

- Criar times
- Listar times
- Membros de time
- Enviar convites
- Aceitar convites
- Recusar convites

### Fase 5 — Notificações

- Notificações persistidas
- Contagem de notificações não lidas
- Marcar notificação como lida
- Eventos de notificação para convites e interações

### Fase 6 — Qualidade

- Testes unitários
- Testes de integração
- Testcontainers
- Documentação da API
- Documentação técnica
- Melhorias no README

## Notas de segurança

Este repositório não deve incluir:

- credenciais reais de banco de dados
- secrets JWT de produção
- chaves de API
- tokens privados
- personal access tokens
- credenciais de provedores cloud
- arquivos privados de ambiente
- dados sensíveis de usuários

Use variáveis de ambiente ou arquivos locais ignorados pelo Git para valores sensíveis.

## Licença

Este projeto está licenciado sob a MIT License.

## Status

Em desenvolvimento.

Estágio atual:

- Estrutura de monorepo criada
- Frontend Angular inicializado
- Tailwind CSS configurado
- Setup do backend em andamento
- Configuração da infraestrutura local planejada
