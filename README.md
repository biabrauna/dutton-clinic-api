# Clínica Dutton — Sistema de Gestão Clínica

> 🔗 **Demo ao vivo:** [dutton-clinic-interface2-gilt.vercel.app](https://dutton-clinic-interface2-gilt.vercel.app)  
> 🔗 **API (Railway):** [clinica-api-production-85b7.up.railway.app](https://clinica-api-production-85b7.up.railway.app/swagger-ui.html)  
> 🔗 **Frontend:** [github.com/biabrauna](https://github.com/biabrauna)

Sistema fullstack para gestão de uma clínica médica — desenvolvido como projeto prático com **Java 17 + Spring Boot** no backend e **React + TypeScript** no frontend, contemplando autenticação JWT, controle de acesso por papel (RBAC), testes automatizados, schema versionado com Flyway e deploy em Cloud.

![CI](https://github.com/biabrauna/clinicaDutton-api/actions/workflows/ci.yml/badge.svg)

---

## Stack

| Camada     | Tecnologia                                                           |
|------------|----------------------------------------------------------------------|
| Backend    | Java 17 · Spring Boot 2.7 · Spring Security · JWT (JJWT 0.11)      |
| Banco      | MySQL 8 · Flyway (migrations versionadas) · H2 (testes em memória)  |
| Frontend   | React 18 · TypeScript · Vite · Tailwind CSS · shadcn/ui             |
| Testes     | JUnit 5 · Mockito · @SpringBootTest · Vitest · Testing Library      |
| Infra      | Docker (multi-stage) · Docker Compose · GitHub Actions CI           |
| Cloud      | Railway (backend) · Vercel (frontend)                               |
| Docs       | SpringDoc / Swagger UI                                              |

---

## Funcionalidades

O sistema tem três perfis de acesso com fluxos independentes:

**ROOT (administração)** — cadastra médicos e pacientes no sistema.

**MÉDICO** — acessa agenda mensal com calendário de slots disponíveis, gerencia consultas (cancelar, marcar como realizada) e cria prontuários eletrônicos com queixa, diagnóstico, prescrição e plano de tratamento.

**PACIENTE** — visualiza suas consultas, agenda novas em um wizard de 5 passos (especialidade → médico → data → horário → confirmação) e consulta histórico.

---

## Arquitetura

```
clinicaDutton-api/                    frontend/ (repo separado)
├── controller/   HTTP layer          ├── pages/      por role
├── service/      regras de negócio   ├── services/   clientes axios
├── repository/   Spring Data JPA     ├── contexts/   AuthContext (JWT)
├── dto/          contratos da API    ├── components/ UI + ProtectedRoute
├── security/     JWT + RBAC          └── test/       Vitest + Testing Library
└── db/migration/ Flyway SQL
```

```
Request → Controller → Service → Repository → MySQL
                  ↓
             DTOs isolam o contrato da API das entidades JPA
```

### Decisões técnicas

**Stateless com JWT** — Cada requisição carrega o token no header `Authorization: Bearer`. O `JwtAuthFilter` intercepta, valida e injeta o `SecurityContext` antes do controller. Não há sessão no servidor — escala horizontalmente sem sticky session.

**RBAC granular no Spring Security** — Cada endpoint tem restrições de role configuradas no `SecurityConfig`. Ex: `POST /doctors` exige `ROOT` ou `MEDICO`; `DELETE /patients` exige `ROOT` ou `PACIENTE`. Testado com `@WithMockUser` nos testes de integração.

**Detecção de conflito de horário** — O `AppointmentService` chama `existsByDoctorIdAndScheduledAt` antes de salvar. Conflito retorna `409 Conflict` com mensagem descritiva. Coberto por teste unitário específico.

**Flyway + `baseline-on-migrate`** — Schema versionado em `db/migration/`. Permite adotar migrations em banco existente sem recriar do zero. Em produção o `ddl-auto` é `validate` — o Hibernate não toca no schema.

**H2 `MODE=MySQL` em testes** — Testes de integração rodam isolados em memória, sem depender de MySQL instalado. Flyway desabilitado no perfil `test`, Hibernate recria o schema via `create-drop`. Resultado: testes rápidos e determinísticos.

**Vite proxy → CORS zero-config em dev** — O frontend usa `baseURL: ''` no axios. Em desenvolvimento, o Vite encaminha `/auth`, `/doctors`, etc. para `localhost:8080`. Em produção (Vercel + Railway), o frontend chama a URL direta com CORS configurado no backend.

---

## Testes

```bash
# Backend: unitários (Mockito) + integração (@SpringBootTest + H2)
./mvnw test

# Frontend: componentes + serviços (Vitest + Testing Library)
cd frontend && npm test
```

**Cobertura backend:**
- `DoctorServiceTest` — 8 casos: findAll com/sem filtro, findById (found/not found), create, update, delete, getSpecialties
- `AppointmentServiceTest` — 7 casos: agendamento sem conflito, **conflito de horário** (lança exceção), médico/paciente inexistente, slots disponíveis excluindo domingos e horários ocupados
- `DoctorControllerTest` — integração com H2: GET 200/404, POST 201/400/403 por role, DELETE 204

**Cobertura frontend:**
- `LoginPage.test.tsx` — render, erro de login, chamada com dados corretos, loading state, toggle senha
- `ProtectedRoute.test.tsx` — 6 casos de RBAC: acesso permitido, redirect para /login, redirect por role incorreto

---

## Segurança

- Senhas armazenadas com **BCrypt** (fator 10)
- JWT assinado com **HMAC-SHA256**, expiração configurável via `jwt.expiration-ms`
- `JwtAuthFilter` rejeita tokens expirados ou inválidos com `401`
- Rotas públicas restritas a `/auth/**`, `/actuator/health` e Swagger
- Dockerfile usa **usuário não-root** (`adduser clinica`)
- Secrets injetados via variáveis de ambiente — nunca hardcoded no código

---

## Banco de dados

Schema gerenciado pelo Flyway com duas migrations versionadas:

- `V1__initial_schema.sql` — tabelas `users`, `CadastroMed`, `CadastroPac`, `Consulta`, `prontuario` com FKs e índices de performance
- `V2__seed_data.sql` — dados de demo (médicos, pacientes, usuário ROOT)

```sql
-- Exemplo: índices criados para performance nas consultas mais frequentes
CREATE INDEX idx_consulta_doctor_status ON Consulta(doctor_id, status);
CREATE INDEX idx_prontuario_patient     ON prontuario(patient_id);
```

---

## API — principais endpoints

| Método | Rota                            | Roles permitidos       | Descrição                         |
|--------|---------------------------------|------------------------|-----------------------------------|
| POST   | `/auth/login`                   | público                | Retorna JWT                       |
| POST   | `/auth/register`                | público                | Cria usuário MEDICO ou PACIENTE   |
| GET    | `/doctors`                      | todos autenticados     | Lista paginada com filtro opcional|
| GET    | `/doctors/specialties`          | todos autenticados     | Lista especialidades distintas    |
| GET    | `/doctors/{id}/available-slots` | todos autenticados     | Slots livres por mês (seg–sáb 8–19h) |
| GET    | `/doctors/{id}/schedule`        | todos autenticados     | Agenda mensal paginada            |
| POST   | `/doctors`                      | ROOT, MEDICO           | Cadastra médico                   |
| GET    | `/patients`                     | todos autenticados     | Lista pacientes                   |
| POST   | `/patients`                     | ROOT, PACIENTE         | Cadastra paciente                 |
| GET    | `/appointments`                 | todos autenticados     | Lista consultas paginada          |
| POST   | `/appointments`                 | todos autenticados     | Agenda consulta (valida conflito) |
| PUT    | `/appointments/{id}`            | todos autenticados     | Atualiza status / dados           |
| GET    | `/patients/{id}/records`        | todos autenticados     | Prontuários do paciente           |
| POST   | `/patients/{id}/records`        | ROOT, MEDICO           | Cria prontuário                   |

Documentação interativa completa: [`/swagger-ui.html`](https://clinica-api-production-85b7.up.railway.app/swagger-ui.html)

---

## Docker

```bash
# Apenas backend + banco
docker compose up --build

# Backend acessível em http://localhost:8080
# Swagger em        http://localhost:8080/swagger-ui.html
```

O `Dockerfile` usa **multi-stage build**:
1. `maven:3.9` compila e gera o `.jar`
2. `eclipse-temurin:17-jre-alpine` copia apenas o artefato — imagem final ~180MB

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS builder
RUN mvn package -DskipTests -q          # dependências em cache separado

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S clinica && adduser -S clinica -G clinica   # não-root
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
```

---

## CI/CD — GitHub Actions

Pipeline em `.github/workflows/ci.yml` — executa em todo push/PR para `main` e `develop`:

```
push → [backend job]  → mvn test (H2, perfil test)
     → [frontend job] → tsc --noEmit + npm test + npm run build
     → [docker job]   → docker build (somente main, após jobs passarem)
```

---

## Como rodar localmente

```bash
# Requer Java 17, Maven 3.9 e MySQL 8 em localhost:3306/clinica
./mvnw spring-boot:run

# Frontend (repositório separado)
npm install && npm run dev
# → http://localhost:5173
```

**Variáveis de ambiente:**

| Variável            | Descrição                           | Padrão dev              |
|---------------------|-------------------------------------|-------------------------|
| `DATABASE_URL`      | JDBC URL do MySQL                   | `localhost:3306/clinica`|
| `DATABASE_USERNAME` | Usuário do banco                    | `root`                  |
| `DATABASE_PASSWORD` | Senha do banco                      | —                       |
| `JWT_SECRET`        | Chave HMAC-SHA256 (mín. 32 chars)   | valor inseguro de dev   |
| `PORT`              | Porta HTTP                          | `8080`                  |

**Credenciais de demo** (seed `V2__seed_data.sql`):

| Role     | E-mail                       | Senha    |
|----------|------------------------------|----------|
| ROOT     | root@clinicadutton.com       | senha123 |
| MEDICO   | ana.lima@clinicadutton.com   | senha123 |
| PACIENTE | joao@email.com               | senha123 |

---

## Deploy

| Serviço  | Plataforma | URL                                                      |
|----------|-----------|-----------------------------------------------------------|
| Backend  | Railway   | https://clinica-api-production-85b7.up.railway.app       |
| Frontend | Vercel    | https://dutton-clinic-interface2-gilt.vercel.app          |

Deploy automático a partir da branch `main` em ambas as plataformas.

---

## Autora

**Ana Beatriz Brauna** — Engenharia de Software  
[github.com/biabrauna](https://github.com/biabrauna)
