# Roteiro Técnico do Kyofuse

Este documento detalha, fase a fase, a ordem de implementação definida para o backlog levantado em 2026-08. A ordem não é arbitrária: cada fase existe porque a anterior resolve um problema (segurança, visibilidade) que a fase seguinte precisaria de qualquer forma para ser bem feita.

Regra geral por trás de toda a ordem: **resolva risco antes de resolver escala**. Falta de rate-limiting no login é um risco que existe *agora*, com o tráfego que você tem hoje. Falta de cache ou de mensageria assíncrona é uma limitação de escala que só dói quando você tiver volume — e você ainda não tem. Por isso segurança e observabilidade vêm antes de Redis/Kafka, mesmo que Redis/Kafka pareçam "mais interessantes" de estudar.

---

## Fase 0 — Endurecer o núcleo de autenticação

**Por que esta fase é a primeira:** você está prestes a *aumentar* a superfície de autenticação (login social, 2FA, troca de contas). Fazer isso em cima de um núcleo que hoje só tem access token + BCrypt sem rate limit significa multiplicar um problema existente por N provedores novos. Corrija a base, depois construa em cima.

### 1. Refresh token
Hoje o sistema emite só um access token JWT (6h de validade) e não existe renovação. Isso é a peça mais estrutural da fase: 2FA, troca de contas e até "lembrar de mim" dependem de existir um conceito de **sessão** mais rico que "um token que expira e pronto".

**O que implementar:** um segundo token (refresh token), opaco ou JWT, de vida mais longa, guardado de forma segura (idealmente httpOnly cookie, não localStorage) e com rotação — cada uso de refresh token invalida o anterior e gera um novo. Isso permite detectar reuso de token roubado (se um refresh token "morto" for usado, é sinal de token vazado, e você pode revogar toda a família de sessões daquele usuário).

**Estudar:** o padrão *refresh token rotation with reuse detection* (RFC 6749 §6 dá a base, mas a rotação com detecção de reuso é uma prática de mercado, não está na RFC — vale procurar por "refresh token rotation reuse detection" especificamente, não só "refresh token JWT tutorial", que geralmente mostra só o caminho feliz sem revogação).

### 2. Segurança de senha / rate limiting no login
Hoje não existe lockout nem contador de tentativas — um script pode tentar senhas indefinidamente contra qualquer conta.

**O que implementar:** um limitador de tentativas por (usuário + IP), com backoff progressivo ou bloqueio temporário após N falhas.

**Estudar:** biblioteca **Bucket4j** (rate limiting em memória ou distribuído, integra bem com Spring Boot). Enquanto você não tem Redis (Fase 2), pode ser em memória; quando tiver Redis, migre o contador para lá, porque contador em memória não sobrevive a restart nem funciona se um dia você tiver mais de uma instância da API rodando.

### 3. Criptografia de dados sensíveis
Primeiro decida **o quê** precisa de criptografia — hoje a entidade `User` guarda `email`, `firstName`, `lastName` em texto puro. Nem tudo precisa virar cifra (nome não é normalmente considerado dado sensível o suficiente para justificar o custo operacional); email já é mais discutível dependendo do seu modelo de ameaça.

**O que implementar:** para os campos que você decidir que precisam, use um `AttributeConverter` do JPA (criptografa/descriptografa de forma transparente na entidade) ou `pgcrypto` na camada do Postgres, se preferir manter a lógica fora da aplicação.

**Estudar:** não é "criptografar tudo por padrão" — é mapear dado que identifica + é sensível (critério parecido com o de LGPD/GDPR). Vale ler sobre *data classification* antes de sair aplicando `@Convert` em toda entidade.

### 4. Política de retenção de dados e auditoria (LGPD)

Nasceu de uma dúvida sobre logs, mas é uma decisão de produto/segurança, não de observabilidade — por isso mora aqui, junto da criptografia (item 3), e não na Fase 1.

**O problema que isso resolve:** se um dia você precisar responder a uma investigação (ex.: fornecer o histórico de conversa de um usuário) ou a um pedido de exclusão de conta, você precisa de dado que *ainda exista* em alguma tabela com propósito — os logs de observabilidade (Fase 1) não servem para isso, são operacionais e de vida curta.

