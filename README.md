# Clínica Dutton — API

API REST para gestão de uma clínica médica. Desenvolvida em **Java 17 + Spring Boot**, com autenticação JWT, controle de acesso por roles, prontuário eletrônico e agenda de médicos.

> Cliente desktop disponível em [Clinica-Dutton](https://github.com/biabrauna/Clinica-Dutton)

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
