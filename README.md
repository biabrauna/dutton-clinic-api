# Clínica Dutton — Sistema de Gestão Clínica

Sistema fullstack para gestão de uma clínica médica, com controle de acesso por papel (RBAC), agendamento de consultas e registro de prontuários eletrônicos.

![CI](https://github.com/biabrauna/clinicaDutton-api/actions/workflows/ci.yml/badge.svg)

---

## Stack

| Camada   | Tecnologia                                                      |
|----------|-----------------------------------------------------------------|
| Backend  | Java 17 · Spring Boot 2.7 · Spring Security · JWT (JJWT 0.11) |
| Banco    | MySQL 8 (prod) · H2 em memória (testes) · Flyway (migrations)  |
| Frontend | React 18 · TypeScript · Vite 5 · Tailwind CSS · shadcn/ui      |
| Infra    | Docker · Docker Compose · GitHub Actions CI                     |
| Docs API | SpringDoc / Swagger UI (`/swagger-ui.html`)                     |

---

## Arquitetura

```
clinicaDutton-api/
├── src/
│   ├── main/java/br/com/clinicah/
│   │   ├── controller/     # Camada HTTP (REST controllers)
│   │   ├── service/        # Regras de negócio
│   │   ├── repository/     # Acesso a dados (Spring Data JPA)
│   │   ├── model/          # Entidades JPA
│   │   ├── dto/            # Request/Response DTOs (sem expor entidades)
│   │   ├── security/       # JWT filter, UserDetailsService, SecurityConfig
│   │   └── exception/      # GlobalExceptionHandler + ResourceNotFoundException
│   └── resources/
│       └── db/migration/   # Scripts Flyway versionados
└── frontend/
    └── src/
        ├── pages/          # Páginas por role (admin / doctor / patient / auth)
        ├── services/       # Clientes axios por domínio (doctorService, etc.)
        ├── contexts/       # AuthContext (JWT decode, roles, IDs por sessão)
        ├── components/     # Componentes UI (shadcn/ui) + ProtectedRoute + Layout
        └── test/           # Vitest + Testing Library
```

### Decisões técnicas

**JWT sem refresh token** — Sessão de 24h com token único armazenado no `localStorage`. Para o escopo clínico atual, a simplicidade supera o custo de rotação. Em produção escalável, adicionaria `refresh_token` persistido em banco com TTL.

**Vite proxy em dev — CORS zero config** — O frontend usa `baseURL: ''` no axios, então todas as chamadas são relativas (`/auth`, `/doctors`). O Vite as encaminha para `localhost:8080`. Em produção, o nginx assume esse papel como proxy reverso — sem configuração de CORS por ambiente.

**H2 + `MODE=MySQL` nos testes** — Testes de integração rodam em memória. Flyway é desabilitado no perfil `test` e o Hibernate cria o schema via `create-drop`. Resultado: testes rápidos, isolados, sem dependência de MySQL instalado.

**Flyway com `baseline-on-migrate`** — Permite adotar migrations em um banco existente sem recriar o schema do zero. O Flyway registra o estado atual como baseline e só aplica versões posteriores.

**`PatientService` sem e-mail** — A entidade `Patient` não tem campo de e-mail (legado da modelagem original). O frontend contorna isso com uma tela de seleção de perfil ao primeiro acesso como `PACIENTE`.

---

## Roles e rotas

| Role      | Acesso                                                        |
|-----------|---------------------------------------------------------------|
| `ROOT`    | Cadastrar médicos e pacientes (`/admin`)                     |
| `MEDICO`  | Agenda mensal, consultas, criar prontuários (`/medico`)      |
| `PACIENTE`| Ver próprias consultas, agendar nova consulta (`/paciente`)  |

---

## Como rodar

### Com Docker (recomendado)

```bash
docker compose up --build

# Frontend:  http://localhost:5173
# Backend:   http://localhost:8080
# Swagger:   http://localhost:8080/swagger-ui.html
```

> O MySQL demora ~20s para ficar pronto. O backend aguarda automaticamente via `healthcheck`.

### Sem Docker (desenvolvimento local)

```bash
# Terminal 1 — Backend (requer MySQL em localhost:3306 com banco "clinica")
./mvnw spring-boot:run

# Terminal 2 — Frontend
cd frontend && npm install && npm run dev
# Acessa http://localhost:5173
```

### Credenciais de demo (seed V2)

| Role     | E-mail                       | Senha    |
|----------|------------------------------|----------|
| ROOT     | root@clinicadutton.com       | senha123 |
| MEDICO   | ana.lima@clinicadutton.com   | senha123 |
| PACIENTE | joao@email.com               | senha123 |

---

## Testes

```bash
# Backend — unitários (Mockito) + integração (@SpringBootTest + H2)
./mvnw test

# Frontend — componentes + serviços (Vitest + Testing Library)
cd frontend
npm test
npm run coverage   # relatório HTML em coverage/
```

---

## Endpoints principais

| Método | Rota                             | Roles                  | Descrição                      |
|--------|----------------------------------|------------------------|--------------------------------|
| POST   | `/auth/login`                    | público                | Autenticação → JWT             |
| POST   | `/auth/register`                 | público                | Cadastro de usuário            |
| GET    | `/doctors`                       | todos autenticados     | Listar médicos (paginado)      |
| GET    | `/doctors/specialties`           | todos autenticados     | Especialidades distintas       |
| GET    | `/doctors/{id}/available-slots`  | todos autenticados     | Horários livres por mês        |
| GET    | `/doctors/{id}/schedule`         | todos autenticados     | Agenda do médico por mês       |
| POST   | `/doctors`                       | ROOT, MEDICO           | Cadastrar médico               |
| GET    | `/patients`                      | todos autenticados     | Listar pacientes               |
| POST   | `/patients`                      | ROOT, PACIENTE         | Cadastrar paciente             |
| GET    | `/appointments`                  | todos autenticados     | Listar consultas               |
| POST   | `/appointments`                  | todos autenticados     | Agendar consulta               |
| PUT    | `/appointments/{id}`             | todos autenticados     | Atualizar status/dados         |
| GET    | `/patients/{id}/records`         | todos autenticados     | Prontuários do paciente        |
| POST   | `/patients/{id}/records`         | ROOT, MEDICO           | Criar prontuário               |

> Documentação completa interativa em `/swagger-ui.html`.

---

## CI/CD — GitHub Actions

Pipeline em `.github/workflows/ci.yml`, executado em push/PR para `main` e `develop`:

1. **backend** — compila + `mvn test` com perfil H2
2. **frontend** — `npm ci` + TypeScript check + `npm test` + `npm run build`
3. **docker** — build das imagens (somente `main`)

---

## Variáveis de ambiente (backend)

| Variável              | Padrão                         | Descrição               |
|-----------------------|--------------------------------|-------------------------|
| `DATABASE_URL`        | `jdbc:mysql://localhost/...`   | JDBC URL do MySQL       |
| `DATABASE_USERNAME`   | `root`                         | Usuário do banco        |
| `DATABASE_PASSWORD`   | `ifgoiano`                     | Senha do banco          |
| `JWT_SECRET`          | (padrão dev — trocar em prod!) | Chave HMAC-SHA256       |
| `PORT`                | `8080`                         | Porta do servidor       |

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 2.7 |
| Segurança | Spring Security + JWT (JJWT 0.11) |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | MySQL 8 (produção) |
| Validação | Bean Validation (Jakarta) |
| Documentação | Springdoc OpenAPI (Swagger UI) |
| Container | Docker (multi-stage build) |
| Deploy | Railway |

---

## Arquitetura

```
Controller  →  Service  →  Repository  →  Database
    ↑               ↑
   DTOs         Regras de
(Request/      negócio +
 Response)    Validações
```

- **Controllers**: recebem a requisição, delegam para o service, retornam o DTO de resposta
- **Services**: contêm toda a lógica de negócio (validação de conflito de horário, integridade de prontuário, etc.)
- **Repositories**: interfaces Spring Data JPA — sem SQL manual onde o JPA resolve
- **DTOs**: isolam o contrato da API das entidades JPA — o cliente nunca vê campos internos do banco

---

## Modelo de dados

```
Doctor ──────────────────────────────┐
  id, name, email, specialty, crm    │
                                     │
Patient ─────────────────────────────┤
  id, name, phone, birthDate,        │
  address, neighborhood, zipCode,    │
  state                              │
                                     ▼
                              Appointment
                          doctor_id → Doctor
                          patient_id → Patient
                          scheduledAt (LocalDateTime)
                          status: AGENDADA | CANCELADA | REALIZADA

                              MedicalRecord (Prontuário)
                          patient_id → Patient
                          doctor_id → Doctor
                          appointment_id → Appointment (opcional)
                          chiefComplaint, clinicalFindings,
                          diagnosis, treatmentPlan, prescription

User (autenticação)
  email, password (BCrypt), role: ROOT | MEDICO | PACIENTE
```

---

## Autenticação

A API usa **JWT stateless**. Todas as rotas (exceto `/auth/**` e Swagger) exigem o header:

```
Authorization: Bearer <token>
```

### Registrar usuário

```http
POST /auth/register
Content-Type: application/json

{
  "name": "Dr. Ana Lima",
  "email": "ana@clinica.com",
  "password": "senha123",
  "role": "MEDICO"
}
```

> Roles disponíveis: `MEDICO`, `PACIENTE`. O role `ROOT` não pode ser criado via API pública.

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "ana@clinica.com",
  "password": "senha123"
}
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

