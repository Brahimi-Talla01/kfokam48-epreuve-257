-- =============================================================================
-- V3__deux_relecteurs.sql — évolution du besoin, étape 3 (ENVELOPPE §2)
-- « Un seul relecteur ça ne marche pas : à partir de maintenant, chaque exercice
-- est relu par deux pairs différents, et la note retenue est la moyenne des deux. »
-- Casse RG5 (issue de Q6), remplacée par RG18/RG19 (cahier §6, §7.3).
--
-- Règle B5 : V1 et V2 ne sont JAMAIS modifiées ; on ajoute une migration.
-- La contrainte n'interdisait qu'un seul relecteur PAR EXERCICE ; elle autorise
-- désormais jusqu'à deux relecteurs distincts par exercice (jamais le même deux
-- fois). Les données du seed (V2, un relecteur par exercice) restent valides sans
-- transformation : chaque ligne existante satisfait toujours la nouvelle contrainte.
-- =============================================================================

-- L'ancien index unique reste physiquement lié à la clé étrangère
-- fk_relecture_exercice même après avoir retiré la contrainte UNIQUE qui l'a créé :
-- on retire d'abord la clé étrangère, puis l'ancienne contrainte (et son index),
-- puis on recrée la clé étrangère — l'ordre inverse laisserait un index orphelin
-- qui continuerait à interdire un second relecteur.
ALTER TABLE relecture DROP CONSTRAINT fk_relecture_exercice;
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;

ALTER TABLE relecture
    ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
ALTER TABLE relecture
    ADD CONSTRAINT fk_relecture_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id);
