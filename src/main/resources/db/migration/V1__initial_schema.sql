-- ============================================================
-- V1 — Schema inicial da Clínica Dutton
-- Autor: Clínica Dutton
-- Data: 2026-01-01
-- ============================================================

-- Usuários de autenticação (todas as roles)
CREATE TABLE IF NOT EXISTS users (
    id       INT          NOT NULL AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('ROOT','MEDICO','PACIENTE') NOT NULL,
    PRIMARY KEY (id)
);

-- Cadastro de médicos
CREATE TABLE IF NOT EXISTS CadastroMed (
    cli_cod        INT          NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(100) NOT NULL,
    email          VARCHAR(150),
    especializacao VARCHAR(100) NOT NULL,
    crm            VARCHAR(30)  NOT NULL,
    PRIMARY KEY (cli_cod)
);

-- Cadastro de pacientes
CREATE TABLE IF NOT EXISTS CadastroPac (
    id             INT          NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(100) NOT NULL,
    telefone       VARCHAR(20),
    dataNascimento DATE,
    endereco       VARCHAR(200),
    complemento    VARCHAR(100),
    bairro         VARCHAR(100),
    cep            VARCHAR(10),
    UF             VARCHAR(2),
    PRIMARY KEY (id)
);

-- Consultas médicas
CREATE TABLE IF NOT EXISTS Consulta (
    id           INT         NOT NULL AUTO_INCREMENT,
    doctor_id    INT         NOT NULL,
    patient_id   INT         NOT NULL,
    scheduled_at DATETIME    NOT NULL,
    status       ENUM('AGENDADA','CANCELADA','REALIZADA') NOT NULL DEFAULT 'AGENDADA',
    notes        VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_consulta_medico   FOREIGN KEY (doctor_id)  REFERENCES CadastroMed(cli_cod),
    CONSTRAINT fk_consulta_paciente FOREIGN KEY (patient_id) REFERENCES CadastroPac(id),
    UNIQUE KEY uk_doctor_slot (doctor_id, scheduled_at)
);

-- Prontuários médicos
CREATE TABLE IF NOT EXISTS prontuario (
    id               INT          NOT NULL AUTO_INCREMENT,
    patient_id       INT          NOT NULL,
    doctor_id        INT          NOT NULL,
    appointment_id   INT,
    record_date      DATETIME     NOT NULL,
    queixa_principal VARCHAR(1000) NOT NULL,
    exame_clinico    VARCHAR(2000),
    diagnostico      VARCHAR(1000),
    plano_tratamento VARCHAR(2000),
    prescricao       VARCHAR(2000),
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_prontuario_paciente    FOREIGN KEY (patient_id)     REFERENCES CadastroPac(id),
    CONSTRAINT fk_prontuario_medico      FOREIGN KEY (doctor_id)      REFERENCES CadastroMed(cli_cod),
    CONSTRAINT fk_prontuario_consulta    FOREIGN KEY (appointment_id) REFERENCES Consulta(id)
);

-- Índices de performance
CREATE INDEX idx_consulta_doctor_status ON Consulta(doctor_id, status);
CREATE INDEX idx_consulta_patient       ON Consulta(patient_id);
CREATE INDEX idx_prontuario_patient     ON prontuario(patient_id);
CREATE INDEX idx_prontuario_doctor      ON prontuario(doctor_id);
