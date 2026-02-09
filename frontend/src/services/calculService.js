/**
 * Service de calculs métiers pour la pisciculture
 * Implémente exactement les règles du cahier des charges
 */
export const calculService = {
  
  /**
   * Calcul des apports nutritionnels totaux selon la règle de trois
   * @param {number} quantitePlatKg - Quantité de plat en kg
   * @param {number} proteinesParKg - Protéines par kg d'aliment (g/kg)
   * @param {number} glucidesParKg - Glucides par kg d'aliment (g/kg)
   * @returns {Object} Apports totaux en protéines et glucides
   */
  calculerApportsTotaux(quantitePlatKg, proteinesParKg, glucidesParKg) {
    // Règle de trois : 
    // Pour 5g de plat → 2g protéines et 4g glucides
    // Donc pour quantitePlatKg kg de plat :
      const platEnGrammes = quantitePlatKg * 1000; // Convertir en grammes
    
      const proteinesTotal = (platEnGrammes * proteinesParKg) / 1000;
      const glucidesTotal = (platEnGrammes * glucidesParKg) / 1000;
      
      return {
        proteinesTotal: Math.round(proteinesTotal * 100) / 100,
        glucidesTotal: Math.round(glucidesTotal * 100) / 100
      };
  },
  
  /**
   * Calcul du gain de poids selon les règles exactes du cahier des charges
   * @param {number} proteinesRecues - Protéines reçues en grammes
   * @param {number} glucidesRecus - Glucides reçus en grammes
   * @returns {number} Gain de poids en grammes
   */
  calculerGainPoids(proteinesRecues, glucidesRecus) {
    // Aligner sur le backend - interpolation linéaire
    const BESOIN_PROTEINES = 2.0;
    const BESOIN_GLUCIDES = 4.0;
    
    // Cas standards (comme backend)
    if (proteinesRecues === BESOIN_PROTEINES && glucidesRecus === BESOIN_GLUCIDES) {
      return 10;
    }
    
    if (proteinesRecues === BESOIN_PROTEINES && glucidesRecus === 0) {
      return 5;
    }
    
    if (proteinesRecues === 0 && glucidesRecus === BESOIN_GLUCIDES) {
      return 5;
    }
    
    if (proteinesRecues === 4 && glucidesRecus === BESOIN_GLUCIDES) {
      return 15;
    }
    
    if (proteinesRecues === 4 && glucidesRecus === 8) {
      return 20;
    }
    
    // Interpolation linéaire comme le backend
    const tauxProteines = Math.min(proteinesRecues / 4, 1);
    const tauxGlucides = Math.min(glucidesRecus / 8, 1);
    const tauxMoyen = (tauxProteines + tauxGlucides) / 2;
    
    return Math.round(tauxMoyen * 20 * 100) / 100; // Arrondi à 2 décimales
  },
  
  /**
   * Calcul des nutriments fournis par un aliment spécifique
   * @param {number} quantiteG - Quantité d'aliment en grammes
   * @param {Object} aliment - Objet aliment avec proteinesParKg et glucidesParKg
   * @returns {Object} Protéines et glucides fournis
   */
  calculerNutrimentsAliment(quantiteG, aliment) {
    if (!aliment) return { proteines: 0, glucides: 0 };
    
    // Convertir g/kg en g/g
    const proteinesParGramme = aliment.proteinesParKg / 1000;
    const glucidesParGramme = aliment.glucidesParKg / 1000;
    
    const proteines = proteinesParGramme * quantiteG;
    const glucides = glucidesParGramme * quantiteG;
    
    return {
      proteines: Math.round(proteines * 100) / 100,
      glucides: Math.round(glucides * 100) / 100
    };
  },
  
  /**
   * Calcul de la satisfaction nutritionnelle en pourcentage
   * @param {number} proteinesRecues - Protéines reçues en g
   * @param {number} glucidesRecus - Glucides reçus en g
   * @returns {Object} Pourcentages de satisfaction
   */
  calculerSatisfaction(proteinesRecues, glucidesRecus) {
    const besoinProteines = 2; // g par jour
    const besoinGlucides = 4;  // g par jour
    
    const satisfactionProteines = Math.min((proteinesRecues / besoinProteines) * 100, 100);
    const satisfactionGlucides = Math.min((glucidesRecus / besoinGlucides) * 100, 100);
    
    return {
      proteines: Math.round(satisfactionProteines),
      glucides: Math.round(satisfactionGlucides),
      moyenne: Math.round((satisfactionProteines + satisfactionGlucides) / 2)
    };
  },
  
  /**
   * Distribution équitable des nutriments aux poissons
   * @param {Array} poissons - Liste des poissons
   * @param {number} proteinesTotales - Protéines totales disponibles
   * @param {number} glucidesTotaux - Glucides totaux disponibles
   * @returns {Array} Poissons avec leurs allocations
   */
  distribuerNutriments(poissons, proteinesTotales, glucidesTotaux) {
    const nbPoissons = poissons.length;
    if (nbPoissons === 0) return poissons;
    
    const proteinesParPoisson = proteinesTotales / nbPoissons;
    const glucidesParPoisson = glucidesTotaux / nbPoissons;
    
    return poissons.map(poisson => ({
      ...poisson,
      proteinesAllouees: proteinesParPoisson,
      glucidesAlloues: glucidesParPoisson,
      gainPrevu: this.calculerGainPoids(proteinesParPoisson, glucidesParPoisson)
    }));
  },
  
  /**
   * Redistribution aux poissons encore affamés
   * @param {Array} poissonsAffames - Poissons encore affamés
   * @param {number} proteinesRestantes - Protéines restantes
   * @param {number} glucidesRestants - Glucides restants
   * @returns {Object} Résultat de la redistribution
   */
  redistribuerAuxAffames(poissonsAffames, proteinesRestantes, glucidesRestants) {
    if (poissonsAffames.length === 0 || (proteinesRestantes <= 0 && glucidesRestants <= 0)) {
      return {
        poissons: poissonsAffames,
        proteinesRestantes,
        glucidesRestants
      };
    }
    
    let poissonsTraites = [...poissonsAffames];
    let proteinesDisponibles = proteinesRestantes;
    let glucidesDisponibles = glucidesRestants;
    
    // Redistribuer équitablement
    while (proteinesDisponibles > 0 || glucidesDisponibles > 0) {
      const nbAffames = poissonsTraites.filter(p => !p.estRassasie).length;
      if (nbAffames === 0) break;
      
      const proteinesParPoisson = proteinesDisponibles / nbAffames;
      const glucidesParPoisson = glucidesDisponibles / nbAffames;
      
      poissonsTraites = poissonsTraites.map(poisson => {
        if (poisson.estRassasie) return poisson;
        
        const proteinesSupplementaires = Math.min(proteinesParPoisson, 2 - poisson.proteinesRecues);
        const glucidesSupplementaires = Math.min(glucidesParPoisson, 4 - poisson.glucidesRecus);
        
        const proteinesTotal = poisson.proteinesRecues + proteinesSupplementaires;
        const glucidesTotal = poisson.glucidesRecus + glucidesSupplementaires;
        
        proteinesDisponibles -= proteinesSupplementaires;
        glucidesDisponibles -= glucidesSupplementaires;
        
        // Vérifier si le poisson est maintenant rassasié
        const estMaintenantRassasie = proteinesTotal >= 2 && glucidesTotal >= 4;
        
        return {
          ...poisson,
          proteinesRecues: proteinesTotal,
          glucidesRecus: glucidesTotal,
          estRassasie: estMaintenantRassasie,
          gainEffectif: this.calculerGainPoids(proteinesTotal, glucidesTotal)
        };
      });
    }
    
    return {
      poissons: poissonsTraites,
      proteinesRestantes: Math.max(0, proteinesDisponibles),
      glucidesRestants: Math.max(0, glucidesDisponibles)
    };
  },
  
  /**
   * Calcul du coût du nourrissage
   * @param {number} quantitePlatKg - Quantité de plat en kg
   * @param {number} prixParKg - Prix de l'aliment par kg
   * @returns {number} Coût total en MGA
   */
  calculerCoutNourrissage(quantitePlatKg, prixParKg) {
    return Math.round(quantitePlatKg * prixParKg);
  },
  
  /**
   * Estimation du nombre de jours restants
   * @param {number} poidsActuel - Poids actuel en g
   * @param {number} poidsMax - Poids maximum en g
   * @param {number} gainJournalierMoyen - Gain journalier moyen en g
   * @returns {number} Nombre estimé de jours
   */
  estimerJoursRestants(poidsActuel, poidsMax, gainJournalierMoyen) {
    if (gainJournalierMoyen <= 0) return Infinity;
    const poidsRestant = poidsMax - poidsActuel;
    return Math.ceil(poidsRestant / gainJournalierMoyen);
  }
};




