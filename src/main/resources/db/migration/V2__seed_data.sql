-- ============================================================
-- V2 — Dados iniciais para desenvolvimento e demo
-- Senha de todos os usuários: "senha123" (BCrypt)
-- ============================================================

-- Usuário ROOT (administrador do sistema)
INSERT INTO users (name, email, password, role) VALUES
('Administrador', 'root@clinicadutton.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2',
 'ROOT');

-- Médicos (usuários de acesso)
INSERT INTO users (name, email, password, role) VALUES
('Dra. Ana Lima',    'ana.lima@clinicadutton.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2', 'MEDICO'),
('Dr. Carlos Souza', 'carlos.souza@clinicadutton.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2', 'MEDICO'),
('Dra. Beatriz Melo','beatriz.melo@clinicadutton.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2', 'MEDICO');

-- Perfis de médicos (CadastroMed)
INSERT INTO CadastroMed (nome, email, especializacao, crm) VALUES
('Dra. Ana Lima',    'ana.lima@clinicadutton.com',    'Cardiologia',  'CRM/RJ 100001'),
('Dr. Carlos Souza', 'carlos.souza@clinicadutton.com','Neurologia',   'CRM/SP 200002'),
('Dra. Beatriz Melo','beatriz.melo@clinicadutton.com','Dermatologia', 'CRM/MG 300003');

-- Pacientes de exemplo
INSERT INTO CadastroPac (nome, telefone, dataNascimento, endereco, bairro, cep, UF) VALUES
('João da Silva',    '(21) 98001-0001', '1985-03-15', 'Rua das Flores, 100',     'Botafogo',   '22250-040', 'RJ'),
('Maria Oliveira',   '(21) 98001-0002', '1990-07-22', 'Av. Atlântica, 500',      'Copacabana', '22010-000', 'RJ'),
('Pedro Alves',      '(21) 98001-0003', '1978-11-05', 'Rua do Catete, 200',      'Catete',     '22220-000', 'RJ'),
('Lucia Ferreira',   '(11) 99001-0004', '2000-01-30', 'Av. Paulista, 1000',      'Bela Vista', '01310-100', 'SP');

-- Usuários pacientes (para login)
INSERT INTO users (name, email, password, role) VALUES
('João da Silva',  'joao@email.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2', 'PACIENTE'),
('Maria Oliveira', 'maria@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lOn2', 'PACIENTE');