**Princípio central da LGPD que guia o desenho:** *finalidade* (Art. 6) — você só pode reter dado pessoal enquanto houver um motivo concreto e declarado, pelo tempo que esse motivo durar. Isso significa que a auditoria **não deveria copiar tudo que é apagado por padrão** — só o que tem uma base legal específica de retenção (investigação de abuso em andamento, obrigação legal como registro fiscal, defesa em processo judicial). Auditoria "por via das dúvidas" já nasce fora de conformidade.

**O que implementar:**
- Uma tabela de auditoria **somente-inserção** (ex.: `message_audit_log`), separada da tabela viva, com uma cópia (snapshot) dos campos relevantes no momento da exclusão/edição — não uma foreign key para a linha original, porque se a linha original for apagada de verdade, a FK quebra. A auditoria precisa sobreviver independente da tabela viva.
- Cada linha da auditoria carrega, além do dado: `user_id` (denormalizado), `motivo`/`purpose` (ex. `"abuse_investigation"`, `"legal_hold"`, `"tax_obligation"`) e `retention_until` (quando esse motivo deixa de justificar a retenção). Isso permite um job automático de expurgo, em vez de depender de alguém lembrar de apagar manualmente.
- Soft delete (`deleted_at` na tabela viva) continua valendo para o caso comum (moderação do dia a dia, "desfazer exclusão") — a tabela de auditoria é para o caso em que você precisa que o dado sobreviva mesmo que a linha viva seja purgada de vez.

**Como isso se conecta com um pedido de exclusão (Art. 18, V) do usuário:**
1. Usuário pede exclusão.
2. Você checa, por categoria de dado, se alguma exceção do Art. 16 se aplica (obrigação legal, uso interno anonimizado, processo em andamento).
3. O que **não tem exceção**: apaga/anonimiza de verdade — inclusive na auditoria. Estar na tabela de auditoria não isenta o dado da LGPD; ele continua sendo dado pessoal e continua sujeito ao mesmo direito de exclusão. Uma base legal válida só justifica retenção *temporária*, nunca uma exceção permanente por estar "no banco errado".
4. O que **tem exceção**: mantém até o `retention_until` daquele motivo expirar, e então apaga/anonimiza também.
5. Por transparência, idealmente você consegue informar ao usuário o que foi retido e por quê.

**Anonimização como alternativa à exclusão:** quando você quer preservar valor estatístico/histórico sem manter a identidade, substitua os campos identificadores (`user_id` → nulo ou hash irreversível, conteúdo redigido se necessário) em vez de apagar a linha inteira. Uma vez anonimizado de forma irreversível, deixa de ser "dado pessoal" para efeitos da LGPD e pode ficar retido indefinidamente.

**Como ligar os dados na hora de entregar informação** (para o próprio usuário pedindo acesso/portabilidade, ou para uma investigação): use um identificador estável (`user_id`, UUID) presente em toda tabela relevante — mensagens, posts, auditoria. A consulta de "tudo sobre o usuário X" vira: tabela viva (`WHERE deleted_at IS NULL`) unida com a tabela de auditoria (pelos registros ainda dentro do prazo de retenção). Pode começar simples — um serviço/script interno que agrega por `user_id` em cada tabela de domínio; não precisa de ferramenta sofisticada de início, isso é essencialmente um DSAR tool (Data Subject Access Request) rudimentar.

**Estudar:** LGPD Art. 6 (princípios, especialmente finalidade e necessidade), Art. 16 (hipóteses de retenção após término do tratamento) e Art. 18 (direitos do titular, incluindo eliminação e portabilidade). O texto da lei é curto e direto nesses artigos — não precisa de curso, vale ler as fontes primárias antes de qualquer artigo de terceiro que já vem com interpretação.

⚠️ Isto é um desenho técnico, não parecer jurídico — antes de tratar dados reais de usuários em produção, vale validar as classificações e prazos de retenção com alguém habilitado a dar orientação jurídica.

### 5. Autenticação de dois fatores (2FA)
Construído em cima do refresh token da etapa 1 (sem sessão bem definida, "lembrar que esse dispositivo já passou por 2FA" fica difícil).

**Estudar:** TOTP (RFC 6238) — é o mesmo padrão do Google Authenticator/Authy. Bibliotecas Java: `java-otp` ou `com.warrenstrange:googleauth`. Não precisa reinventar nada, é gerar um segredo por usuário, mostrar como QR code, e validar o código de 6 dígitos com uma janela de tempo tolerante a pequeno desvio de relógio.

