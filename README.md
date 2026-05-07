# Kyofuse

Kyofuse is a CS2 social platform where players can create competitive profiles, publish posts, find squads, send team invites and interact with the Counter-Strike 2 community.

## Overview

Kyofuse is a full stack project built as a learning and portfolio application.

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

### 2. Start local services

docker compose up -d

### 3. Run the backend

cd api
./mvnw spring-boot:run

The backend should run at:

http://localhost:8080

### 4. Run the frontend

cd app
npm install
npm start

The frontend should run at:

http://localhost:4200

## API Documentation

After starting the backend, the API documentation will be available at:

http://localhost:8080/swagger-ui/index.html

## Environment Variables

The backend may use the following environment variables:

DB_HOST=localhost
DB_PORT=5432
DB_NAME=kyofuse
DB_USER=kyofuse
DB_PASSWORD=kyofuse
JWT_SECRET=change-me

For local development, default values may be provided in application.yml.

## Database

Kyofuse uses PostgreSQL as its main database.

Database migrations are handled with Flyway.

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

## License

This project is licensed under the Apache License 2.0.

## Status

In development.
