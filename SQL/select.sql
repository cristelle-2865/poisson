-- Voir tout l'historique des nourrissages
SELECT 
    f.id_Fisakafoanana,
    f.date_Nourrissage_Fisakafoanana,
    f.heure_Nourrissage_Fisakafoanana,
    p.nom_Poisson,
    rp.nom_RacePoisson,
    f.ancien_Poids_Fisakafoanana AS "Poids avant (g)",
    f.nouveau_Poids_Fisakafoanana AS "Poids après (g)",
    f.gain_Poids_Fisakafoanana AS "Gain (g)",
    f.quantite_Nourriture_Fisakafoanana AS "Nourriture (g)",
    f.proteines_Recues_Fisakafoanana AS "Protéines (g)",
    f.glucides_Recus_Fisakafoanana AS "Glucides (g)",
    f.taux_Satisfaction_Fisakafoanana AS "Satisfaction (%)",
    CASE 
        WHEN f.besoins_Satisfaits_Fisakafoanana THEN 'Oui ✓'
        ELSE 'Non ✗'
    END AS "Besoins satisfaits",
    pl.nom_Plat AS "Plat utilisé",
    f.date_Creation_Fisakafoanana AS "Date création"
FROM Fisakafoanana f
JOIN Poisson p ON f.id_Poisson = p.id_Poisson
JOIN RacePoisson rp ON p.id_RacePoisson = rp.id_RacePoisson
LEFT JOIN Plat pl ON f.id_Plat = pl.id_Plat
ORDER BY f.date_Nourrissage_Fisakafoanana DESC, f.heure_Nourrissage_Fisakafoanana DESC
LIMIT 50;


-- Trouver tous les poissons dans un bassin spécifique
SELECT * FROM Poisson WHERE id_Piscine_Actuel = 1;

-- Compter les poissons par bassin
SELECT id_Piscine_Actuel, COUNT(*) 
FROM Poisson 
WHERE id_Piscine_Actuel IS NOT NULL
GROUP BY id_Piscine_Actuel;

-- Vérifier la capacité d'un bassin
SELECT 
    p.nom_Piscine,
    p.capacite_Max_Piscine,
    COUNT(po.id_Poisson) AS occupation_actuelle
FROM Piscine p
LEFT JOIN Poisson po ON p.id_Piscine = po.id_Piscine_Actuel
GROUP BY p.id_Piscine;

-- Voir tous les changements d'un poisson
SELECT * FROM AffectationPiscine 
WHERE id_Poisson = 1 
ORDER BY date_Entree_Affectation;

-- Statistiques de transfert
SELECT raison_Sortie_Affectation, COUNT(*)
FROM AffectationPiscine 
WHERE raison_Sortie_Affectation IS NOT NULL
GROUP BY raison_Sortie_Affectation;







-- 1. Ajouter la colonne
ALTER TABLE Poisson ADD COLUMN id_Piscine_Actuel INT;

-- 2. Ajouter la contrainte de clé étrangère
ALTER TABLE Poisson 
ADD CONSTRAINT fk_Poisson_PiscineActuel 
FOREIGN KEY (id_Piscine_Actuel) 
REFERENCES Piscine(id_Piscine) 
ON DELETE SET NULL;

-- 3. Peupler avec les données existantes
UPDATE Poisson p
SET id_Piscine_Actuel = (
    SELECT ap.id_Piscine 
    FROM AffectationPiscine ap 
    WHERE ap.id_Poisson = p.id_Poisson 
    AND ap.date_Sortie_Affectation IS NULL
    ORDER BY ap.date_Entree_Affectation DESC 
    LIMIT 1
);

-- 4. Créer le trigger de synchronisation
CREATE OR REPLACE FUNCTION sync_piscine_actuelle_from_affectation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.date_Sortie_Affectation IS NULL THEN
        -- Mettre à jour le poisson avec la nouvelle piscine
        UPDATE Poisson 
        SET id_Piscine_Actuel = NEW.id_Piscine
        WHERE id_Poisson = NEW.id_Poisson;
    
    ELSIF TG_OP = 'UPDATE' AND NEW.date_Sortie_Affectation IS NOT NULL THEN
        -- Effacer la piscine actuelle du poisson
        UPDATE Poisson 
        SET id_Piscine_Actuel = NULL
        WHERE id_Poisson = NEW.id_Poisson;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_sync_piscine_after_affectation
AFTER INSERT OR UPDATE ON AffectationPiscine
FOR EACH ROW EXECUTE FUNCTION sync_piscine_actuelle_from_affectation();