### 6. Recuperação de conta
Desenhe já assumindo que 2FA existe — senão o fluxo de recuperação vira, na prática, um jeito de contornar o 2FA (é um erro comum: proteger o login com 2FA e deixar a recuperação de senha sem nenhuma camada extra).

### 7. Login com Google e Steam
Você mencionou que pretende usar a API de cada provedor — isso é correto para o Google, mas **atenção a uma diferença importante entre os dois**:

- **Google** tem OAuth2/OIDC padrão. `spring-boot-starter-oauth2-client` resolve praticamente tudo — client-id, client-secret, redirect, e o Spring já sabe validar o token de volta.
- **Steam não tem OAuth2/OIDC.** A Valve usa **OpenID 2.0**, um protocolo mais antigo (o mesmo que sites usavam antes do OAuth2 virar padrão de mercado). Isso significa que o client OAuth2 padrão do Spring **não serve para Steam** — você vai precisar de uma lib específica de OpenID 2.0 ou implementar a validação manualmente (é um fluxo de redirecionamento + validação de assinatura, não é complexo, mas é diferente do que você vai fazer para o Google, então não assuma que vai reaproveitar o mesmo código).

Faça o Google primeiro (é o caminho mais direto e você já vai ter validado a integração de "login social" com um provedor bem documentado), depois trate o Steam como uma integração à parte.

### 8. Troca de contas (multi-conta)
Depende do refresh token da etapa 1: trocar de conta sem perder sessão é, na prática, manter mais de um par (access + refresh) vivo ao mesmo tempo no client e alternar qual está ativo.

---

## Fase 1 — Observabilidade

**Por que vem antes de Redis/Kafka/microsserviços:** você não pode otimizar, dividir ou paralelizar o que não consegue medir. Sem isso, qualquer decisão nas próximas fases vira achismo.

### 1. Logs na aplicação

**📌 Sua dúvida: "vou precisar sair colocando log do SLF4J em todo o projeto?"**

Não, e não deveria. Log em todo método é ruído, não observabilidade — dificulta achar o que importa e ainda pesa em I/O. O Spring Boot já loga bastante por padrão (framework, Hibernate se você ligar `show-sql`, Tomcat, exceptions que sobem até o handler padrão) via SLF4J + Logback nos bastidores — só que em texto simples e sem nada da sua lógica de negócio.

O que fazer, de forma proporcional:

1. **Um filtro transversal (`OncePerRequestFilter`) que gera um correlation ID por requisição** e coloca no MDC do SLF4J. Isso te dá, de graça, log de toda requisição (método, path, status, duração, usuário) sem escrever `log.info` em cada controller.
2. **Log centralizado de exceções** no seu `@ControllerAdvice`/exception handler global — um lugar só, cobre a aplicação inteira.
3. **Logs pontuais e deliberados** só em eventos que realmente importam para segurança ou auditoria: tentativa de login (sucesso/falha), pedido de reset de senha, falha de 2FA, negação de permissão. Isso não é "cobertura de todo método", é logar decisão, não fluxo.

Ou seja: 1 filtro + 1 handler global + logs pontuais em pontos sensíveis. Isso cobre 90% do valor com uma fração do código que "logar tudo" exigiria.

**Formato:** configure logging estruturado (JSON) via `logback-spring.xml` assim que fizer esse trabalho — texto puro é ok para rodar local, mas quando você for ligar isso ao Grafana (via Loki, por exemplo, no futuro) ou só grepar logs em produção, JSON com campos (`traceId`, `userId`, `level`, `message`) é muito mais fácil de filtrar do que string solta.

### 2. Logs no banco de dados

Isso é **configuração do motor do Postgres** (`log_min_duration_statement` para achar queries lentas, `pg_stat_statements` para estatística agregada) — zero código, zero SLF4J. Ativa direto no `postgresql.conf` ou nos parâmetros do container.

**📌 Sua dúvida: "consigo usar esses logs como histórico para uma investigação — ex. fornecer dados de conversa de um usuário — ou ainda preciso ter isso salvo no banco, tipo um soft delete estranho?"**

Ponto importante para alinhar: **"logs do banco de dados" (no sentido de observabilidade) e "histórico para investigação/auditoria" são duas coisas completamente diferentes**, mesmo que os dois envolvam a palavra "log".

