CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    rg VARCHAR(30) NOT NULL,
    senha VARCHAR(100) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    primeiro_login BIT NOT NULL DEFAULT b'1',
    ativo BIT NOT NULL DEFAULT b'1',
    perfil VARCHAR (20) NULL,
    status_usuario VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    tentativas_falhas INT NOT NULL DEFAULT 0,
    bloquado_ate DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_rg UNIQUE (rg)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE atas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero_ata INT NOT NULL,
    data_reuniao DATE NOT NULL,
    liberada_para_alunos BIT NOT NULL DEFAULT b'0',
    pauta TEXT NOT NULL,
    usuario_id BIGINT NOT NULL,
    criado_em DATETIME(6) NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ATIVA',
    PRIMARY KEY (id),
    CONSTRAINT uk_atas_numero_ata UNIQUE (numero_ata),
    CONSTRAINT fk_atas_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE arquivos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome_original VARCHAR(255) NOT NULL,
    nome_armazenado VARCHAR(255) NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    criptografado BIT NOT NULL DEFAULT b'1',
    hash VARCHAR(64) NOT NULL,
    data_envio DATETIME(6) NOT NULL,
    ata_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_arquivos_nome_armazenado UNIQUE (nome_armazenado),
    CONSTRAINT uk_arquivos_ata_id UNIQUE (ata_id),
    CONSTRAINT fk_arquivos_ata
        FOREIGN KEY (ata_id) REFERENCES atas (id),
    CONSTRAINT fk_arquivos_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tokens_recuperacao_senha (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    expira_em DATETIME(6) NOT NULL,
    usado_em DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT idx_token_recuperacao_hash UNIQUE (token_hash),
    CONSTRAINT fk_tokens_recuperacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    INDEX idx_token_recuperacao_usuario (usuario_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;