-- ============================================================
-- WASTEWISE TN - Schéma complet de la base de données
-- Base : pidev
-- ============================================================

CREATE DATABASE IF NOT EXISTS pidev
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pidev;

-- ============================================================
-- 1. TABLE USER
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    email                       VARCHAR(180) NOT NULL UNIQUE,
    password                    VARCHAR(255) NOT NULL,
    roles                       VARCHAR(100) DEFAULT 'CITIZEN',
    nom                         VARCHAR(100),
    prenom                      VARCHAR(100),
    telephone                   VARCHAR(20),
    type                        VARCHAR(50) DEFAULT 'CITIZEN',
    created_at                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active                   BOOLEAN DEFAULT TRUE,
    is_verified                 BOOLEAN DEFAULT FALSE,
    face_embedding              LONGTEXT,
    face_updated_at             DATETIME,
    last_seen_at                DATETIME,
    google_authenticator_secret VARCHAR(100),
    is_two_factor_enabled       BOOLEAN DEFAULT FALSE,
    -- Champs étendus (gestion-user branch)
    adresse                     VARCHAR(255),
    photo_profil                VARCHAR(255),
    notify_validation           BOOLEAN DEFAULT TRUE,
    notify_points               BOOLEAN DEFAULT TRUE,
    notify_refus                BOOLEAN DEFAULT TRUE,
    notify_nouvelles_declarations BOOLEAN DEFAULT TRUE,
    langue                      VARCHAR(10) DEFAULT 'fr',
    theme                       VARCHAR(20) DEFAULT 'light',
    unite_preferee              VARCHAR(10) DEFAULT 'kg',
    date_inscription            DATETIME DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion          DATETIME,
    statut_centre               VARCHAR(50),
    capacite_max_journaliere    DECIMAL(10,2),
    organisation_centre         VARCHAR(255),
    zone_couverture             VARCHAR(255),
    types_dechets_acceptes      TEXT,
    stripe_connect_account_id   VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. TABLE TYPE_DECHET