---

## Controle de acesso por role

| Endpoint | ROOT | MEDICO | PACIENTE |
|---|:---:|:---:|:---:|
| `GET /doctors/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /doctors/**` | ✅ | ✅ | ❌ |
| `GET /patients/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /patients/**` | ✅ | ❌ | ✅ |
| `GET /patients/*/records/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /patients/*/records/**` | ✅ | ✅ | ❌ |
| `/appointments/**` | ✅ | ✅ | ✅ |

---

## Endpoints

### Autenticação
```
POST /auth/register    cria usuário (MEDICO ou PACIENTE)
POST /auth/login       retorna JWT token
```

### Médicos
```
GET    /doctors?page=0&size=10&sort=name
GET    /doctors/{id}
POST   /doctors
PUT    /doctors/{id}
DELETE /doctors/{id}
GET    /doctors/{id}/schedule?year=2026&month=5   agenda mensal paginada
```

### Pacientes
```
GET    /patients?page=0&size=10&sort=name
GET    /patients/{id}
POST   /patients
PUT    /patients/{id}
DELETE /patients/{id}
```

### Consultas
```
GET    /appointments?page=0&size=10
GET    /appointments/{id}
POST   /appointments
PUT    /appointments/{id}
DELETE /appointments/{id}
```

> **Regra:** não é possível agendar uma consulta se o médico já tem outra no mesmo horário — retorna `409 Conflict`.

