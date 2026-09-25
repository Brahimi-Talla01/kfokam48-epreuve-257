-- =============================================================================
-- V2__seed_demo.sql — jeu de données de démonstration (ENF4 / démarrage)
-- Charge une promotion de 8 étudiants, 2 sessions (une ouverte, une clôturée),
-- des présences, des exercices et des relectures — pour que le correcteur ne
-- tombe jamais sur une application vide.
--
-- Portabilité : aucun identifiant explicite (les clés sont obtenues par sous-requête),
-- ce qui évite de désynchroniser les séquences identité et permet au seed de tourner
-- aussi bien sur PostgreSQL que sur H2 en mode PostgreSQL (tests).
--
-- À noter : le code de la session de démonstration expire dans 4 h et non 15 min,
-- afin de rester utilisable pendant une relecture complète de l'application.
-- RG1 (15 min) s'applique à toute session créée par l'application — voir README.
-- =============================================================================

-- ---------------------------------------------------------------- promotions --
INSERT INTO promotion (nom, created_at) VALUES ('KFOKAM48 Promotion A', CURRENT_TIMESTAMP);
INSERT INTO promotion (nom, created_at) VALUES ('KFOKAM48 Promotion B', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------- étudiants --
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Koffi Aya', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Traore Ibrahim', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Diabate Fatoumata', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Yao Mariam', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Kouassi Serge', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Nguessan Eric', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Bamba Awa', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Gnamien Marc', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion A';
INSERT INTO etudiant (nom, promotion_id, created_at)
SELECT 'Ouattara Salif', id, CURRENT_TIMESTAMP FROM promotion WHERE nom = 'KFOKAM48 Promotion B';

-- ----------------------------------------------------------------- sessions --
-- Session en cours : code de démonstration K7F2M48 (voir l'en-tête du fichier).
INSERT INTO session (titre, promotion_id, code, statut, ouverture_at, expiration_at, cloture_at, created_at)
SELECT 'Atelier - Architecture d application',
       id,
       'K7F2M48',
       'OUVERTE',
       CURRENT_TIMESTAMP - INTERVAL '2' MINUTE,
       CURRENT_TIMESTAMP + INTERVAL '4' HOUR,
       NULL,
       CURRENT_TIMESTAMP
  FROM promotion WHERE nom = 'KFOKAM48 Promotion A';

-- Session terminée et clôturée : alimente l'historique et le tableau.
INSERT INTO session (titre, promotion_id, code, statut, ouverture_at, expiration_at, cloture_at, created_at)
SELECT 'Cours - Introduction au SQL',
       id,
       'SQL2026',
       'CLOTUREE',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY,
       CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '15' MINUTE,
       CURRENT_TIMESTAMP - INTERVAL '1' DAY,
       CURRENT_TIMESTAMP - INTERVAL '2' DAY
  FROM promotion WHERE nom = 'KFOKAM48 Promotion A';

-- --------------------------------------------------------------- présences --
-- Session clôturée : les 8 étudiants de la promotion étaient présents.
INSERT INTO presence (session_id, etudiant_id, source, marque_le)
SELECT s.id, e.id, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '2' DAY
  FROM session s
  JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'SQL2026'
   AND e.nom IN ('Koffi Aya', 'Traore Ibrahim', 'Diabate Fatoumata', 'Yao Mariam',
                 'Kouassi Serge', 'Nguessan Eric', 'Bamba Awa', 'Gnamien Marc');

-- Session ouverte : 5 présents par code, 1 relevée manuellement par le formateur (RG13 / Q14).
INSERT INTO presence (session_id, etudiant_id, source, marque_le)
SELECT s.id, e.id, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '2' MINUTE
  FROM session s
  JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'K7F2M48'
   AND e.nom IN ('Koffi Aya', 'Traore Ibrahim', 'Diabate Fatoumata',
                 'Yao Mariam', 'Kouassi Serge');

INSERT INTO presence (session_id, etudiant_id, source, marque_le)
SELECT s.id, e.id, 'FORMATEUR', CURRENT_TIMESTAMP - INTERVAL '1' MINUTE
  FROM session s
  JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'K7F2M48' AND e.nom = 'Nguessan Eric';

-- --------------------------------------------------------------- exercices --
-- Session clôturée : 4 dépôts.
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-sql/koffi-aya', 'RENDU',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'SQL2026' AND e.nom = 'Koffi Aya';
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-sql/traore-ibrahim', 'RENDU',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'SQL2026' AND e.nom = 'Traore Ibrahim';
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-sql/diabate-fatoumata', 'EN_ATTENTE',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'SQL2026' AND e.nom = 'Diabate Fatoumata';
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-sql/yao-mariam', 'EN_ATTENTE',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'SQL2026' AND e.nom = 'Yao Mariam';

-- Session ouverte : 3 dépôts en attente de relecture.
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-api/koffi-aya', 'EN_ATTENTE',
       CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'K7F2M48' AND e.nom = 'Koffi Aya';
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-api/traore-ibrahim', 'EN_ATTENTE',
       CURRENT_TIMESTAMP - INTERVAL '9' MINUTE, CURRENT_TIMESTAMP - INTERVAL '9' MINUTE
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'K7F2M48' AND e.nom = 'Traore Ibrahim';
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_le, mise_a_jour_le)
SELECT s.id, e.id, 'https://github.com/kfokam48/tp-api/diabate-fatoumata', 'EN_ATTENTE',
       CURRENT_TIMESTAMP - INTERVAL '8' MINUTE, CURRENT_TIMESTAMP - INTERVAL '8' MINUTE
  FROM session s JOIN etudiant e ON e.promotion_id = s.promotion_id
 WHERE s.code = 'K7F2M48' AND e.nom = 'Diabate Fatoumata';

-- -------------------------------------------------------------- relectures --
-- Session clôturée : 2 notes rendues (RG8 : entier 0-20), 2 en attente (RG10).
INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'RENDUE', 15, 'Requêtes correctes, pensez a indexer la colonne filtre.',
       CURRENT_TIMESTAMP - INTERVAL '20' HOUR
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'SQL2026'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Koffi Aya'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Traore Ibrahim';

INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'RENDUE', 12, 'Le JOIN manque : la requete renvoie des doublons.',
       CURRENT_TIMESTAMP - INTERVAL '19' HOUR
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'SQL2026'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Traore Ibrahim'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Diabate Fatoumata';

INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'EN_ATTENTE', NULL, NULL, NULL
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'SQL2026'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Diabate Fatoumata'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Yao Mariam';

INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'EN_ATTENTE', NULL, NULL, NULL
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'SQL2026'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Yao Mariam'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Koffi Aya';

-- Session ouverte : 3 relectures affectées, toutes encore en attente.
INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'EN_ATTENTE', NULL, NULL, NULL
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'K7F2M48'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Koffi Aya'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Yao Mariam';

INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, r.id, 'EN_ATTENTE', NULL, NULL, NULL
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'K7F2M48'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Traore Ibrahim'
  JOIN etudiant r ON r.promotion_id = a.promotion_id AND r.nom = 'Kouassi Serge';

INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, rendue_le)
SELECT x.id, NULL, 'EN_ATTENTE', NULL, NULL, NULL
  FROM exercice x
  JOIN session s ON s.id = x.session_id AND s.code = 'K7F2M48'
  JOIN etudiant a ON a.id = x.etudiant_id AND a.nom = 'Diabate Fatoumata'
 WHERE NOT EXISTS (SELECT 1 FROM relecture rl WHERE rl.exercice_id = x.id);