-- ============================================================
CREATE TABLE IF NOT EXISTS type_dechet (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    libelle           VARCHAR(100) NOT NULL,
    valeur_points_kg  DOUBLE DEFAULT 0,
    description_tri   TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. TABLE DECLARATION_DECHET
-- ============================================================
CREATE TABLE IF NOT EXISTS declaration_dechet (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    description                 TEXT,
    statut                      VARCHAR(50) DEFAULT 'EN_ATTENTE',
    type_dechet_id              INT,
    photo                       VARCHAR(255),
    latitude                    DOUBLE,
    longitude                   DOUBLE,
    quantite                    DOUBLE,
    unite                       VARCHAR(20) DEFAULT 'kg',
    created_at                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    score_ia                    DOUBLE,
    points_attribues            INT DEFAULT 0,
    qr_code                     VARCHAR(255),
    citoyen_id                  INT,
    valorisateur_confirmateur_id INT,
    date_confirmation           DATETIME,
    statut_historique           TEXT,
    deleted_at                  DATETIME,
    FOREIGN KEY (type_dechet_id) REFERENCES type_dechet(id) ON DELETE SET NULL,
    FOREIGN KEY (citoyen_id) REFERENCES `user`(id) ON DELETE SET NULL,
    FOREIGN KEY (valorisateur_confirmateur_id) REFERENCES `user`(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. TABLE WALLET
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet (
    id_wallet       INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT UNIQUE,
    solde_actuel    INT DEFAULT 0,
    date_mj         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. TABLE WALLET_TRANSACTION
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id_transaction  INT AUTO_INCREMENT PRIMARY KEY,
    wallet_id       INT,
    montant         INT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    motif           VARCHAR(255),
    date_transaction DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallet(id_wallet) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. TABLE BADGE_PARTENAIRE
-- ============================================================
CREATE TABLE IF NOT EXISTS badge_partenaire (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    partenaire_id   INT,
    code            VARCHAR(50) UNIQUE,
    nom             VARCHAR(100) NOT NULL,
    description     TEXT,
    couleur         VARCHAR(20),
    icone           VARCHAR(255),
    score_impact    INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_current      BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. TABLE BON_ACHAT
-- ============================================================
CREATE TABLE IF NOT EXISTS bon_achat (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    partenaire_id               INT,
    nom_magasin                 VARCHAR(150) NOT NULL,
    logo_magasin                VARCHAR(255),
    description                 TEXT,
    valeur_monetaire            DOUBLE,
    points_requis               INT DEFAULT 0,
    date_debut                  DATE,
    date_expiration             DATE,
    nombre_maximum_utilisations INT,
    nombre_utilisations         INT DEFAULT 0,
    conditions_utilisation      TEXT,
    zone_geographique           VARCHAR(255),
    image_promotionnelle        VARCHAR(255),
    statut                      VARCHAR(30) DEFAULT 'ACTIF',
    historique_modifications    TEXT,
    created_at                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. TABLE INDICATEUR_IMPACT
-- ============================================================
CREATE TABLE IF NOT EXISTS indicateur_impact (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    total_kg_recoltes   DOUBLE NOT NULL DEFAULT 0,
    co2_evite           DOUBLE NOT NULL DEFAULT 0,
    date_calcul         DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. TABLE ZONE_POLLUEE
-- ============================================================
CREATE TABLE IF NOT EXISTS zone_polluee (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nom_zone            VARCHAR(150) NOT NULL,
    coordonnees_gps     VARCHAR(100),
    niveau_pollution    INT NOT NULL CHECK (niveau_pollution BETWEEN 1 AND 10),
    date_identification DATETIME DEFAULT CURRENT_TIMESTAMP,
    indicateur_id       INT,
    FOREIGN KEY (indicateur_id) REFERENCES indicateur_impact(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 10. TABLE QRSCAN
-- ============================================================
CREATE TABLE IF NOT EXISTS qrscan (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    zone_id     INT,
    scanned_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address  VARCHAR(50),
    country     VARCHAR(100),
    FOREIGN KEY (zone_id) REFERENCES zone_polluee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 11. TABLE EVENEMENT
-- ============================================================
CREATE TABLE IF NOT EXISTS evenement (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    dateHeure       DATETIME,
    lieu            VARCHAR(200),
    nomOrganisateur VARCHAR(150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 12. TABLE PARTICIPATION
-- ============================================================
CREATE TABLE IF NOT EXISTS participation (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    dateInscription DATE DEFAULT (CURRENT_DATE),
    evenement_id    INT,
    nomCitoyen      VARCHAR(150),
    email           VARCHAR(180),
    FOREIGN KEY (evenement_id) REFERENCES evenement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 13. TABLE RESET_PASSWORD_TOKEN
-- ============================================================
CREATE TABLE IF NOT EXISTS reset_password_token (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  DATETIME NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    used_at     DATETIME,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- DONNÉES DE TEST - Utilisateur admin par défaut
-- Mot de passe : admin123 (hashé avec BCrypt)
-- ============================================================
INSERT INTO `user` (email, password, roles, nom, prenom, type, is_active, is_verified)
VALUES (
    'admin@wastewise.tn',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'Admin',
    'WasteWise',
    'ADMIN',
    TRUE,
    TRUE
);

-- Types de déchets par défaut
INSERT INTO type_dechet (libelle, valeur_points_kg, description_tri) VALUES
('Plastique',     10.0, 'Bouteilles, sacs, emballages plastiques'),
('Papier/Carton', 5.0,  'Journaux, cartons, papiers'),
('Verre',         8.0,  'Bouteilles, bocaux en verre'),
('Métal',         12.0, 'Canettes, boîtes de conserve'),
('Organique',     3.0,  'Déchets alimentaires, végétaux'),
('Électronique',  20.0, 'Appareils électroniques usagés');
