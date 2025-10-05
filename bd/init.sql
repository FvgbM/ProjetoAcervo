CREATE DATABASE IF NOT EXISTS acervo_db;

USE acervo_db;

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(20) NOT NULL,
    nome VARCHAR(80),
    rg VARCHAR(11) UNIQUE,
    ativo BOOLEAN DEFAULT TRUE,
    papel ENUM('ALUNO', 'PROFESSOR', 'COLABORADOR') NOT NULL
);

CREATE TABLE IF NOT EXISTS arquivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_original VARCHAR(80) NOT NULL,
    nome_armazenado VARCHAR(80) NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_envio DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS ata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numeroAta INT NOT NULL,
    dataReuniao DATE NOT NULL,
    pauta VARCHAR(255) NOT NULL,
    participantes VARCHAR(255),
    criadoPor VARCHAR(80) NOT NULL,
    criadoEm DATE NOT NULL,
    status VARCHAR(50)
);

