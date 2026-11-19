-- Compatibiliza o schema inicial com a entidade Ata da Sprint 4.
ALTER TABLE atas
    ADD COLUMN participantes TEXT NULL AFTER pauta;
