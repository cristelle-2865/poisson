// calculService.js - VERSION COMPLÈTE AVEC LIPIDES ET VITAMINES

/**
 * Service de calculs métiers pour la pisciculture
 * Implémente exactement les règles du cahier des charges
 * NOUVEAU : Inclut maintenant les lipides et vitamines
 */
export const calculService = {
  
  /**
   * Calcul des apports nutritionnels totaux selon la règle de trois
   * @param {number} quantitePlatKg - Quantité de plat en kg
   * @param {number} proteinesParKg - Protéines par kg d'aliment (g/kg)
   * @param {number} glucidesParKg - Glucides par kg d'aliment (g/kg)
   * @param {number} lipidesParKg - Lipides par kg d'aliment (g/kg) - NOUVEAU
   * @param {number} vitaminesParKg - Vitamines par kg d'aliment (g/kg) - NOUVEAU
   * @returns {Object} Apports totaux en nutriments
   */
  calculerApportsTotaux(quantitePlatKg, proteinesParKg, glucidesParKg, lipidesParKg = 5, vitaminesParKg = 0.5) {
    // Règle de trois : 
    // Pour 5g de plat → 2g protéines, 4g glucides, 1g lipides, 0.5g vitamines
    // Donc pour quantitePlatKg kg de plat :
    const platEnGrammes = quantitePlatKg * 1000; // Convertir en grammes
    
    const proteinesTotal = (platEnGrammes * proteinesParKg) / 1000;
    const glucidesTotal = (platEnGrammes * glucidesParKg) / 1000;
    const lipidesTotal = (platEnGrammes * (lipidesParKg || 5)) / 1000; // NOUVEAU
    const vitaminesTotal = (platEnGrammes * (vitaminesParKg || 0.5)) / 1000; // NOUVEAU
    
    return {
      proteinesTotal: Math.round(proteinesTotal * 100) / 100,
      glucidesTotal: Math.round(glucidesTotal * 100) / 100,
      lipidesTotal: Math.round(lipidesTotal * 100) / 100, // NOUVEAU
      vitaminesTotal: Math.round(vitaminesTotal * 100) / 100 // NOUVEAU
    };
  },
  
  /**
   * Calcul du gain de poids selon les règles exactes du cahier des charges
   * VERSION COMPLÈTE avec lipides et vitamines
   * @param {number} proteinesRecues - Protéines reçues en grammes
   * @param {number} glucidesRecus - Glucides reçus en grammes
   * @param {number} lipidesRecus - Lipides reçus en grammes (NOUVEAU)
   * @param {number} vitaminesRecues - Vitamines reçues en grammes (NOUVEAU)
   * @returns {number} Gain de poids en grammes
   */
  calculerGainPoidsComplet(proteinesRecues, glucidesRecus, lipidesRecus = 0, vitaminesRecues = 0) {
    // Besoins quotidiens standards :
    const BESOIN_PROTEINES = 2.0;
    const BESOIN_GLUCIDES = 4.0;
    const BESOIN_LIPIDES = 1.0;    // NOUVEAU
    const BESOIN_VITAMINES = 0.5;  // NOUVEAU
    
    // Facteurs de conversion (basés sur la valeur nutritionnelle) :
    const FACTEUR_PROTEINES = 0.8;  // 1g protéine → 0.8g gain
    const FACTEUR_GLUCIDES = 0.5;   // 1g glucide → 0.5g gain
    const FACTEUR_LIPIDES = 0.9;    // 1g lipide → 0.9g gain (énergie dense)
    const FACTEUR_VITAMINES = 0.1;  // 1g vitamine → 0.1g gain (impact indirect)
    
    // Limiter l'apport aux besoins (au-delà n'apporte pas plus de gain)
    const proteinesEfficaces = Math.min(proteinesRecues, BESOIN_PROTEINES * 2); // Max 2x besoins
    const glucidesEfficaces = Math.min(glucidesRecus, BESOIN_GLUCIDES * 2);
    const lipidesEfficaces = Math.min(lipidesRecus, BESOIN_LIPIDES * 2);
    const vitaminesEfficaces = Math.min(vitaminesRecues, BESOIN_VITAMINES * 2);
    
    // Calcul du gain de chaque nutriment
    let gainTotal = 0;
    
    // Gain des protéines (diminution marginale au-delà des besoins)
    if (proteinesEfficaces <= BESOIN_PROTEINES) {
      gainTotal += proteinesEfficaces * FACTEUR_PROTEINES;
    } else {
      // Au-delà des besoins, rendement décroissant
      const excedent = proteinesEfficaces - BESOIN_PROTEINES;
      gainTotal += (BESOIN_PROTEINES * FACTEUR_PROTEINES) + 
                   (excedent * FACTEUR_PROTEINES * 0.5); // 50% d'efficacité
    }
    
    // Gain des glucides
    if (glucidesEfficaces <= BESOIN_GLUCIDES) {
      gainTotal += glucidesEfficaces * FACTEUR_GLUCIDES;
    } else {
      const excedent = glucidesEfficaces - BESOIN_GLUCIDES;
      gainTotal += (BESOIN_GLUCIDES * FACTEUR_GLUCIDES) + 
                   (excedent * FACTEUR_GLUCIDES * 0.4); // 40% d'efficacité
    }
    
    // Gain des lipides (NOUVEAU)
    if (lipidesEfficaces <= BESOIN_LIPIDES) {
      gainTotal += lipidesEfficaces * FACTEUR_LIPIDES;
    } else {
      const excedent = lipidesEfficaces - BESOIN_LIPIDES;
      gainTotal += (BESOIN_LIPIDES * FACTEUR_LIPIDES) + 
                   (excedent * FACTEUR_LIPIDES * 0.6); // 60% d'efficacité
    }
    
    // Gain des vitamines (NOUVEAU)
    if (vitaminesEfficaces <= BESOIN_VITAMINES) {
      gainTotal += vitaminesEfficaces * FACTEUR_VITAMINES;
    } else {
      const excedent = vitaminesEfficaces - BESOIN_VITAMINES;
      gainTotal += (BESOIN_VITAMINES * FACTEUR_VITAMINES) + 
                   (excedent * FACTEUR_VITAMINES * 0.3); // 30% d'efficacité
    }
    
    // Bonus synergique si tous les besoins sont satisfaits
    const besoinsSatisfaits = proteinesEfficaces >= BESOIN_PROTEINES &&
                             glucidesEfficaces >= BESOIN_GLUCIDES &&
                             lipidesEfficaces >= BESOIN_LIPIDES &&
                             vitaminesEfficaces >= BESOIN_VITAMINES;
    
    if (besoinsSatisfaits) {
      gainTotal *= 1.1; // +10% de gain si tous besoins satisfaits
    }
    
    return Math.round(gainTotal * 100) / 100; // Arrondi à 2 décimales
  },
  
  /**
   * Version simplifiée pour compatibilité (seulement protéines et glucides)
   */
  calculerGainPoids(proteinesRecues, glucidesRecus) {
    return this.calculerGainPoidsComplet(proteinesRecues, glucidesRecus, 0, 0);
  },
  
  /**
   * Calcul des nutriments fournis par un aliment spécifique
   * @param {number} quantiteG - Quantité d'aliment en grammes
   * @param {Object} aliment - Objet aliment avec valeurs par kg
   * @returns {Object} Tous les nutriments fournis
   */
  calculerNutrimentsAlimentComplet(quantiteG, aliment) {
    if (!aliment) return { proteines: 0, glucides: 0, lipides: 0, vitamines: 0 };
    
    // Convertir g/kg en g/g
    const proteinesParGramme = (aliment.proteinesParKg || 0) / 1000;
    const glucidesParGramme = (aliment.glucidesParKg || 0) / 1000;
    const lipidesParGramme = (aliment.lipidesParKg || 5) / 1000; // NOUVEAU
    const vitaminesParGramme = (aliment.vitaminesParKg || 0.5) / 1000; // NOUVEAU
    
    const proteines = proteinesParGramme * quantiteG;
    const glucides = glucidesParGramme * quantiteG;
    const lipides = lipidesParGramme * quantiteG; // NOUVEAU
    const vitamines = vitaminesParGramme * quantiteG; // NOUVEAU
    
    return {
      proteines: Math.round(proteines * 100) / 100,
      glucides: Math.round(glucides * 100) / 100,
      lipides: Math.round(lipides * 100) / 100, // NOUVEAU
      vitamines: Math.round(vitamines * 100) / 100 // NOUVEAU
    };
  },
  
  /**
   * Version simplifiée pour compatibilité
   */
  calculerNutrimentsAliment(quantiteG, aliment) {
    const result = this.calculerNutrimentsAlimentComplet(quantiteG, aliment);
    return { proteines: result.proteines, glucides: result.glucides };
  },
  
  /**
   * Calcul de la satisfaction nutritionnelle en pourcentage
   * VERSION COMPLÈTE avec lipides et vitamines
   * @param {number} proteinesRecues - Protéines reçues en g
   * @param {number} glucidesRecus - Glucides reçus en g
   * @param {number} lipidesRecus - Lipides reçus en g (NOUVEAU)
   * @param {number} vitaminesRecues - Vitamines reçues en g (NOUVEAU)
   * @returns {Object} Pourcentages de satisfaction pour tous les nutriments
   */
  calculerSatisfactionComplet(proteinesRecues, glucidesRecus, lipidesRecus = 0, vitaminesRecues = 0) {
    const besoinProteines = 2;  // g par jour
    const besoinGlucides = 4;   // g par jour
    const besoinLipides = 1;    // g par jour (NOUVEAU)
    const besoinVitamines = 0.5; // g par jour (NOUVEAU)
    
    const satisfactionProteines = Math.min((proteinesRecues / besoinProteines) * 100, 200); // Max 200%
    const satisfactionGlucides = Math.min((glucidesRecus / besoinGlucides) * 100, 200);
    const satisfactionLipides = Math.min((lipidesRecus / besoinLipides) * 100, 200); // NOUVEAU
    const satisfactionVitamines = Math.min((vitaminesRecues / besoinVitamines) * 100, 200); // NOUVEAU
    
    const moyenne = (satisfactionProteines + satisfactionGlucides + 
                    satisfactionLipides + satisfactionVitamines) / 4;
    
    return {
      proteines: Math.round(satisfactionProteines),
      glucides: Math.round(satisfactionGlucides),
      lipides: Math.round(satisfactionLipides), // NOUVEAU
      vitamines: Math.round(satisfactionVitamines), // NOUVEAU
      moyenne: Math.round(moyenne),
      tousSatisfaits: satisfactionProteines >= 100 && 
                     satisfactionGlucides >= 100 && 
                     satisfactionLipides >= 100 && 
                     satisfactionVitamines >= 100
    };
  },
  
  /**
   * Version simplifiée pour compatibilité
   */
  calculerSatisfaction(proteinesRecues, glucidesRecus) {
    const result = this.calculerSatisfactionComplet(proteinesRecues, glucidesRecus, 0, 0);
    return {
      proteines: result.proteines,
      glucides: result.glucides,
      moyenne: result.moyenne
    };
  },
  
  /**
   * Distribution équitable des nutriments aux poissons
   * VERSION COMPLÈTE
   * @param {Array} poissons - Liste des poissons
   * @param {number} proteinesTotales - Protéines totales disponibles
   * @param {number} glucidesTotaux - Glucides totaux disponibles
   * @param {number} lipidesTotaux - Lipides totaux disponibles (NOUVEAU)
   * @param {number} vitaminesTotales - Vitamines totales disponibles (NOUVEAU)
   * @returns {Array} Poissons avec leurs allocations complètes
   */
  distribuerNutrimentsComplet(poissons, proteinesTotales, glucidesTotaux, lipidesTotaux = 0, vitaminesTotales = 0) {
    const nbPoissons = poissons.length;
    if (nbPoissons === 0) return poissons;
    
    const proteinesParPoisson = proteinesTotales / nbPoissons;
    const glucidesParPoisson = glucidesTotaux / nbPoissons;
    const lipidesParPoisson = lipidesTotaux / nbPoissons; // NOUVEAU
    const vitaminesParPoisson = vitaminesTotales / nbPoissons; // NOUVEAU
    
    return poissons.map(poisson => ({
      ...poisson,
      proteinesAllouees: proteinesParPoisson,
      glucidesAlloues: glucidesParPoisson,
      lipidesAlloues: lipidesParPoisson, // NOUVEAU
      vitaminesAllouees: vitaminesParPoisson, // NOUVEAU
      gainPrevu: this.calculerGainPoidsComplet(
        proteinesParPoisson, 
        glucidesParPoisson, 
        lipidesParPoisson, 
        vitaminesParPoisson
      )
    }));
  },
  
  /**
   * Version simplifiée pour compatibilité
   */
  distribuerNutriments(poissons, proteinesTotales, glucidesTotaux) {
    return this.distribuerNutrimentsComplet(poissons, proteinesTotales, glucidesTotaux, 0, 0);
  },
  
  /**
   * Évaluation de la qualité nutritionnelle d'un aliment
   * NOUVELLE FONCTION
   * @param {Object} aliment - L'aliment à évaluer
   * @returns {Object} Scores de qualité
   */
  evaluerQualiteAliment(aliment) {
    if (!aliment) return { score: 0, qualite: 'Inconnue', recommandation: '' };
    
    // Poids des nutriments dans le score total
    const POIDS_PROTEINES = 0.35;
    const POIDS_GLUCIDES = 0.25;
    const POIDS_LIPIDES = 0.25;
    const POIDS_VITAMINES = 0.15;
    
    // Valeurs de référence (g/kg)
    const REF_PROTEINES = 30;
    const REF_GLUCIDES = 20;
    const REF_LIPIDES = 10;
    const REF_VITAMINES = 1;
    
    // Calcul des scores individuels (0-100)
    const scoreProteines = Math.min((aliment.proteinesParKg / REF_PROTEINES) * 100, 100);
    const scoreGlucides = Math.min((aliment.glucidesParKg / REF_GLUCIDES) * 100, 100);
    const scoreLipides = Math.min(((aliment.lipidesParKg || 0) / REF_LIPIDES) * 100, 100);
    const scoreVitamines = Math.min(((aliment.vitaminesParKg || 0) / REF_VITAMINES) * 100, 100);
    
    // Score total pondéré
    const scoreTotal = Math.round(
      (scoreProteines * POIDS_PROTEINES) +
      (scoreGlucides * POIDS_GLUCIDES) +
      (scoreLipides * POIDS_LIPIDES) +
      (scoreVitamines * POIDS_VITAMINES)
    );
    
    // Détermination de la qualité
    let qualite, recommandation;
    if (scoreTotal >= 90) {
      qualite = 'Excellent';
      recommandation = 'Aliment complet, parfait pour une croissance optimale';
    } else if (scoreTotal >= 75) {
      qualite = 'Bon';
      recommandation = 'Aliment équilibré, convient à la plupart des poissons';
    } else if (scoreTotal >= 50) {
      qualite = 'Moyen';
      recommandation = 'Peut nécessiter une supplémentation';
    } else if (scoreTotal >= 25) {
      qualite = 'Faible';
      recommandation = 'À utiliser en complément d\'autres aliments';
    } else {
      qualite = 'Très faible';
      recommandation = 'Ne convient pas comme aliment principal';
    }
    
    return {
      score: scoreTotal,
      qualite,
      recommandation,
      details: {
        proteines: scoreProteines,
        glucides: scoreGlucides,
        lipides: scoreLipides,
        vitamines: scoreVitamines
      }
    };
  },
  
  /**
   * Calcul du coût-efficacité d'un aliment
   * NOUVELLE FONCTION
   * @param {Object} aliment - L'aliment à analyser
   * @returns {Object} Métriques de coût-efficacité
   */
  calculerCoutEfficacite(aliment) {
    if (!aliment || !aliment.prixKgAliment) {
      return { coutParGain: 0, efficacite: 0, rentabilite: 'Indéterminée' };
    }
    
    // Calcul de la valeur nutritionnelle théorique (pour 1kg)
    const proteinesParKg = aliment.proteinesParKg || 0;
    const glucidesParKg = aliment.glucidesParKg || 0;
    const lipidesParKg = aliment.lipidesParKg || 5;
    const vitaminesParKg = aliment.vitaminesParKg || 0.5;
    
    // Gain théorique pour 1kg d'aliment
    const gainParKg = this.calculerGainPoidsComplet(
      proteinesParKg, 
      glucidesParKg, 
      lipidesParKg, 
      vitaminesParKg
    );
    
    // Coût par gramme de gain
    const coutParGain = aliment.prixKgAliment / gainParKg;
    
    // Efficacité nutritionnelle (0-100)
    const efficacite = Math.min((gainParKg / 50) * 100, 100);
    
    // Rentabilité
    let rentabilite;
    if (coutParGain < 10) rentabilite = 'Très rentable';
    else if (coutParGain < 20) rentabilite = 'Rentable';
    else if (coutParGain < 30) rentabilite = 'Moyenne';
    else if (coutParGain < 40) rentabilite = 'Peu rentable';
    else rentabilite = 'Non rentable';
    
    return {
      coutParGain: Math.round(coutParGain * 100) / 100,
      efficacite: Math.round(efficacite),
      rentabilite,
      gainParKg: Math.round(gainParKg * 100) / 100,
      details: {
        proteines: proteinesParKg,
        glucides: glucidesParKg,
        lipides: lipidesParKg,
        vitamines: vitaminesParKg
      }
    };
  },
  
  /**
   * Redistribution aux poissons encore affamés (version complète)
   */
  redistribuerAuxAffames(poissonsAffames, proteinesRestantes, glucidesRestants, lipidesRestants = 0, vitaminesRestantes = 0) {
    if (poissonsAffames.length === 0 || 
        (proteinesRestantes <= 0 && glucidesRestants <= 0 && 
         lipidesRestants <= 0 && vitaminesRestantes <= 0)) {
      return {
        poissons: poissonsAffames,
        proteinesRestantes,
        glucidesRestants,
        lipidesRestants,
        vitaminesRestantes
      };
    }
    
    let poissonsTraites = [...poissonsAffames];
    let proteinesDisponibles = proteinesRestantes;
    let glucidesDisponibles = glucidesRestants;
    let lipidesDisponibles = lipidesRestants; // NOUVEAU
    let vitaminesDisponibles = vitaminesRestantes; // NOUVEAU
    
    // Redistribuer équitablement
    while (proteinesDisponibles > 0 || glucidesDisponibles > 0 || 
           lipidesDisponibles > 0 || vitaminesDisponibles > 0) {
      const nbAffames = poissonsTraites.filter(p => !p.estRassasie).length;
      if (nbAffames === 0) break;
      
      const proteinesParPoisson = proteinesDisponibles / nbAffames;
      const glucidesParPoisson = glucidesDisponibles / nbAffames;
      const lipidesParPoisson = lipidesDisponibles / nbAffames; // NOUVEAU
      const vitaminesParPoisson = vitaminesDisponibles / nbAffames; // NOUVEAU
      
      poissonsTraites = poissonsTraites.map(poisson => {
        if (poisson.estRassasie) return poisson;
        
        // Allocation supplémentaire pour chaque nutriment
        const proteinesSupplementaires = Math.min(proteinesParPoisson, 2 - (poisson.proteinesRecues || 0));
        const glucidesSupplementaires = Math.min(glucidesParPoisson, 4 - (poisson.glucidesRecus || 0));
        const lipidesSupplementaires = Math.min(lipidesParPoisson, 1 - (poisson.lipidesRecus || 0)); // NOUVEAU
        const vitaminesSupplementaires = Math.min(vitaminesParPoisson, 0.5 - (poisson.vitaminesRecues || 0)); // NOUVEAU
        
        // Mettre à jour les totaux
        const proteinesTotal = (poisson.proteinesRecues || 0) + proteinesSupplementaires;
        const glucidesTotal = (poisson.glucidesRecus || 0) + glucidesSupplementaires;
        const lipidesTotal = (poisson.lipidesRecus || 0) + lipidesSupplementaires; // NOUVEAU
        const vitaminesTotal = (poisson.vitaminesRecues || 0) + vitaminesSupplementaires; // NOUVEAU
        
        // Mettre à jour les disponibilités
        proteinesDisponibles -= proteinesSupplementaires;
        glucidesDisponibles -= glucidesSupplementaires;
        lipidesDisponibles -= lipidesSupplementaires; // NOUVEAU
        vitaminesDisponibles -= vitaminesSupplementaires; // NOUVEAU
        
        // Vérifier si le poisson est maintenant rassasié
        const estMaintenantRassasie = proteinesTotal >= 2 && 
                                      glucidesTotal >= 4 && 
                                      lipidesTotal >= 1 && 
                                      vitaminesTotal >= 0.5;
        
        return {
          ...poisson,
          proteinesRecues: proteinesTotal,
          glucidesRecus: glucidesTotal,
          lipidesRecus: lipidesTotal, // NOUVEAU
          vitaminesRecues: vitaminesTotal, // NOUVEAU
          estRassasie: estMaintenantRassasie,
          gainEffectif: this.calculerGainPoidsComplet(
            proteinesTotal, 
            glucidesTotal, 
            lipidesTotal, 
            vitaminesTotal
          )
        };
      });
    }
    
    return {
      poissons: poissonsTraites,
      proteinesRestantes: Math.max(0, proteinesDisponibles),
      glucidesRestants: Math.max(0, glucidesDisponibles),
      lipidesRestants: Math.max(0, lipidesDisponibles), // NOUVEAU
      vitaminesRestantes: Math.max(0, vitaminesDisponibles) // NOUVEAU
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
  },
  
  /**
   * NOUVELLE : Calcul des ratios nutritionnels idéaux
   * @returns {Object} Ratios recommandés pour une croissance optimale
   */
  getRatiosNutrimentsIdeaux() {
    return {
      proteinesGlucides: '1:2', // 1g protéines pour 2g glucides
      proteinesLipides: '2:1',  // 2g protéines pour 1g lipides
      energieTotale: '40% protéines, 40% glucides, 20% lipides',
      vitamines: '0.5% du poids total des nutriments',
      recommandations: [
        'Maintenir un ratio protéines/glucides proche de 1:2',
        'Inclure au moins 1g de lipides par jour',
        'Ne pas dépasser 0.5g de vitamines par jour',
        'Ajuster selon la température de l\'eau'
      ]
    };
  },
  
  /**
   * NOUVELLE : Vérification de l'équilibre nutritionnel d'un plat
   * @param {Object} plat - Le plat à analyser
   * @returns {Object} Analyse d'équilibre
   */
  analyserEquilibrePlat(plat) {
    if (!plat || !plat.compositions || plat.compositions.length === 0) {
      return { equilibre: 'Incomplet', score: 0, recommendations: [] };
    }
    
    // Calculer les totaux
    let totalProteines = 0;
    let totalGlucides = 0;
    let totalLipides = 0;
    let totalVitamines = 0;
    
    plat.compositions.forEach(comp => {
      const nutriments = this.calculerNutrimentsAlimentComplet(
        comp.poidsAlimentComposition * 1000, // Convertir kg en g
        comp.aliment
      );
      
      totalProteines += nutriments.proteines;
      totalGlucides += nutriments.glucides;
      totalLipides += nutriments.lipides;
      totalVitamines += nutriments.vitamines;
    });
    
    // Vérifier les ratios
    const ratios = this.getRatiosNutrimentsIdeaux();
    const recommendations = [];
    let score = 100;
    
    // Vérifier ratio protéines/glucides
    const ratioPG = totalGlucides > 0 ? totalProteines / totalGlucides : 0;
    if (ratioPG < 0.4) {
      recommendations.push('Trop de glucides par rapport aux protéines');
      score -= 20;
    } else if (ratioPG > 0.6) {
      recommendations.push('Trop de protéines par rapport aux glucides');
      score -= 20;
    }
    
    // Vérifier présence de lipides
    if (totalLipides < 0.5) {
      recommendations.push('Lipides insuffisants (< 0.5g)');
      score -= 15;
    }
    
    // Vérifier présence de vitamines
    if (totalVitamines < 0.2) {
      recommendations.push('Vitamines insuffisantes (< 0.2g)');
      score -= 10;
    }
    
    // Vérifier excès de vitamines
    if (totalVitamines > 1) {
      recommendations.push('Excès de vitamines (> 1g)');
      score -= 5;
    }
    
    // Déterminer l'équilibre
    let equilibre;
    if (score >= 90) equilibre = 'Excellent';
    else if (score >= 75) equilibre = 'Bon';
    else if (score >= 60) equilibre = 'Acceptable';
    else if (score >= 40) equilibre = 'Faible';
    else equilibre = 'Déséquilibré';
    
    return {
      equilibre,
      score: Math.round(score),
      recommendations,
      totaux: {
        proteines: Math.round(totalProteines * 100) / 100,
        glucides: Math.round(totalGlucides * 100) / 100,
        lipides: Math.round(totalLipides * 100) / 100,
        vitamines: Math.round(totalVitamines * 100) / 100
      }
    };
  }
};

