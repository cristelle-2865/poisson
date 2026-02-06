-- BDD.sql
-- 1. Table des races de poissons
CREATE TABLE RacePoisson (
    id_RacePoisson SERIAL PRIMARY KEY,
    nom_RacePoisson VARCHAR(100) UNIQUE NOT NULL,
    description_RacePoisson TEXT,
    date_Creation_RacePoisson TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_Actif_RacePoisson BOOLEAN DEFAULT TRUE
);

-- 2. Table des aliments
CREATE TABLE Aliment (
    id_Aliment SERIAL PRIMARY KEY,
    nom_Aliment VARCHAR(100) UNIQUE NOT NULL,
    prix_Kg_Aliment DECIMAL(10,2) NOT NULL CHECK (prix_Kg_Aliment > 0),
    proteines_Par_Kg_Aliment DECIMAL(8,2) NOT NULL CHECK (proteines_Par_Kg_Aliment >= 0),
    glucides_Par_Kg_Aliment DECIMAL(8,2) NOT NULL CHECK (glucides_Par_Kg_Aliment >= 0),
    stock_Aliment DECIMAL(10,2) DEFAULT 0 CHECK (stock_Aliment >= 0),
    seuil_Minimum_Aliment DECIMAL(10,2) DEFAULT 10,
    est_Actif_Aliment BOOLEAN DEFAULT TRUE,
    date_Creation_Aliment TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_Modification_Aliment TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Table des piscines/bassins (CRITIQUE - AJOUTER EN PREMIER)
CREATE TABLE Piscine (
    id_Piscine SERIAL PRIMARY KEY,
    nom_Piscine VARCHAR(100) NOT NULL,
    capacite_Max_Piscine INT NOT NULL CHECK (capacite_Max_Piscine > 0),
    description_Piscine TEXT,
    est_Active_Piscine BOOLEAN DEFAULT TRUE,
    date_Creation_Piscine TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    volume_Piscine DECIMAL(10,2),
    temperature_Piscine DECIMAL(5,2),
    ph_Piscine DECIMAL(4,2)
);

-- 4. Table des poissons (MODIFIÉE - ajout colonne piscine)
CREATE TABLE Poisson (
    id_Poisson SERIAL PRIMARY KEY,
    id_RacePoisson INT NOT NULL REFERENCES RacePoisson(id_RacePoisson),
    id_Piscine_Actuel INT REFERENCES Piscine(id_Piscine) ON DELETE SET NULL,
    nom_Poisson VARCHAR(100) NOT NULL,
    prix_Achat_Poisson DECIMAL(10,2) NOT NULL CHECK (prix_Achat_Poisson > 0),
    prix_Vente_Poisson DECIMAL(10,2) NOT NULL CHECK (prix_Vente_Poisson > 0),
    poids_Maximal_Poisson DECIMAL(8,2) NOT NULL CHECK (poids_Maximal_Poisson > 0),
    capacite_Augmentation_Poisson DECIMAL(8,2) NOT NULL CHECK (capacite_Augmentation_Poisson > 0),
    poids_Initial_Poisson DECIMAL(8,2) NOT NULL CHECK (poids_Initial_Poisson > 0),
    poids_Actuel_Poisson DECIMAL(8,2) NOT NULL DEFAULT 0,
    date_Arrivee_Poisson DATE NOT NULL,
    date_Vente_Poisson DATE,
    est_Rassasie_Poisson BOOLEAN DEFAULT FALSE,
    est_Vendu_Poisson BOOLEAN DEFAULT FALSE,
    est_EnVie_Poisson BOOLEAN DEFAULT TRUE,
    date_Creation_Poisson TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_Modification_Poisson TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_Prix_Vente_Superieur CHECK (prix_Vente_Poisson > prix_Achat_Poisson),
    CONSTRAINT chk_Poids_Actuel CHECK (poids_Actuel_Poisson <= poids_Maximal_Poisson),
    CONSTRAINT chk_Poids_Initial CHECK (poids_Initial_Poisson <= poids_Maximal_Poisson),
    CONSTRAINT chk_Dates CHECK (date_Vente_Poisson IS NULL OR date_Vente_Poisson >= date_Arrivee_Poisson)
);

-- 5. Table d'affectation des poissons aux piscines (historique)
CREATE TABLE AffectationPiscine (
    id_AffectationPiscine SERIAL PRIMARY KEY,
    id_Piscine INT NOT NULL REFERENCES Piscine(id_Piscine),
    id_Poisson INT NOT NULL REFERENCES Poisson(id_Poisson),
    date_Entree_Affectation DATE NOT NULL DEFAULT CURRENT_DATE,
    date_Sortie_Affectation DATE,
    raison_Sortie_Affectation VARCHAR(50),
    date_Creation_Affectation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaires_Affectation TEXT,
    
    CONSTRAINT chk_Date_Sortie CHECK (date_Sortie_Affectation IS NULL OR date_Sortie_Affectation >= date_Entree_Affectation)
);

-- 6. TABLE CRITIQUE : Historique de nourrissage et croissance (fisakafoanana)
CREATE TABLE Fisakafoanana (
    id_Fisakafoanana SERIAL PRIMARY KEY,
    id_Poisson INT NOT NULL REFERENCES Poisson(id_Poisson),
    date_Nourrissage_Fisakafoanana DATE NOT NULL,
    heure_Nourrissage_Fisakafoanana TIME DEFAULT CURRENT_TIME,
    ancien_Poids_Fisakafoanana DECIMAL(8,2) NOT NULL CHECK (ancien_Poids_Fisakafoanana >= 0),
    nouveau_Poids_Fisakafoanana DECIMAL(8,2) NOT NULL CHECK (nouveau_Poids_Fisakafoanana >= 0),
    quantite_Nourriture_Fisakafoanana DECIMAL(8,2) NOT NULL DEFAULT 0 CHECK (quantite_Nourriture_Fisakafoanana >= 0),
    proteines_Recues_Fisakafoanana DECIMAL(8,2) DEFAULT 0 CHECK (proteines_Recues_Fisakafoanana >= 0),
    glucides_Recus_Fisakafoanana DECIMAL(8,2) DEFAULT 0 CHECK (glucides_Recus_Fisakafoanana >= 0),
    gain_Poids_Fisakafoanana DECIMAL(8,2) DEFAULT 0,
    taux_Satisfaction_Fisakafoanana DECIMAL(5,2) DEFAULT 0 CHECK (taux_Satisfaction_Fisakafoanana >= 0 AND taux_Satisfaction_Fisakafoanana <= 100),
    besoins_Satisfaits_Fisakafoanana BOOLEAN DEFAULT FALSE,
    date_Creation_Fisakafoanana TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_Poids_Augmentation CHECK (nouveau_Poids_Fisakafoanana >= ancien_Poids_Fisakafoanana)
);

-- 7. Table des utilisateurs (pour authentification)
CREATE TABLE Utilisateur (
    id_Utilisateur SERIAL PRIMARY KEY,
    nom_Utilisateur VARCHAR(100) NOT NULL,
    email_Utilisateur VARCHAR(100) UNIQUE NOT NULL,
    motdepasse_Utilisateur VARCHAR(255) NOT NULL,
    role_Utilisateur VARCHAR(30) DEFAULT 'UTILISATEUR' CHECK (role_Utilisateur IN ('ADMIN', 'GERANT', 'UTILISATEUR')),
    est_Actif_Utilisateur BOOLEAN DEFAULT TRUE,
    date_Derniere_Connexion TIMESTAMP,
    date_Creation_Utilisateur TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser les performances
CREATE INDEX idx_Poisson_Race ON Poisson(id_RacePoisson);
CREATE INDEX idx_Poisson_Piscine ON Poisson(id_Piscine_Actuel);
CREATE INDEX idx_Affectation_Piscine ON AffectationPiscine(id_Piscine);
CREATE INDEX idx_Affectation_Poisson ON AffectationPiscine(id_Poisson);
CREATE INDEX idx_Fisakafoanana_Poisson ON Fisakafoanana(id_Poisson);
CREATE INDEX idx_Poisson_DateArrivee ON Poisson(date_Arrivee_Poisson);
CREATE INDEX idx_Poisson_EstVendu ON Poisson(est_Vendu_Poisson);
CREATE INDEX idx_Fisakafoanana_Date ON Fisakafoanana(date_Nourrissage_Fisakafoanana);

-- Données de test
INSERT INTO RacePoisson (nom_RacePoisson, description_RacePoisson) VALUES
('Tilapia', 'Poisson à croissance rapide, résistant'),
('Carpe', 'Poisson herbivore, adapté aux étangs'),
('Truite', 'Poisson d''eau froide, valeur commerciale élevée'),
('Poisson-chat', 'Poisson robuste, omnivore');

INSERT INTO Aliment (nom_Aliment, prix_Kg_Aliment, proteines_Par_Kg_Aliment, glucides_Par_Kg_Aliment, stock_Aliment, seuil_Minimum_Aliment) VALUES
('Larves', 2000.00, 100.00, 100.00, 50.00, 10.00),
('Granulés Standard', 1500.00, 150.00, 200.00, 100.00, 20.00),
('Farine de poisson', 3000.00, 600.00, 50.00, 30.00, 5.00),
('Son de riz', 800.00, 80.00, 300.00, 80.00, 15.00);

INSERT INTO Piscine (nom_Piscine, capacite_Max_Piscine, description_Piscine, volume_Piscine) VALUES
('Bassin Nord', 50, 'Bassin principal - Eau douce', 100.00),
('Bassin Sud', 30, 'Bassin secondaire', 60.00),
('Nurserie', 20, 'Pour les alevins', 30.00),
('Bassin Quarantaine', 10, 'Pour les nouveaux arrivants', 15.00);

-- Utilisateur admin par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom_Utilisateur, email_Utilisateur, motdepasse_Utilisateur, role_Utilisateur) VALUES
('Administrateur', 'admin@pisciculture.mg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeG3vRjH6B6E3Y2fGz7JwJQ4fQXr1W3Yy', 'ADMIN');







-- Table des plats (mélanges d'aliments)
CREATE TABLE plat (
    id_plat SERIAL PRIMARY KEY,
    nom_plat VARCHAR(100) NOT NULL,
    description_plat TEXT,
    poids_total_plat DECIMAL(10,2) DEFAULT 0,
    cout_total_plat DECIMAL(10,2) DEFAULT 0,
    proteines_total_plat DECIMAL(8,2) DEFAULT 0,
    glucides_total_plat DECIMAL(8,2) DEFAULT 0,
    proteines_par_kg_plat DECIMAL(8,2) DEFAULT 0,
    glucides_par_kg_plat DECIMAL(8,2) DEFAULT 0,
    date_preparation_plat DATE DEFAULT CURRENT_DATE,
    est_utilise_plat BOOLEAN DEFAULT FALSE,
    date_creation_plat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de composition des plats
CREATE TABLE compositionplat (
    id_compositionplat SERIAL PRIMARY KEY,
    id_plat INT NOT NULL REFERENCES plat(id_plat) ON DELETE CASCADE,
    id_aliment INT NOT NULL REFERENCES aliment(id_aliment),
    poids_aliment_composition DECIMAL(8,2) NOT NULL CHECK (poids_aliment_composition > 0),
    cout_aliment_composition DECIMAL(10,2) DEFAULT 0,
    proteines_composition DECIMAL(8,2) DEFAULT 0,
    glucides_composition DECIMAL(8,2) DEFAULT 0,
    date_creation_composition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification_composition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(id_plat, id_aliment)
);

COMMENT ON TABLE plat IS 'Table des plats préparés (mélanges d''aliments)';
COMMENT ON TABLE compositionplat IS 'Table de liaison entre plats et aliments avec les quantités';

-- Créer des indexes pour les performances
CREATE INDEX idx_composition_plat ON compositionplat(id_plat);
CREATE INDEX idx_composition_aliment ON compositionplat(id_aliment);
CREATE INDEX idx_plat_date ON plat(date_preparation_plat);
CREATE INDEX idx_plat_utilise ON plat(est_utilise_plat) WHERE est_utilise_plat = FALSE;

