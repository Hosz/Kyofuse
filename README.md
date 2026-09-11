# Kyofuse ⚡

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java: 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot: 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular: 21](https://img.shields.io/badge/Angular-21-dd0031.svg?logo=angular)](https://angular.dev/)
[![Tailwind CSS: v4](https://img.shields.io/badge/Tailwind_CSS-v4-38bdf8.svg?logo=tailwindcss)](https://tailwindcss.com/)
[![PostgreSQL: 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Redis: 8](https://img.shields.io/badge/Redis-8-red.svg?logo=redis)](https://redis.io/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)](https://www.docker.com/)
[![Security: Defense--in--Depth](https://img.shields.io/badge/Security-Defense--in--Depth-emerald.svg)](#-arquitetura-de-segurança--privacy-by-design)

**Plataforma social e hub competitivo de alta performance para a comunidade global de Counter-Strike 2 (CS2).**

[Português](#português) • [English](#english)

</div>

---

# Português

## 📖 Visão Geral

O **Kyofuse** é uma rede social e plataforma de recrutamento desenvolvida sob medida para o ecossistema competitivo de **Counter-Strike 2 (CS2)**.

Diferente de redes sociais generalistas, o Kyofuse centraliza as necessidades essenciais de jogadores, elencos táticos e criadores de conteúdo: formação de squads com recrutamento por funções táticas (*AWPer, IGL, Entry, etc.*), perfis detalhados com estatísticas competitivas, comunidades e hubs temáticos, mensageria instantânea com baixa latência e compartilhamento de mídias.

Projetado como um **Monólito Modular orientado pelo Domain-Driven Design (DDD)**, o sistema une a resiliência transacional do Spring Boot no backend à reatividade moderna do Angular no frontend, sustentado por um barramento de tempo real via WebSocket (STOMP), cache e presença via Redis, armazenamento de objetos compatível com S3 (MinIO) e observabilidade completa através do Prometheus e Grafana.

---

## 🎯 Principais Funcionalidades

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           KYOFUSE ECOSYSTEM                             │
├───────────────────┬───────────────────┬─────────────────────────────────┤
│ 🎮 CS2 PROFILES   │ 👥 SQUADS & TEAMS │ ⚡ REAL-TIME ENGINE             │
│ • Patente & Rank  │ • Recrutamento LFM│ • Chat STOMP / WebSocket        │
│ • Funções Táticas │ • Gestão de Elenco│ • Presença Ao Vivo (Redis)      │
│ • Map Pool & Bio  │ • Convites Formais│ • Recibos de Entrega / Leitura  │
├───────────────────┼───────────────────┼─────────────────────────────────┤
│ 🌐 COMUNIDADES    │ 📰 FEED & MÍDIAS  │ 🛡️ IDENTIDADE & SESSÃO          │
│ • Hubs Públicos   │ • Upload Seguro S3│ • 2FA TOTP (RFC 6238)           │
│ • Hubs Privados   │ • Discussões & Com│ • Auditoria de Sessões Ativas   │
│ • Feed Exclusivo  │ • Reações Emojis  │ • Troca Rápida de Contas        │
└───────────────────┴───────────────────┴─────────────────────────────────┘
```

### 🎮 1. Perfil Gamer & Identidade Competitiva
- **Registro de Habilidades e Patente:** Registro de patente competitiva, estilo de jogo e biografia gamer.
- **Funções Táticas de CS2:** Definição clara de função primária e secundária (*Entry Fragger, AWPer, In-Game Leader (IGL), Lurker, Support e Anchor*).
- **Map Pool Preferido:** Seleção dos mapas competitivos de domínio do jogador (Mirage, Inferno, Nuke, Dust II, Ancient, Anubis, etc.).
- **Integração Steam & Bandeira Nacional:** Autenticação federada com Steam OpenID e resolução automática de nacionalidade via GeoIP.
- **Grafo Social & Privacidade:** Conexões bilaterais de amizade com confirmação, seguidores, bloqueio de usuários e controles granulares de privacidade do perfil.

### 👥 2. Gestão de Equipes, Squads & Recrutamento
- **Hub de Equipes:** Criação e personalização de squads com escudo, banner, identificador exclusivo e página do elenco.
- **Mural Tático de Vagas (*Looking For More - LFM*):** Times especificam funções em aberto no elenco para que jogadores compatíveis encontrem oportunidades ideais.
- **Fluxo Bilateral de Ingressos:** Envio e recebimento formal de convites, análise de solicitações de entrada e política de sucessão de liderança.

### 🌐 3. Comunidades & Hubs Temáticos
- **Hubs Especializados:** Criação de comunidades públicas ou restritas com moderação própria, dedicadas a torneios, organizadores e criadores de conteúdo.
- **Comunidades Fixadas:** Fixação de hubs favoritos diretamente no painel principal para acesso rápido.
- **Feeds Segmentados:** Linha do tempo de publicações restrita aos membros da comunidade.

### 📰 4. Feed Social, Mídias & Reações
- **Publicações com Mídias:** Postagens com suporte a marcação de mapas do CS2 e anexação de imagens com compressão e geração de thumbnails automáticos.
- **Engajamento Completo:** Discussões aninhadas com comentários encadeados, contadores consolidados de visualizações e reações com emojis interativos.

### ⚡ 5. Mensageria em Tempo Real & Presença
- **Chat Privado e Grupal:** Troca instantânea de mensagens 1:1 e conversas coletivas com protocolo STOMP sobre WebSocket.
- **Recibos de Entrega e Leitura:** Sincronização multi-dispositivo do status de leitura e visualização em tempo real.
- **Indicador Efêmero de Digitação:** Notificação ágil de digitação ativa (*typing indicator*) sem consumo persistente de banco.
- **Presença Ao Vivo:** Monitoramento de status (*Online, Jogando CS2, Ausente, Offline*) coordenado por heartbeats e pub/sub no Redis.
- **Central de Notificações:** Notificações persistidas e eventos push para menções, convites e interações.

### 📊 6. Descoberta & Leaderboards
- **Filtros de Busca Especializados:** Localização rápida de jogadores e squads combinando patentes, funções e disponibilidade.
- **Leaderboards da Comunidade:** Painéis de destaque ordenados por atividade e métricas competitivas.

---

## 🔒 Arquitetura de Segurança & Privacy by Design

O Kyofuse aplica uma postura rigorosa de **Defense-in-Depth (Defesa em Profundidade)** e **Zero-Trust**:

```
                  INTERNET (Clientes Web / Mobile)
                                 │
                                 ▼
                ┌─────────────────────────────────┐
                │     Nginx Gateway & Ingress     │
                │  (Reverse Proxy, TLS, Headers)  │
                └────────────────┬────────────────┘
                                 │
                ┌────────────────▼────────────────┐
                │    Spring Security Pipeline     │
                │   - Rate Limiting Distribuído   │
                │   - Sanitização de Requisições  │
                │   - Validação de Tokens JWT     │
                └────────────────┬────────────────┘
                                 │
            ┌────────────────────┴────────────────────┐
            ▼                                         ▼
┌───────────────────────────┐             ┌─────────────────────────┐
│     Camada de Domínio     │             │     Camada de Mídia     │
│  - Checagem de Titularidade│             │  - Tika MIME Inspection │
│  - Verificação de Escopo  │             │  - Processamento Seguro │
└───────────┬───────────────┘             └────────────┬────────────┘
            │                                         │
════════════╪═════════════════════════════════════════╪════════════════ [Isolamento de Rede]
            ▼                                         ▼
┌───────────────────────────┐             ┌─────────────────────────┐
│   PostgreSQL 16 (Dados)   │             │   MinIO / S3 (Storage)  │
│  - PII Criptografada      │             │  - Acesso Segregado     │
│  - Sem Exposição Externa  │             │  - Sem Execução Direta  │
└───────────────────────────┘             └─────────────────────────┘
```

### 1. Prevenção de IDOR e Validação de Titularidade
- Todas as requisições autenticadas extraem a identidade do usuário diretamente dos *claims* assinados do JWT (`@AuthenticationPrincipal`).
- O sistema não aceita identificadores de usuário enviados pelo cliente para operações sensíveis. A camada de serviços valida deterministicamente se o usuário logado possui a posse do recurso antes de qualquer alteração ou leitura restrita.

### 2. Gestão de Identidade e Sessões
- **Transporte Seguro de Tokens:** Tokens trafegam via cookies com atributos `HttpOnly`, `SameSite=Lax` e `Secure`, mitigando riscos de exfiltração por scripts maliciosos (XSS).
- **Autenticação em Dois Fatores (2FA/TOTP):** Suporte nativo ao padrão RFC 6238 (Google Authenticator, Bitwarden), mantendo os segredos de TOTP criptografados em repouso.
- **Auditoria de Sessões Concorrentes:** Registro de dispositivos conectados com resolução de IP/GeoIP e User-Agent, permitindo revogação remota de acessos indesejados.
- **Troca Segura de Perfis:** Alternância ágil entre contas vinculadas sem contaminação de contexto de autorização.

### 3. Proteção de Dados e Privacidade (LGPD / GDPR)
- **Criptografia em Repouso de Dados Pessoais:** Dados sensíveis são armazenados no banco de dados sob criptografia simétrica forte.
- **Indexação Cega (*Blind Indexing*):** Consultas de unicidade e buscas em campos de identidade são realizadas através de hashes criptográficos irreversíveis, dispensando a necessidade de expor dados claros em disco.
- **Políticas Estruturadas de Retenção:** Trilhas de auditoria desacopladas com ciclo de vida legal parametrizado para garantir conformidade contínua.

### 4. Mitigação de Abuso e Hardening de Infraestrutura
- **Rate Limiting Distribuído:** Camadas dinâmicas de limitação de requisições sustentadas por Redis protegem endpoints críticos (autenticação, registro, 2FA e criação de publicações).
- **Inspeção Estrita de Mídias:** Validação de cabeçalhos reais (*Magic Bytes*) via Apache Tika em todos os uploads para impedir arquivos executáveis ou malformados camuflados como imagens.
- **Startup Security Enforcer:** Validador automatizado que analisa o ecossistema na inicialização e interrompe o boot em produção caso credenciais fracas ou padrões previsíveis sejam configurados.
- **Segmentação Física de Redes:** A infraestrutura em Docker isola o tráfego de dados (`kyofuse-internal`) do tráfego público (`kyofuse-public`). Bancos de dados e instâncias de cache não possuem portas expostas diretamente para a internet.

---

## 🏛️ Arquitetura de Software & Módulos

O backend é organizado em módulos independentes orientados a contextos de negócio:

```
com.hokyozu.kyofuse/
├── auth/            # Autenticação, 2FA TOTP, sessões e troca de contas
├── users/           # Gerenciamento de usuários, preferências e privacidade
├── profiles/        # Perfis competitivos, patentes e funções táticas
├── teams/           # Gestão de equipes, vagas abertas e cargos
├── communities/     # Hubs temáticos, admissões e moderação comunitária
├── posts/           # Publicações no feed, filtros de mapas e visualizações
├── comments/        # Discussões aninhadas e moderação
├── reactions/       # Reações com emojis em posts e comentários
├── chat/            # Mensageria instantânea 1:1 e salas coletivas (STOMP)
├── presence/        # Rastreamento de conectividade ao vivo via Redis
├── notifications/   # Centro de notificações push e persistidas
├── invites/         # Fluxo bilateral de convites para times e grupos
├── leaderboard/     # Rankings de engajamento e métricas competitivas
├── moderation/      # Denúncias e trilhas de auditoria
├── storage/         # Pipeline de mídias, sanitização e storage S3
└── infrastructure/  # Segurança, Redis, WebSocket e observabilidade
```

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Propósito |
|---|---|---|
| **Backend Core** | Java 21, Spring Boot 4.x | Núcleo do sistema, concorrência e alta performance |
| **Segurança & Auth** | Spring Security, OAuth2, TOTP (RFC 6238) | Autenticação, controle de acesso e 2FA |
| **Banco de Dados** | PostgreSQL 16, Flyway (57+ migrations) | Persistência relacional transacional e versionamento |
| **Tempo Real & Cache** | Redis 8, Spring WebSocket (STOMP) | Presença ao vivo, pub/sub, mensageria e rate limiting |
| **Storage de Mídias** | MinIO / AWS S3 SDK v2, Apache Tika | Armazenamento de mídias, sanitização de binários e thumbnails |
| **Frontend** | Angular 21, Tailwind CSS v4, TypeScript | SPA de alta reatividade com auto-escaping nativo |
| **Observabilidade** | Prometheus, Grafana, Micrometer, Actuator | Monitoramento de métricas, saúde da JVM e latências |
| **DevOps & Infra** | Docker, Docker Compose, Nginx, Mailpit | Orquestração de containers e simulação de SMTP |

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- **Git**
- **Docker** e **Docker Compose** (v2.20+)

### 1. Clonar o Repositório
```bash
git clone https://github.com/YOUR_USERNAME/kyofuse.git
cd kyofuse
```

### 2. Configuração de Variáveis de Ambiente
Crie seu arquivo de configuração local a partir do modelo:
```bash
cp .env.example .env
```

> [!IMPORTANT]
> O arquivo `.env.example` fornece as referências das variáveis necessárias. Antes de iniciar, edite o `.env` gerando credenciais únicas de alta entropia.
> 
> Chaves seguras podem ser geradas via terminal com:
> ```bash
> openssl rand -base64 32
> ```

### 3. Iniciar os Serviços via Docker Compose
Inicie a infraestrutura e os serviços em segundo plano:
```bash
docker compose up -d
```

O compose coordenará a subida sequencial:
1. `postgres`: Inicializa o banco de dados PostgreSQL 16.
2. `redis`: Sobe o cluster de cache e mensageria em tempo real.
3. `minio` & `minio-init`: Inicializa o object storage e cria os buckets de mídia.
4. `mailpit`: Disponibiliza o servidor de captura de e-mails em desenvolvimento.
5. `api`: Compila e sobe o backend Spring Boot, aplicando as migrações Flyway.
6. `app`: Sobe a interface Angular servida pelo Nginx com proxy reverso.
7. `prometheus` & `grafana`: Ativa a esteira de métricas e dashboards.

### 4. Portas e Serviços Locais

| Serviço | URL Local | Descrição |
|---|---|---|
| **Kyofuse Web App** | `http://localhost:4200` | Interface do usuário |
| **API Backend** | `http://localhost:8080` | Endpoints REST e WebSocket |
| **Mailpit Dashboard** | `http://localhost:8025` | Painel local de visualização de e-mails |
| **MinIO Console** | `http://localhost:9001` | Dashboard administrativo do storage |
| **Grafana Dashboards** | `http://localhost:3000` | Painéis e visualização de métricas |

---

## 🧪 Qualidade e Testes

Execute as suítes de testes unitários e de integração:

```bash
# Executar suíte de testes do Backend (Spring Boot + JUnit 5 + Mockito)
cd api
./mvnw test

# Executar suíte de testes do Frontend (Angular + Vitest)
cd ../app
npm test
```

---

## 🧭 Roadmap do Projeto

- [x] **Fase 1 — Núcleo de Identidade, Autenticação & 2FA:** JWT, TOTP, multi-conta, auditoria de sessões e recuperação segura.
- [x] **Fase 2 — Perfil Gamer & Grafo Social:** Patentes de CS2, funções táticas, mapas prediletos, amizades e bloqueios.
- [x] **Fase 3 — Times, Recrutamento & Comunidades:** Vagas por função (LFM), hubs públicos e privados, moderação de membros.
- [x] **Fase 4 — Feed Social, Mídias & Reações:** Upload seguro de mídias, comentários e reações em tempo real.
- [x] **Fase 5 — Chat em Tempo Real & Presença:** WebSocket STOMP, recibos de leitura, typing indicator e pub/sub Redis.
- [x] **Fase 6 — Observabilidade & Métricas:** Instrumentação com Micrometer, Prometheus e dashboards no Grafana.
- [ ] **Fase 7 — Próximos Passos:** Integração com estatísticas da FACEIT, canais de voz WebRTC para squads e gerenciamento automatizado de campeonatos.

---

## 🤝 Padrão de Contribuição e Git

O repositório adota **Conventional Commits** e estrutura limpa de branches:

- `main`: Código validado para produção.
- `develop`: Integração das novas funcionalidades em desenvolvimento.
- `feature/*`: Novas funcionalidades.
- `fix/*`: Resolução de correções e bugs.
- `chore/*`: Atualizações de dependências e documentações.

```bash
# Exemplos de commits:
feat(chat): implement multi-device read synchronization
fix(teams): prevent duplicate pending invitations
```

---

# English

## 📖 Overview

**Kyofuse** is a high-performance, competitive social platform and team recruitment network engineered exclusively for the **Counter-Strike 2 (CS2)** ecosystem.

Unlike generic social media, Kyofuse addresses the concrete requirements of players, competitive rosters, and gaming communities: tactical role-based squad recruitment (*AWPer, IGL, Entry Fragger, etc.*), authentic competitive gamer profiles, dedicated hubs, low-latency communication, and rich media sharing.

Built as a **Modular Monolith guided by Domain-Driven Design (DDD)**, Kyofuse combines the transactional reliability of Spring Boot 4 on the backend with the modern reactivity of Angular 21 on the frontend. The platform incorporates WebSocket STOMP for instant messaging, Redis for distributed presence and caching, S3-compatible media storage (MinIO), and full-stack observability powered by Prometheus and Grafana.

---

## 🎯 Key Capabilities

### 🎮 1. Gamer Identity & Competitive CS2 Profiles
- **Rank & Skill Records:** Track competitive ranks, competitive playstyle, and gamer biography.
- **Tactical Roles:** Clear primary and secondary role specializations (*Entry Fragger, AWPer, In-Game Leader, Lurker, Support, and Anchor*).
- **Map Pool Preferences:** Highlight mastery across active-duty and reserve competitive maps.
- **Steam Integration & Geolocation:** Federated authentication via Steam OpenID and automatic flag resolution through GeoIP.
- **Social Graph & Privacy:** Mutual friendships, followers, user blocking, and fine-grained profile privacy controls.

### 👥 2. Squad Management & Recruitment
- **Team Hubs:** Create and manage squads with custom crests, banners, unique tags, and team roster pages.
- **Tactical Openings (*Looking For More - LFM*):** Publish openings by specific tactical role to match compatible players.
- **Bilateral Invite Workflows:** Formal invite dispatching, application reviews, and transparent leadership succession policies.

### 🌐 3. Thematic Communities & Hubs
- **Dedicated Hubs:** Public and private gaming communities tailored for leagues, scrim groups, and creators.
- **Pinned Hubs:** Custom pin functionality to display priority hubs on the personal dashboard.
- **Segmented Feeds:** Community-exclusive discussion and content streams.

### 📰 4. Social Feed, Media & Reactions
- **Rich Post Creation:** Media attachments with automatic image optimization and thumbnail generation, plus CS2 map tagging.
- **Interactive Discussions:** Threaded nested discussions, consolidated view counting, and live emoji reactions.

### ⚡ 5. Real-Time Engine & Presence (WebSocket + Redis)
- **Direct & Group Messaging:** 1:1 and multi-user direct conversations powered by STOMP over WebSocket.
- **Delivery & Read Receipts:** Instant status updates and cross-device read synchronization.
- **Ephemeral Typing Indicators:** Lightweight typing broadcast without persistent disk write overhead.
- **Live Presence Engine:** Real-time online status (*Online, In-Game, Away, Offline*) backed by Redis heartbeats and pub/sub.
- **Notification Center:** Persistent notification feeds and live push alerts for squad invites, mentions, and interactions.

### 📊 6. Discovery & Leaderboards
- **Tactical Search:** Multi-attribute filtering across ranks, roles, and squad availability.
- **Community Leaderboards:** Activity-based rankings and community standing highlights.

---

## 🔒 Security Architecture & Privacy by Design

Kyofuse enforces a strict **Defense-in-Depth** and **Zero-Trust** security architecture:

### 1. Insecure Direct Object Reference (IDOR) Mitigation
- All authenticated actions derive caller identity exclusively from cryptographically verified JWT claims (`@AuthenticationPrincipal`).
- The system never trusts client-supplied identifiers in request bodies or query parameters. The service layer strictly validates object ownership before executing any query or mutation.

### 2. Session & Identity Governance
- **Hardened Cookie Transport:** Tokens travel strictly via `HttpOnly`, `SameSite=Lax`, and `Secure` cookies, eliminating script-based exfiltration vectors (XSS).
- **Two-Factor Authentication (2FA/TOTP):** Built on RFC 6238 standards, compatible with standard authenticator applications, with TOTP secrets encrypted at rest.
- **Active Session Audit:** Multi-device session tracking with GeoIP and device fingerprint resolution, enabling instant remote revocation.
- **Seamless Account Switching:** Switch between linked profiles safely without cross-account state contamination.

### 3. At-Rest Data Encryption & Privacy Standards
- **Envelope Encryption for Sensitive Data:** Personal Identifiable Information (PII) is encrypted at rest using strong symmetric ciphers.
- **Blind Indexing:** Uniqueness guarantees and lookups on identity fields are performed via cryptographic hashes, avoiding cleartext PII exposure in the database.
- **Structured Data Retention:** Compliant with modern privacy standards (LGPD / GDPR) using append-only immutable audit logs with legal retention rules.

### 4. Abuse Mitigation & Infrastructure Hardening
- **Distributed Rate Limiting:** Redis-backed request throttling on critical authentication, registration, and media upload paths.
- **Binary MIME Inspection:** File uploads are checked for *Magic Bytes* using Apache Tika to block disguised malicious payloads.
- **Startup Security Enforcer:** Fails the production boot process automatically if weak secrets, predictable passwords, or low-entropy credentials are detected.
- **Docker Network Segmentation:** Two isolated network tiers (`kyofuse-internal` vs `kyofuse-public`). Relational databases and caching clusters are completely inaccessible from the outside world.

---

## 🛠️ Technology Stack

| Tier | Technologies |
|---|---|
| **Backend Core** | Java 21, Spring Boot 4.x, Spring Data JPA, Spring Security, Flyway |
| **Frontend** | Angular 21, Tailwind CSS v4, TypeScript, RxJS |
| **Real-Time & Cache** | Redis 8, Spring WebSocket (STOMP), RxStomp |
| **Database** | PostgreSQL 16 |
| **Storage** | MinIO / AWS S3 SDK v2, Apache Tika, Thumbnailator |
| **Observability** | Prometheus, Grafana, Micrometer, Spring Boot Actuator |
| **DevOps** | Docker, Docker Compose, Nginx, Mailpit |

---

## 🚀 Quick Start (Local Setup)

### Prerequisites
- **Git**
- **Docker** and **Docker Compose** (v2.20+)

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/kyofuse.git
cd kyofuse
```

### 2. Environment Configuration
Copy the environment template:
```bash
cp .env.example .env
```

> [!IMPORTANT]
> The `.env.example` file contains safe placeholder values. Edit your `.env` file to set your own distinct, high-entropy secrets before starting the environment.
>
> Generate secure keys easily with:
> ```bash
> openssl rand -base64 32
> ```

### 3. Launch Services via Docker Compose
Spin up the entire platform in one command:
```bash
docker compose up -d
```

### 4. Access Points

| Service | Address |
|---|---|
| **Web Frontend** | `http://localhost:4200` |
| **API & WebSocket** | `http://localhost:8080` |
| **Mailpit Dashboard** | `http://localhost:8025` |
| **MinIO Console** | `http://localhost:9001` |
| **Grafana Dashboards** | `http://localhost:3000` |

---

## 🧪 Testing

```bash
# Run backend test suite (Spring Boot + JUnit 5 + Mockito)
cd api
./mvnw test

# Run frontend test suite (Angular + Vitest)
cd ../app
npm test
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
