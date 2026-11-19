-- Corrige o nome da coluna de bloqueio para corresponder à entidade Usuario.
ALTER TABLE usuarios
    RENAME COLUMN bloquado_ate TO bloqueado_ate;