### Prontuário Eletrônico
```
GET    /patients/{patientId}/records
GET    /patients/{patientId}/records/{id}
POST   /patients/{patientId}/records
PUT    /patients/{patientId}/records/{id}
DELETE /patients/{patientId}/records/{id}
```

**Regras de negócio:**
- Se `appointmentId` informado, valida que a consulta pertence ao mesmo médico e paciente
- `recordDate` não pode ser no futuro
- Somente o médico que criou o prontuário pode editá-lo

---

## Rodando localmente

### Pré-requisitos
- Java 17+
- Maven 3.9+
- MySQL 8 rodando em `localhost:3306` com database `clinica`

### Variáveis de ambiente

Copie `.env.example` para `.env` e preencha:

```env
DATABASE_URL=jdbc:mysql://localhost:3306/clinica?useSSL=false&allowPublicKeyRetrieval=true
DATABASE_USERNAME=root
DATABASE_PASSWORD=sua_senha
JWT_SECRET=uma-chave-com-pelo-menos-32-caracteres-aqui!
PORT=8080
```

> O `JWT_SECRET` precisa ter no mínimo 32 caracteres (256 bits para HMAC-SHA256).

### Executar

```bash
mvn spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Rodando com Docker

```bash
# Build
docker build -t clinica-dutton-api .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://host.docker.internal:3306/clinica \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=sua_senha \
  -e JWT_SECRET=sua-chave-secreta-com-32-chars!! \
  clinica-dutton-api
```

O Dockerfile usa **multi-stage build**: Maven compila na primeira etapa, a segunda copia apenas o `.jar` para uma imagem JRE enxuta.

---

## Rodando com Docker Compose

```bash
docker compose up
```

```yaml
# docker-compose.yml
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:mysql://db:3306/clinica?useSSL=false&allowPublicKeyRetrieval=true
      DATABASE_USERNAME: root
      DATABASE_PASSWORD: clinica123
      JWT_SECRET: clinica-dutton-segredo-local-dev-32chars!
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: clinica123
      MYSQL_DATABASE: clinica
    ports:
      - "3306:3306"
```

---

## Rodando os testes

```bash
mvn test
```

---

## Estrutura do projeto

```
src/main/java/br/com/clinicah/
├── controller/
│   ├── AuthController.java
│   ├── DoctorController.java
│   ├── PatientController.java
│   ├── AppointmentController.java
│   └── MedicalRecordController.java
├── service/
│   ├── DoctorService.java
│   ├── PatientService.java
│   ├── AppointmentService.java
│   └── MedicalRecordService.java
├── repository/
├── model/
│   ├── Doctor.java, Patient.java, Appointment.java
│   ├── MedicalRecord.java
│   ├── User.java, Role.java
│   └── AppointmentStatus.java
├── dto/                    ← contratos da API (Request / Response)
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── SecurityConfig.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── ErrorResponse.java
```

---

## Variáveis de ambiente

| Variável | Descrição | Default (dev) |
|---|---|---|
| `DATABASE_URL` | JDBC URL do MySQL | `localhost:3306/clinica` |
| `DATABASE_USERNAME` | Usuário do banco | `root` |
| `DATABASE_PASSWORD` | Senha do banco | — |
| `JWT_SECRET` | Chave de assinatura JWT (mín. 32 chars) | valor local inseguro |
| `PORT` | Porta do servidor | `8080` |

> **Nunca commite `.env` ou credenciais reais.** Adicione `.env` ao `.gitignore`.

---

## Deploy (Railway)

A API está publicada no Railway com deploy automático a partir da branch `main`. As variáveis de ambiente estão configuradas no painel do Railway.

---

## Autora

**Ana Beatriz Brauna** — Engenharia de Software  
[GitHub](https://github.com/biabrauna)