Os logs do motor do Postgres (`log_statement`, `pg_stat_statements`, WAL):
- Existem para diagnóstico operacional (query lenta, erro de conexão, crash) — não para virar prova ou histórico de negócio.
- São rotacionados/descartados com frequência (geralmente dias, não meses ou anos) — não são feitos para reter dado por muito tempo.
- Se você ligar `log_statement = 'all'` para "logar tudo", o log passa a conter o **texto literal das queries, incluindo parâmetros** — ou seja, senha, conteúdo de mensagem, tudo em texto puro dentro de um arquivo de log sem controle de acesso equivalente ao do banco. Isso é considerado má prática de segurança justamente por esse motivo — você estaria duplicando dado sensível num lugar com menos proteção que a tabela original.
- O WAL (write-ahead log) tecnicamente contém todas as mudanças, mas ele é feito para replicação e recuperação de desastre (PITR), não para ser consultado como "o que o usuário X disse na conversa Y" — não existe uma forma prática de extrair isso dali como se fosse uma tabela normal.

**Conclusão prática: não dá para responder a uma investigação (ex. "forneça o conteúdo das conversas do usuário X") usando logs do banco. Isso exige que o dado ainda exista, de fato, numa tabela — de propósito.**

O que você descreveu como "soft delete estranho" é exatamente o padrão certo para isso, e não é estranho, é o padrão de mercado:

- **Soft delete** (`deleted_at TIMESTAMP NULL` na tabela, em vez de `DELETE FROM`): a mensagem/registro some da UI e das queries normais da aplicação (você filtra `WHERE deleted_at IS NULL`), mas continua fisicamente na tabela, disponível para uma ferramenta de moderação/admin ou para uma exportação de dados sob pedido legal.
- **Tabela de auditoria separada** (`audit_log` ou `moderation_log`), somente inserção, que guarda uma cópia/snapshot do dado no momento em que ele foi alterado ou "apagado". É mais robusto que soft delete puro porque não depende do ciclo de vida da tabela principal — mesmo que um dia você decida realmente purgar a tabela principal, o registro de auditoria sobrevive.

As duas técnicas não são excludentes: soft delete é bom para operação do dia a dia (moderação, "desfazer exclusão"); tabela de auditoria separada é melhor para retenção legal de longo prazo, porque fica desacoplada do schema operacional.

O desenho completo dessa política de retenção (o que fica na auditoria, por quanto tempo, e como isso se conecta a um pedido de exclusão via LGPD) está detalhado na **Fase 0, item 4 — Política de retenção de dados e auditoria (LGPD)**, já que é uma decisão de produto/segurança, não de observabilidade.

### 3. Prometheus + Grafana

**📌 Sua dúvida: "por que começar por Actuator/Micrometer e não já ir direto para Prometheus e Grafana?"**

Essa pergunta parte de uma premissa que vale corrigir: **Actuator/Micrometer não é uma alternativa a Prometheus/Grafana — é uma peça que os dois precisam para funcionar.** Não existe "pular" para Prometheus sem isso, no seu stack. São camadas diferentes da mesma pilha:

```
[Seu código Spring Boot]
        │  instrumentado por
        ▼
   Micrometer            ← biblioteca dentro da sua JVM, produz as métricas
        │  exposto via
        ▼
Actuator (/actuator/prometheus)  ← endpoint HTTP que expõe essas métricas
        │  raspado (scrape) por
        ▼
    Prometheus            ← banco de séries temporais, guarda o histórico
        │  consultado por
        ▼
     Grafana              ← visualização, dashboards, alertas
```

Prometheus não sabe nada sobre "quantas mensagens de chat foram enviadas por minuto" a não ser que **alguma coisa dentro do seu processo Java produza essa métrica primeiro** — e essa "alguma coisa" é o Micrometer. Sem entender essa camada, você teria Prometheus e Grafana rodando bonitinho, mas só conseguiria enxergar métricas genéricas de JVM (uso de heap, threads) — nenhuma métrica de negócio (mensagens enviadas, pedidos de entrada em comunidade, latência de login), porque essas você tem que instrumentar manualmente com Micrometer (`Counter`, `Timer`, `Gauge`) dentro do seu código.

