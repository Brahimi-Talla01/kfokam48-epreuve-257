-- =============================================================================
-- V1__init.sql — schéma KFOKAM48
-- Conforme à docs/diagrammes/D2-modele-de-donnees.md (D2 ≡ migrations, étape 1).
-- Règle B5 : après commit, ce fichier ne change plus — on n'ajoute que V3__, V4__…
-- DDL volontairement portable PostgreSQL / H2 (mode PostgreSQL) pour que les tests
-- tournent sur un poste vierge (B6).
-- =============================================================================

CREATE TABLE promotion (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom        VARCHAR(120) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_promotion_nom UNIQUE (nom)
);

CREATE TABLE etudiant (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom          VARCHAR(160) NOT NULL,
    promotion_id BIGINT       NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_etudiant_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id)
);

CREATE TABLE session (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titre         VARCHAR(200) NOT NULL,
    promotion_id  BIGINT       NOT NULL,
    code          VARCHAR(16)  NOT NULL,
    statut        VARCHAR(16)  NOT NULL,
    ouverture_at  TIMESTAMP    NOT NULL,
    expiration_at TIMESTAMP    NOT NULL,
    cloture_at    TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_session_code   UNIQUE (code),
    CONSTRAINT ck_session_statut CHECK (statut IN ('OUVERTE', 'CLOTUREE')),
    CONSTRAINT ck_session_dates  CHECK (expiration_at > ouverture_at),
    CONSTRAINT fk_session_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id)
);

-- RG14 : une présence par (étudiant, session) → 409 DEJA_PRESENT.
CREATE TABLE presence (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id BIGINT       NOT NULL,
    etudiant_id BIGINT      NOT NULL,
    source     VARCHAR(16)  NOT NULL,
    marque_le  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_presence_etudiant_session UNIQUE (session_id, etudiant_id),
    CONSTRAINT ck_presence_source CHECK (source IN ('ETUDIANT', 'FORMATEUR')),
    CONSTRAINT fk_presence_session  FOREIGN KEY (session_id)  REFERENCES session (id),
    CONSTRAINT fk_presence_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant (id)
);

-- Q13 : un lien, jamais un fichier — un dépôt par (étudiant, session).
CREATE TABLE exercice (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id    BIGINT       NOT NULL,
    etudiant_id   BIGINT       NOT NULL,
    lien          VARCHAR(500) NOT NULL,
    statut        VARCHAR(16)  NOT NULL,
    depose_le     TIMESTAMP    NOT NULL,
    mise_a_jour_le TIMESTAMP   NOT NULL,
    CONSTRAINT uk_exercice_etudiant_session UNIQUE (session_id, etudiant_id),
    CONSTRAINT ck_exercice_statut CHECK (statut IN ('EN_ATTENTE', 'RENDU')),
    CONSTRAINT fk_exercice_session  FOREIGN KEY (session_id)  REFERENCES session (id),
    CONSTRAINT fk_exercice_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant (id)
);

-- RG5 : un seul relecteur par exercice (uk_relecture_exercice) ; relecteur_id nullable
-- = aucun pair éligible à la session (cahier des charges §7.2).
CREATE TABLE relecture (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    exercice_id  BIGINT      NOT NULL,
    relecteur_id BIGINT,
    statut       VARCHAR(16) NOT NULL,
    note         INT,
    commentaire  VARCHAR(2000),
    rendue_le    TIMESTAMP,
    CONSTRAINT uk_relecture_exercice UNIQUE (exercice_id),
    CONSTRAINT ck_relecture_statut CHECK (statut IN ('EN_ATTENTE', 'RENDUE')),
    CONSTRAINT ck_relecture_note   CHECK (note IS NULL OR (note BETWEEN 0 AND 20)),
    CONSTRAINT fk_relecture_exercice  FOREIGN KEY (exercice_id)  REFERENCES exercice (id),
    CONSTRAINT fk_relecture_relecteur FOREIGN KEY (relecteur_id) REFERENCES etudiant (id)
);

-- ENF2 : tableau du formateur à moins de 2 s pour 60 étudiants.
CREATE INDEX idx_presence_session    ON presence (session_id);
CREATE INDEX idx_exercice_session    ON exercice (session_id);
CREATE INDEX idx_exercice_etudiant   ON exercice (etudiant_id);
CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);
CREATE INDEX idx_session_promotion   ON session (promotion_id);
