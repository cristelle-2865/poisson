
-- 1. Table des races de poissons
CREATE TABLE RacePoisson (
    id_RacePoisson SERIAL PRIMARY KEY,
    nom_RacePoisson VARCHAR(100) UNIQUE NOT NULL,
    description_RacePoisson TEXT,
    date_Creation_RacePoisson TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_Actif_RacePoisson BOOLEAN DEFAULT TRUE
);

COMMENT ON TABLE RacePoisson IS 'Table des différentes races de poissons disponibles';
COMMENT ON COLUMN RacePoisson.id_RacePoisson IS 'Identifiant unique de la race';
COMMENT ON COLUMN RacePoisson.nom_RacePoisson IS 'Nom de la race (ex: Tilapia, Carpe)';
COMMENT ON COLUMN RacePoisson.est_Actif_RacePoisson IS 'Flag indiquant si la race est encore utilisée';

-- 2. Table des poissons
CREATE TABLE Poisson (
    id_Poisson SERIAL PRIMARY KEY,
    id_RacePoisson INT NOT NULL REFERENCES RacePoisson(id_RacePoisson),
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

COMMENT ON TABLE Poisson IS 'Table contenant tous les poissons de la pisciculture';
COMMENT ON COLUMN Poisson.capacite_Augmentation_Poisson IS 'Capacité maximale de gain de poids par jour (en grammes)';
COMMENT ON COLUMN Poisson.est_Rassasie_Poisson IS 'Indique si le poisson a atteint ses besoins nutritionnels journaliers';

-- 3. Table des aliments
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

COMMENT ON TABLE Aliment IS 'Table des différents types d''aliments disponibles';
COMMENT ON COLUMN Aliment.proteines_Par_Kg_Aliment IS 'Grammes de protéines par kilogramme d''aliment';
COMMENT ON COLUMN Aliment.glucides_Par_Kg_Aliment IS 'Grammes de glucides par kilogramme d''aliment';

-- 4. Table des plats (mélanges d'aliments)
CREATE TABLE Plat (
    id_Plat SERIAL PRIMARY KEY,
    nom_Plat VARCHAR(100) NOT NULL,
    description_Plat TEXT,
    poids_Total_Plat DECIMAL(10,2) DEFAULT 0,
    cout_Total_Plat DECIMAL(10,2) DEFAULT 0,
    date_Preparation_Plat DATE DEFAULT CURRENT_DATE,
    est_Utilise_Plat BOOLEAN DEFAULT FALSE,
    date_Creation_Plat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE Plat IS 'Table des plats préparés (mélanges d''aliments)';
COMMENT ON COLUMN Plat.est_Utilise_Plat IS 'Indique si le plat a déjà été utilisé pour nourrir';

-- 5. Table de composition des plats
CREATE TABLE CompositionPlat (
    id_CompositionPlat SERIAL PRIMARY KEY,
    id_Plat INT NOT NULL REFERENCES Plat(id_Plat) ON DELETE CASCADE,
    id_Aliment INT NOT NULL REFERENCES Aliment(id_Aliment),
    poids_Aliment_Composition DECIMAL(8,2) NOT NULL CHECK (poids_Aliment_Composition > 0),
    date_Creation_Composition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(id_Plat, id_Aliment)
);

COMMENT ON TABLE CompositionPlat IS 'Table de liaison entre plats et aliments avec les quantités';

-- 6. Table des piscines/bassins
CREATE TABLE Piscine (
    id_Piscine SERIAL PRIMARY KEY,
    nom_Piscine VARCHAR(100) NOT NULL,
    capacite_Max_Piscine INT NOT NULL CHECK (capacite_Max_Piscine > 0),
    description_Piscine TEXT,
    est_Active_Piscine BOOLEAN DEFAULT TRUE,
    date_Creation_Piscine TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE Piscine IS 'Table des bassins/piscines de l''élevage';

-- 7. Table d'affectation des poissons aux piscines
CREATE TABLE AffectationPiscine (
    id_AffectationPiscine SERIAL PRIMARY KEY,
    id_Piscine INT NOT NULL REFERENCES Piscine(id_Piscine),
    id_Poisson INT NOT NULL REFERENCES Poisson(id_Poisson),
    date_Entree_Affectation DATE NOT NULL DEFAULT CURRENT_DATE,
    date_Sortie_Affectation DATE,
    raison_Sortie_Affectation VARCHAR(50),
    date_Creation_Affectation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_Date_Sortie CHECK (date_Sortie_Affectation IS NULL OR date_Sortie_Affectation >= date_Entree_Affectation),
    CONSTRAINT uk_Poisson_Unique_Period UNIQUE(id_Poisson, date_Entree_Affectation)
);

COMMENT ON TABLE AffectationPiscine IS 'Historique des affectations des poissons aux piscines';
COMMENT ON COLUMN AffectationPiscine.raison_Sortie_Affectation IS 'Raison de la sortie (vente, mort, transfert)';

-- 8. TABLE CRITIQUE : Historique de nourrissage et croissance (fisakafoanana)
CREATE TABLE Fisakafoanana (
    id_Fisakafoanana SERIAL PRIMARY KEY,
    id_Poisson INT NOT NULL REFERENCES Poisson(id_Poisson),
    id_Plat INT REFERENCES Plat(id_Plat),
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
    
    CONSTRAINT chk_Poids_Augmentation CHECK (nouveau_Poids_Fisakafoanana >= ancien_Poids_Fisakafoanana),
    CONSTRAINT chk_Gain_Poids CHECK (gain_Poids_Fisakafoanana = nouveau_Poids_Fisakafoanana - ancien_Poids_Fisakafoanana)
);

COMMENT ON TABLE Fisakafoanana IS 'Table historique de l''évolution du poids et de la nutrition des poissons';
COMMENT ON COLUMN Fisakafoanana.taux_Satisfaction_Fisakafoanana IS 'Pourcentage de satisfaction des besoins nutritionnels (0-100%)';

-- 9. Table des transactions financières
CREATE TABLE Transaction (
    id_Transaction SERIAL PRIMARY KEY,
    type_Transaction VARCHAR(30) NOT NULL CHECK (type_Transaction IN (
        'ACHAT_POISSON', 
        'ACHAT_ALIMENT', 
        'VENTE_POISSON', 
        'FRAIS_ENTRETIEN',
        'AUTRE_DEPENSE',
        'AUTRE_RECETTE'
    )),
    montant_Transaction DECIMAL(10,2) NOT NULL,
    devise_Transaction VARCHAR(3) DEFAULT 'MGA',
    description_Transaction TEXT,
    date_Transaction DATE NOT NULL DEFAULT CURRENT_DATE,
    reference_Transaction VARCHAR(50),
    id_Poisson INT REFERENCES Poisson(id_Poisson),
    id_Aliment INT REFERENCES Aliment(id_Aliment),
    est_Valide_Transaction BOOLEAN DEFAULT TRUE,
    date_Creation_Transaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_Montant CHECK (montant_Transaction != 0)
);

COMMENT ON TABLE Transaction IS 'Table de suivi de toutes les transactions financières';

-- 10. Table de paramétrage du système
CREATE TABLE ParametreSysteme (
    id_ParametreSysteme SERIAL PRIMARY KEY,
    cle_Parametre VARCHAR(50) UNIQUE NOT NULL,
    valeur_Parametre TEXT NOT NULL,
    type_Parametre VARCHAR(20) CHECK (type_Parametre IN ('INTEGER', 'DECIMAL', 'STRING', 'BOOLEAN', 'DATE', 'JSON')),
    description_Parametre TEXT,
    est_Modifiable_Parametre BOOLEAN DEFAULT TRUE,
    date_Creation_Parametre TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_Modification_Parametre TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ParametreSysteme IS 'Table de paramétrage configurable du système';

-- 11. Table des utilisateurs (pour authentification)
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

COMMENT ON TABLE Utilisateur IS 'Table des utilisateurs du système';

```

---

## 📊 **Index et Performances**

```sql
-- ============================================
-- INDEX pour optimiser les performances
-- ============================================

-- Index sur les clés étrangères
CREATE INDEX idx_Poisson_Race ON Poisson(id_RacePoisson);
CREATE INDEX idx_Composition_Plat ON CompositionPlat(id_Plat);
CREATE INDEX idx_Composition_Aliment ON CompositionPlat(id_Aliment);
CREATE INDEX idx_Affectation_Piscine ON AffectationPiscine(id_Piscine);
CREATE INDEX idx_Affectation_Poisson ON AffectationPiscine(id_Poisson);
CREATE INDEX idx_Fisakafoanana_Poisson ON Fisakafoanana(id_Poisson);
CREATE INDEX idx_Fisakafoanana_Plat ON Fisakafoanana(id_Plat);
CREATE INDEX idx_Transaction_Poisson ON Transaction(id_Poisson);
CREATE INDEX idx_Transaction_Aliment ON Transaction(id_Aliment);

-- Index pour les recherches fréquentes
CREATE INDEX idx_Poisson_DateArrivee ON Poisson(date_Arrivee_Poisson);
CREATE INDEX idx_Poisson_EstVendu ON Poisson(est_Vendu_Poisson) WHERE est_Vendu_Poisson = FALSE;
CREATE INDEX idx_Poisson_PoidsActuel ON Poisson(poids_Actuel_Poisson);
CREATE INDEX idx_Fisakafoanana_Date ON Fisakafoanana(date_Nourrissage_Fisakafoanana);
CREATE INDEX idx_Transaction_TypeDate ON Transaction(type_Transaction, date_Transaction);
CREATE INDEX idx_Aliment_Stock ON Aliment(stock_Aliment) WHERE stock_Aliment < 20;

-- Index pour les jointures fréquentes
CREATE INDEX idx_Plat_DatePreparation ON Plat(date_Preparation_Plat);
CREATE INDEX idx_Affectation_Dates ON AffectationPiscine(date_Entree_Affectation, date_Sortie_Affectation);

``

---

## 🔍 **Vues utiles pour les rapports**

-- ============================================
-- VUES pour simplifier les requêtes complexes
-- ============================================

-- Vue: État actuel des poissons
CREATE OR REPLACE VIEW Vue_EtatPoissons AS
SELECT 
    p.id_Poisson,
    p.nom_Poisson,
    rp.nom_RacePoisson,
    p.poids_Actuel_Poisson,
    p.poids_Maximal_Poisson,
    ROUND((p.poids_Actuel_Poisson / p.poids_Maximal_Poisson) * 100, 2) AS pourcentage_Croissance,
    p.date_Arrivee_Poisson,
    p.est_Rassasie_Poisson,
    p.est_Vendu_Poisson,
    pi.nom_Piscine,
    ap.date_Entree_Affectation
FROM Poisson p
JOIN RacePoisson rp ON p.id_RacePoisson = rp.id_RacePoisson
LEFT JOIN AffectationPiscine ap ON p.id_Poisson = ap.id_Poisson 
    AND ap.date_Sortie_Affectation IS NULL
LEFT JOIN Piscine pi ON ap.id_Piscine = pi.id_Piscine
WHERE p.est_Vendu_Poisson = FALSE AND p.est_EnVie_Poisson = TRUE;

-- Vue: Historique complet de croissance
CREATE OR REPLACE VIEW Vue_HistoriqueCroissance AS
SELECT 
    f.id_Poisson,
    p.nom_Poisson,
    f.date_Nourrissage_Fisakafoanana,
    f.ancien_Poids_Fisakafoanana,
    f.nouveau_Poids_Fisakafoanana,
    f.gain_Poids_Fisakafoanana,
    f.quantite_Nourriture_Fisakafoanana,
    f.proteines_Recues_Fisakafoanana,
    f.glucides_Recus_Fisakafoanana,
    f.taux_Satisfaction_Fisakafoanana,
    pl.nom_Plat,
    ROUND(SUM(f.gain_Poids_Fisakafoanana) OVER (PARTITION BY f.id_Poisson ORDER BY f.date_Nourrissage_Fisakafoanana), 2) AS gain_Total_Cumule
FROM Fisakafoanana f
JOIN Poisson p ON f.id_Poisson = p.id_Poisson
LEFT JOIN Plat pl ON f.id_Plat = pl.id_Plat
ORDER BY f.id_Poisson, f.date_Nourrissage_Fisakafoanana;

-- Vue: Bilan financier journalier
CREATE OR REPLACE VIEW Vue_BilanFinancierJournalier AS
SELECT 
    date_Transaction,
    SUM(CASE WHEN type_Transaction IN ('VENTE_POISSON', 'AUTRE_RECETTE') THEN montant_Transaction ELSE 0 END) AS recettes_Journalieres,
    SUM(CASE WHEN type_Transaction IN ('ACHAT_POISSON', 'ACHAT_ALIMENT', 'FRAIS_ENTRETIEN', 'AUTRE_DEPENSE') THEN montant_Transaction ELSE 0 END) AS depenses_Journalieres,
    SUM(CASE WHEN type_Transaction IN ('VENTE_POISSON', 'AUTRE_RECETTE') THEN montant_Transaction ELSE -montant_Transaction END) AS solde_Journalier
FROM Transaction
WHERE est_Valide_Transaction = TRUE
GROUP BY date_Transaction
ORDER BY date_Transaction DESC;

-- Vue: Stocks d'aliments critiques
CREATE OR REPLACE VIEW Vue_StocksCritiques AS
SELECT 
    id_Aliment,
    nom_Aliment,
    stock_Aliment,
    seuil_Minimum_Aliment,
    ROUND((stock_Aliment / seuil_Minimum_Aliment) * 100, 2) AS pourcentage_Stock,
    CASE 
        WHEN stock_Aliment <= 0 THEN 'RUPTURE'
        WHEN stock_Aliment <= seuil_Minimum_Aliment * 0.3 THEN 'CRITIQUE'
        WHEN stock_Aliment <= seuil_Minimum_Aliment THEN 'FAIBLE'
        ELSE 'NORMAL'
    END AS etat_Stock
FROM Aliment
WHERE est_Actif_Aliment = TRUE
ORDER BY stock_Aliment ASC;

-- Vue: Performance par race de poisson
-- 6. Vue: Performance par race de poisson (SIMPLIFIÉE)
CREATE OR REPLACE VIEW Vue_PerformanceRace AS
SELECT 
    rp.id_RacePoisson,
    rp.nom_RacePoisson,
    COUNT(DISTINCT p.id_Poisson) AS nombre_Poissons,
    COUNT(DISTINCT CASE WHEN p.est_Vendu_Poisson = TRUE THEN p.id_Poisson END) AS nombre_Vendus,
    AVG(p.poids_Actuel_Poisson) AS poids_Moyen_Actuel,
    AVG(p.poids_Maximal_Poisson) AS poids_Maximal_Moyen,
    -- Durée d'élevage moyenne en jours
    AVG(
        CASE 
            WHEN p.date_Vente_Poisson IS NOT NULL 
                THEN p.date_Vente_Poisson - p.date_Arrivee_Poisson
            WHEN p.est_EnVie_Poisson = TRUE 
                THEN CURRENT_DATE - p.date_Arrivee_Poisson
            ELSE NULL
        END
    ) AS duree_Elevage_Moyenne,
    SUM(CASE 
        WHEN t.type_Transaction = 'VENTE_POISSON' 
        THEN t.montant_Transaction 
        ELSE 0 
    END) AS chiffre_Affaires,
    SUM(CASE 
        WHEN t.type_Transaction = 'ACHAT_POISSON' 
        THEN t.montant_Transaction 
        ELSE 0 
    END) AS cout_Achat
FROM RacePoisson rp
LEFT JOIN Poisson p ON rp.id_RacePoisson = p.id_RacePoisson
LEFT JOIN Transaction t ON p.id_Poisson = t.id_Poisson
GROUP BY rp.id_RacePoisson, rp.nom_RacePoisson;

```

---

## ⚙️ **Données de référence initiales**

```sql
-- ============================================
-- INSERT des données de référence
-- ============================================

-- Races de poissons
INSERT INTO RacePoisson (nom_RacePoisson, description_RacePoisson) VALUES
('Tilapia', 'Poisson à croissance rapide, résistant'),
('Carpe', 'Poisson herbivore, adapté aux étangs'),
('Truite', 'Poisson d''eau froide, valeur commerciale élevée'),
('Poisson-chat', 'Poisson robuste, omnivore');

-- Aliments de base
INSERT INTO Aliment (nom_Aliment, prix_Kg_Aliment, proteines_Par_Kg_Aliment, glucides_Par_Kg_Aliment, stock_Aliment, seuil_Minimum_Aliment) VALUES
('Larves', 2000.00, 100.00, 100.00, 50.00, 10.00), -- 10g protéines/100g, 10g glucides/100g
('Granulés Standard', 1500.00, 150.00, 200.00, 100.00, 20.00),
('Farine de poisson', 3000.00, 600.00, 50.00, 30.00, 5.00),
('Son de riz', 800.00, 80.00, 300.00, 80.00, 15.00),
('Tourteau de soja', 2500.00, 400.00, 100.00, 40.00, 8.00);

-- Piscines
INSERT INTO Piscine (nom_Piscine, capacite_Max_Piscine, description_Piscine) VALUES
('Bassin Nord', 500, 'Bassin principal - Eau douce'),
('Bassin Sud', 300, 'Bassin secondaire'),
('Nurserie', 100, 'Pour les alevins'),
('Bassin Quarantaine', 50, 'Pour les nouveaux arrivants');

-- Paramètres système
INSERT INTO ParametreSysteme (cle_Parametre, valeur_Parametre, type_Parametre, description_Parametre) VALUES
('BESOINS_PROTEINES_JOUR', '2.0', 'DECIMAL', 'Besoins journaliers en protéines (grammes)'),
('BESOINS_GLUCIDES_JOUR', '4.0', 'DECIMAL', 'Besoins journaliers en glucides (grammes)'),
('GAIN_POIDS_MAX_JOUR', '20.0', 'DECIMAL', 'Gain de poids maximal par jour (grammes)'),
('SEUIL_RASSASIEMENT', '80.0', 'DECIMAL', 'Seuil de satisfaction nutritionnelle pour être rassasié (%)'),
('DEVISE_PAR_DEFAUT', 'MGA', 'STRING', 'Devise utilisée par défaut'),
('TAXE_VENTE', '0.0', 'DECIMAL', 'Taxe applicable sur les ventes (%)'),
('NOTIFICATION_STOCK', 'true', 'BOOLEAN', 'Activer les notifications de stock bas');

-- Utilisateur admin par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom_Utilisateur, email_Utilisateur, motdepasse_Utilisateur, role_Utilisateur) VALUES
('Administrateur', 'admin@pisciculture.mg', '$2a$10$YourHashedPasswordHere', 'ADMIN');

INSERT INTO Utilisateur (nom_Utilisateur, email_Utilisateur, motdepasse_Utilisateur, role_Utilisateur) VALUES
('Miah', 'miah@gmail.com', '123', 'ADMIN');


INSERT INTO Utilisateur (nom_Utilisateur, email_Utilisateur, motdepasse_Utilisateur, role_Utilisateur) VALUES
('Krys', 'krys@gmail.com', 'miah123', 'ADMIN');


```

---

## 🔄 **Triggers pour la cohérence des données**

```sql
-- ============================================
-- TRIGGERS pour maintenir l'intégrité des données
-- ============================================

-- 1. Trigger pour mettre à jour automatiquement le poids total du plat
CREATE OR REPLACE FUNCTION update_poids_total_plat()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        UPDATE Plat 
        SET poids_Total_Plat = (
            SELECT SUM(poids_Aliment_Composition) 
            FROM CompositionPlat 
            WHERE id_Plat = NEW.id_Plat
        ),
        cout_Total_Plat = (
            SELECT SUM(cp.poids_Aliment_Composition * a.prix_Kg_Aliment)
            FROM CompositionPlat cp
            JOIN Aliment a ON cp.id_Aliment = a.id_Aliment
            WHERE cp.id_Plat = NEW.id_Plat
        )
        WHERE id_Plat = NEW.id_Plat;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE Plat 
        SET poids_Total_Plat = (
            SELECT COALESCE(SUM(poids_Aliment_Composition), 0)
            FROM CompositionPlat 
            WHERE id_Plat = OLD.id_Plat
        ),
        cout_Total_Plat = (
            SELECT COALESCE(SUM(cp.poids_Aliment_Composition * a.prix_Kg_Aliment), 0)
            FROM CompositionPlat cp
            JOIN Aliment a ON cp.id_Aliment = a.id_Aliment
            WHERE cp.id_Plat = OLD.id_Plat
        )
        WHERE id_Plat = OLD.id_Plat;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_update_poids_plat
AFTER INSERT OR UPDATE OR DELETE ON CompositionPlat
FOR EACH ROW EXECUTE FUNCTION update_poids_total_plat();

-- 2. Trigger pour mettre à jour le stock d'aliment après création d'un plat
CREATE OR REPLACE FUNCTION update_stock_aliment_after_plat()
RETURNS TRIGGER AS $$
DECLARE
    v_poids_total DECIMAL;
BEGIN
    -- Vérifier si le plat est marqué comme utilisé
    IF NEW.est_Utilise_Plat = TRUE AND OLD.est_Utilise_Plat = FALSE THEN
        -- Diminuer le stock de chaque aliment
        UPDATE Aliment a
        SET stock_Aliment = stock_Aliment - cp.poids_Aliment_Composition
        FROM CompositionPlat cp
        WHERE a.id_Aliment = cp.id_Aliment
        AND cp.id_Plat = NEW.id_Plat;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_update_stock_plat
AFTER UPDATE ON Plat
FOR EACH ROW EXECUTE FUNCTION update_stock_aliment_after_plat();

-- 3. Trigger pour mettre à jour date_Modification
CREATE OR REPLACE FUNCTION update_modification_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_Modification_Poisson = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_update_timestamp_poisson
BEFORE UPDATE ON Poisson
FOR EACH ROW EXECUTE FUNCTION update_modification_timestamp();

-- 4. Trigger pour vérifier la capacité de la piscine
CREATE OR REPLACE FUNCTION check_capacite_piscine()
RETURNS TRIGGER AS $$
DECLARE
    v_nombre_poissons INT;
    v_capacite_max INT;
BEGIN
    -- Compter le nombre de poissons actifs dans la piscine
    SELECT COUNT(*) INTO v_nombre_poissons
    FROM AffectationPiscine
    WHERE id_Piscine = NEW.id_Piscine
    AND date_Sortie_Affectation IS NULL;
    
    -- Obtenir la capacité maximale
    SELECT capacite_Max_Piscine INTO v_capacite_max
    FROM Piscine
    WHERE id_Piscine = NEW.id_Piscine;
    
    -- Vérifier si l'ajout dépasse la capacité
    IF v_nombre_poissons >= v_capacite_max THEN
        RAISE EXCEPTION 'La piscine % a atteint sa capacité maximale de % poissons', 
            NEW.id_Piscine, v_capacite_max;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_check_capacite_piscine
BEFORE INSERT ON AffectationPiscine
FOR EACH ROW EXECUTE FUNCTION check_capacite_piscine();
```

---

## 📝 **Exemple de données de test**

```sql
-- ============================================
-- DONNÉES DE TEST
-- ============================================

-- 1. Création de poissons
INSERT INTO Poisson (
    id_RacePoisson, nom_Poisson, prix_Achat_Poisson, prix_Vente_Poisson,
    poids_Maximal_Poisson, capacite_Augmentation_Poisson, poids_Initial_Poisson,
    poids_Actuel_Poisson, date_Arrivee_Poisson, id_Piscine_Actuel
) VALUES
(1, 'Tilapia-001', 500.00, 2000.00, 500.00, 20.00, 50.00, 50.00, '2024-01-15', '1'),
(1, 'Tilapia-002', 500.00, 2000.00, 500.00, 20.00, 50.00, 50.00, '2024-01-15', '2'),
(2, 'Carpe-001', 600.00, 2500.00, 800.00, 15.00, 100.00, 100.00, '2024-01-10', '3'),
(3, 'Truite-001', 800.00, 3500.00, 300.00, 25.00, 30.00, 30.00, '2024-01-20', '4');

-- 2. Affectation aux piscines
INSERT INTO AffectationPiscine (id_Piscine, id_Poisson, date_Entree_Affectation) VALUES
(1, 1, '2024-01-15'),
(1, 2, '2024-01-15'),
(2, 3, '2024-01-10'),
(3, 4, '2024-01-20');

-- 3. Création d'un plat
INSERT INTO Plat (nom_Plat, description_Plat, date_Preparation_Plat) 
VALUES ('Mélange Standard', 'Mélange équilibré pour croissance optimale', '2024-01-25');

-- 4. Composition du plat (en grammes)
INSERT INTO CompositionPlat (id_Plat, id_Aliment, poids_Aliment_Composition) VALUES
(1, 1, 2000), -- 2kg de Larves
(1, 2, 3000), -- 3kg de Granulés Standard
(1, 5, 1000); -- 1kg de Tourteau de soja

-- Le poids total (6kg) et coût total seront calculés automatiquement par le trigger
```

---

## 📊 **Requêtes SQL essentielles pour l'application**

```sql
-- 1. Calcul du bénéfice sur une période
SELECT 
    SUM(CASE WHEN type_Transaction = 'VENTE_POISSON' THEN montant_Transaction ELSE 0 END) AS total_Ventes,
    SUM(CASE WHEN type_Transaction IN ('ACHAT_POISSON', 'ACHAT_ALIMENT') THEN montant_Transaction ELSE 0 END) AS total_Achats,
    SUM(CASE WHEN type_Transaction = 'VENTE_POISSON' THEN montant_Transaction 
             WHEN type_Transaction IN ('ACHAT_POISSON', 'ACHAT_ALIMENT') THEN -montant_Transaction 
             ELSE 0 END) AS benefice_Net
FROM Transaction
WHERE date_Transaction BETWEEN '2024-01-01' AND '2024-01-31'
AND est_Valide_Transaction = TRUE;

-- 2. Évolution du poids d'un poisson spécifique
SELECT 
    date_Nourrissage_Fisakafoanana,
    ancien_Poids_Fisakafoanana,
    nouveau_Poids_Fisakafoanana,
    gain_Poids_Fisakafoanana,
    taux_Satisfaction_Fisakafoanana,
    quantite_Nourriture_Fisakafoanana,
    nom_Plat
FROM Fisakafoanana f
LEFT JOIN Plat p ON f.id_Plat = p.id_Plat
WHERE f.id_Poisson = 1
ORDER BY date_Nourrissage_Fisakafoanana;

-- 3. Poissons prêts à être vendus (atteignant 95% de leur poids max)
SELECT 
    p.id_Poisson,
    p.nom_Poisson,
    rp.nom_RacePoisson,
    p.poids_Actuel_Poisson,
    p.poids_Maximal_Poisson,
    ROUND((p.poids_Actuel_Poisson / p.poids_Maximal_Poisson) * 100, 2) AS pourcentage_Atteint,
    p.prix_Vente_Poisson
FROM Poisson p
JOIN RacePoisson rp ON p.id_RacePoisson = rp.id_RacePoisson
WHERE p.est_Vendu_Poisson = FALSE
AND p.est_EnVie_Poisson = TRUE
AND (p.poids_Actuel_Poisson / p.poids_Maximal_Poisson) >= 0.95;

-- 4. Calcul des besoins nutritionnels pour une piscine
SELECT 
    pi.nom_Piscine,
    COUNT(DISTINCT p.id_Poisson) AS nombre_Poissons,
    COUNT(DISTINCT p.id_Poisson) * 2.0 AS besoins_Proteines_Totaux, -- 2g par poisson
    COUNT(DISTINCT p.id_Poisson) * 4.0 AS besoins_Glucides_Totaux, -- 4g par poisson
    COUNT(DISTINCT p.id_Poisson) * 5.0 AS quantite_Plat_Necessaire -- 5g de plat par poisson (règle de trois)
FROM Piscine pi
JOIN AffectationPiscine ap ON pi.id_Piscine = ap.id_Piscine
JOIN Poisson p ON ap.id_Poisson = p.id_Poisson
WHERE ap.date_Sortie_Affectation IS NULL
AND p.est_Vendu_Poisson = FALSE
AND p.est_Rassasie_Poisson = FALSE
GROUP BY pi.id_Piscine, pi.nom_Piscine;
```

---

## 🚀 **Script de déploiement**

```bash
#!/bin/bash
# deploy_database.sh

echo "Création de la base de données pisciculture..."

# 1. Créer la base de données
createdb -U postgres pisciculture_db

# 2. Exécuter le script SQL
psql -U postgres -d pisciculture_db -f pisciculture_schema.sql

# 3. Vérifier la création
psql -U postgres -d pisciculture_db -c "\dt"

echo "Base de données créée avec succès !"

Cette structure complète respecte la convention `id_NomTable`, assure l'intégrité des données, et est optimisée pour les performances. Elle inclut tous les éléments nécessaires pour le système décrit dans votre cahier des charges.



















-- Au lieu de mettre à jour poidsActuelPoisson directement
-- Toujours passer par fisakafoanana
CREATE OR REPLACE FUNCTION before_poisson_update_fn()
RETURNS TRIGGER AS $$
BEGIN

    IF OLD.poidsactuelpoisson IS DISTINCT FROM NEW.poidsactuelpoisson THEN
        
        INSERT INTO fisakafoanana(
            poisson_id,
            ancien_poids,
            nouveau_poids,
            date_modif
        )
        VALUES (
            OLD.id,
            OLD.poidsactuelpoisson,
            NEW.poidsactuelpoisson,
            NOW()
        );

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;




--verification de la structure src
tree /f












-- Ajouter la colonne pour le bassin actuel
ALTER TABLE Poisson 
ADD COLUMN id_Piscine_Actuel INT REFERENCES Piscine(id_Piscine);

-- Comment expliquer cette colonne
COMMENT ON COLUMN Poisson.id_Piscine_Actuel IS 'Piscine actuelle du poisson (pour performances)';

-- Mettre à jour avec les données existantes
UPDATE Poisson p
SET id_Piscine_Actuel = (
    SELECT ap.id_Piscine 
    FROM AffectationPiscine ap 
    WHERE ap.id_Poisson = p.id_Poisson 
    AND ap.date_Sortie_Affectation IS NULL
    ORDER BY ap.date_Entree_Affectation DESC 
    LIMIT 1
);

CREATE OR REPLACE FUNCTION sync_piscine_actuelle()
RETURNS TRIGGER AS $$
BEGIN
    -- Pour INSERT (nouvelle affectation)
    IF TG_OP = 'INSERT' THEN
        UPDATE Poisson 
        SET id_Piscine_Actuel = NEW.id_Piscine,
            date_Modification_Poisson = CURRENT_TIMESTAMP
        WHERE id_Poisson = NEW.id_Poisson;
    
    -- Pour UPDATE (fin d'affectation)
    ELSIF TG_OP = 'UPDATE' AND NEW.date_Sortie_Affectation IS NOT NULL THEN
        UPDATE Poisson 
        SET id_Piscine_Actuel = NULL,
            date_Modification_Poisson = CURRENT_TIMESTAMP
        WHERE id_Poisson = NEW.id_Poisson;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_sync_piscine_actuelle
AFTER INSERT OR UPDATE ON AffectationPiscine
FOR EACH ROW EXECUTE FUNCTION sync_piscine_actuelle();

-- 2. Ajouter un index pour les performances
CREATE INDEX idx_Poisson_PiscineActuel ON Poisson(id_Piscine_Actuel) 
WHERE id_Piscine_Actuel IS NOT NULL;

-- 3. Ajouter des contraintes
ALTER TABLE Poisson
ADD CONSTRAINT fk_Poisson_Piscine FOREIGN KEY (id_Piscine_Actuel) 
REFERENCES Piscine(id_Piscine) ON DELETE SET NULL;

-- Vue qui montre l'historique ET la situation actuelle
CREATE OR REPLACE VIEW Vue_Poisson_Piscine AS
SELECT 
    p.id_Poisson,
    p.nom_Poisson,
    
    -- Piscine actuelle (depuis colonne directe)
    p.id_Piscine_Actuel AS piscine_actuelle_id,
    pi.nom_Piscine AS piscine_actuelle_nom,
    
    -- Dernière affectation (depuis l'historique)
    ap.id_Piscine AS derniere_piscine_id,
    ap.date_Entree_Affectation,
    ap.date_Sortie_Affectation,
    ap.raison_Sortie_Affectation,
    
    -- Historique complet
    (SELECT COUNT(*) FROM AffectationPiscine ap2 
     WHERE ap2.id_Poisson = p.id_Poisson) AS nombre_changements_piscine
    
FROM Poisson p
LEFT JOIN Piscine pi ON p.id_Piscine_Actuel = pi.id_Piscine
LEFT JOIN AffectationPiscine ap ON p.id_Poisson = ap.id_Poisson 
    AND ap.date_Sortie_Affectation IS NULL;