Ou seja, a ordem de estudo não é "em vez de", é "pré-requisito técnico": Actuator/Micrometer é a parte específica do seu stack (Spring Boot) que você precisa entender para saber *o que* está sendo medido e como adicionar suas próprias métricas; Prometheus/Grafana são as ferramentas genéricas de infraestrutura por cima disso, que você usaria com qualquer stack (Java, Node, Go, não importa).

---

## Fase 2 — Cache (Redis)

**📌 Sua dúvida: "por que Redis não está entre os primeiros itens de maior interesse?"**

Porque Redis, nesse backlog, resolve um problema de **escala** — e você ainda não tem esse problema. Cache existe para reduzir carga/latência quando algo já está lento ou sob volume alto. Hoje, sem tráfego real e sem métricas (por isso a Fase 1 vem antes), você não sabe se algum endpoint é lento o suficiente para justificar cache, nem qual endpoint seria. Colocar Redis agora seria adicionar uma peça de infraestrutura a mais para manter (mais um serviço no compose, mais uma fonte de bug — cache invalidado errado é uma das causas mais comuns de "dado errado aparecendo pro usuário") sem ter evidência de que resolve algo.

Compare com a Fase 0: falta de rate-limiting é um risco que existe **agora**, independente de tráfego — um único atacante já consegue explorar isso hoje. Falta de cache não é um risco, é uma limitação que só aparece com volume. Risco ativo > otimização especulativa.

**Quando a Fase 2 chegar:**
1. Comece pequeno — cache de leitura em endpoints que você já *sabe* que são custosos (feed, perfis públicos, contadores), usando `@Cacheable` do Spring com Redis como provider.
2. Meça o ganho de verdade usando o Grafana que você montou na Fase 1 — antes/depois. Não assuma que cache ajuda; confirme.
3. Aproveite o Redis já disponível para o rate-limiting da Fase 0 (migrar contador de memória para Redis) e para presença online/offline (abaixo).

**Status online/offline e evoluções de tempo real (WebSocket):**
A base de WebSocket/STOMP e chat em tempo real já foi implementada (conexão autenticada via cookies e JWT, recibos de entrega/leitura, histórico por adesão e notificações em tempo real). As próximas evoluções utilizando essa infraestrutura incluem:

- **Presença em tempo real (Online / Offline / Jogando):** Escutar eventos de ciclo de vida da sessão (`SessionConnectedEvent`, `SessionDisconnectEvent`) no backend e integrar com Redis (conjunto de usuários ativos com TTL/heartbeat). Broadcast do status dos amigos (Online, Ausente, Offline) e status rico no Gamer Profile (*"Jogando CS2"*, *"Em partida"*).
- **Indicador de digitação ("Digitando..."):** Mensagens efêmeras via STOMP (`/app/conversations/{id}/typing` -> `/topic/conversations/{id}/typing`) sem persistência no banco, exibindo animação com debounce de ~3s.
- **Reações a mensagens com Emojis:** Broadcast via `/topic/conversations/{id}/reactions` para atualização instantânea dos contadores de reações (👍, 🔥, ❤️, 🎮).
- **Eventos de grupo e moderação ao vivo:** Notificação via `/topic/conversations/{id}/events` quando membros entram, saem, são promovidos a admin ou quando nome/foto do grupo são alterados, atualizando a UI sem refresh.
- **Sincronização multi-aba / multi-dispositivo:** Envio de sinal em `/user/queue/read-sync` para zerar badges de não lidas em todas as abas abertas simultaneamente.
- **Canais de voz com sinalização WebRTC:** Utilização do WebSocket STOMP como servidor de sinalização (troca de SDP Offer/Answer e ICE Candidates) para salas de voz de times e comunidades estilo Discord.
- **Interações sociais do feed em tempo real:** Atualização ao vivo de contadores de curtidas e novos comentários nos posts via `/user/queue/feed-events` ou `/topic/posts/{id}`.

---

## Fase 3 — Arquitetura: microsserviços, Kafka, resiliência

**Por que essa fase é a mais tardia entre as técnicas:** é a decisão mais cara de reverter de todo o roteiro. Migrar para microsserviços cedo demais, sem saber onde estão os gargalos (que só a Fase 1 revela) e sem ainda ter resolvido segurança básica (Fase 0), é o cenário clássico de "complexidade distribuída para resolver um problema que a arquitetura atual nem tinha".

### 1. Quais serviços migrar e como
Comece identificando *bounded contexts* no monólito modular que você já tem — e aqui você já está em vantagem: o projeto já é organizado por pacotes de feature (`chat`, `communities`, `posts`, `teams`, `auth`), o que facilita bastante enxergar onde estariam as fronteiras de um futuro serviço separado.

**Estudar:** *Strangler Fig Pattern* (Martin Fowler) para migração incremental (extrai um serviço de cada vez, sem parar tudo para reescrever); conceitos de *bounded context* (Domain-Driven Design) antes de decidir os limites — decidir a fronteira errada é mais caro de corrigir depois do que não ter decidido nada ainda.

### 2. Kafka

**📌 Sua dúvida (mesma lógica do Redis): por que Kafka não está entre os primeiros itens?**

Kafka resolve comunicação assíncrona **entre serviços**. Hoje você tem um monólito — não existe um "segundo serviço" para conversar de forma assíncrona. Introduzir Kafka agora seria rodar um broker de mensageria distribuído para, na prática, o seu único serviço mandar mensagem para ele mesmo. O valor de Kafka só aparece *depois* que a Fase 3.1 (decisão de microsserviços) definir que existem, de fato, dois ou mais serviços que precisam se comunicar sem acoplamento direto (ex.: serviço de chat publica um evento "mensagem enviada", e um futuro serviço de notificações consome esse evento sem o chat precisar saber que ele existe).

Kafka também tem custo operacional real (partições, consumer groups, offsets, monitoramento de lag) — vale a pena pagar esse custo quando existe um caso de uso concreto, não antes.

**Estudar:** Kafka "Getting Started" oficial, e o padrão *event-driven* (modelar eventos de domínio — "isso aconteceu" — em vez de usar Kafka como uma fila de RPC disfarçada, que é um erro comum de quem está aprendendo).

### 3. Fallback / resiliência
Só fica relevante quando você já tem chamadas para fora do processo que podem falhar de forma independente — Redis, Kafka, provedores OAuth (Google/Steam), e futuros serviços internos. Antes disso, "fallback" não tem muito o que proteger.

**Estudar:** **Resilience4j** (circuit breaker, retry, timeout) — integra direto com Spring Boot, é a biblioteca padrão de mercado para isso hoje (sucessora do Hystrix, que está descontinuado).

---

## Fase 4 — Produto / UX

Baixo risco técnico, pode intercalar com as fases acima quando quiser variar o tipo de trabalho:

1. Aba de descobrir
2. Ajustar times em destaque e sugestões de seguir
3. Adição de mídias aos posts
4. Idiomas do site (i18n)
5. Tema preto e branco

---

## Fase 5 — Avançado: feed com Machine Learning

Deixado por último de propósito: para "aplicar ML no feed" ter algum valor real, você precisa de **dados de interação de usuários reais** para treinar/validar contra alguma coisa — e isso só existe depois que a Fase 1 (logging/métricas) estiver rodando por um tempo coletando esses dados. Sem isso, qualquer modelo seria treinado no vácuo.

**Estudar, em ordem de complexidade crescente:**
1. Comece por ranking por heurística/regras (ex.: pontuação = recência + engajamento) — não é ML, mas já resolve boa parte do problema e te dá uma baseline para comparar depois.
2. Depois, *collaborative filtering* básico (recomendação por similaridade de comportamento entre usuários) é o próximo degrau natural.
3. Só then vale considerar modelos mais sofisticados — nessa ordem, não pulando direto para o fim.

---

## Resumo da ordem completa

| Fase | Foco | Por quê nessa posição |
|---|---|---|
| 0 | Refresh token → rate limit → criptografia → 2FA → recuperação de conta → login Google/Steam → troca de contas | Corrige risco de segurança ativo antes de ampliar a superfície de auth |
| 1 | Logs da aplicação → logs do banco → Prometheus/Grafana | Sem visibilidade, as próximas decisões (cache, split de serviços) são achismo |
| 2 | Redis (cache) → presença online/offline (+WebSocket) | Resolve escala, mas só depois de confirmar que existe um problema de escala |
| 3 | Split de microsserviços → Kafka → resiliência | Decisão mais cara de reverter; só faz sentido com dados da Fase 1 e um caso de uso real |
| 4 | Produto/UX | Baixo risco, pode intercalar a qualquer momento |
| 5 | ML no feed | Precisa dos dados que só a Fase 1, rodando por um tempo, vai ter gerado |
