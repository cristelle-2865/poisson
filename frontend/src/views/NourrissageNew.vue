<!-- NourrissageCombined.vue - VERSION COMPLÈTE AVEC LIPIDES ET VITAMINES -->
<template>
  <div class="nourrissage-page">
    <div class="page-header">
      <h1>
        Nourrissage des Poissons
        <span v-if="stats.bassinNom" class="bassin-info">
          - Bassin: {{ stats.bassinNom }}
        </span>
      </h1>
      <div class="header-info">
        <div class="info-item">
          <span class="label">Poissons affamés:</span>
          <span class="value hungry">{{ stats.poissonsAffames || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">Besoin total:</span>
          <span class="value">{{ besoinTotal }} g</span>
        </div>
        <div class="info-item">
          <span class="label">Coût estimé:</span>
          <span class="value">{{ formatPrice(coutTotal) }} MGA</span>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- Formulaire de nourrissage -->
      <div class="form-section">
        <div class="card">
          <h2>Nouveau Nourrissage</h2>
          
          <!-- Mode de sélection : Plat ou Aliment -->
          <div class="mode-select-section">
            <h3>1. Mode de nourrissage</h3>
            <div class="mode-select-options">
              <div class="mode-option" 
                   :class="{ selected: modeNourrissage === 'aliment' }"
                   @click="modeNourrissage = 'aliment'">
                <div class="mode-icon">🥫</div>
                <div class="mode-content">
                  <h4>Aliment simple</h4>
                  <p>Utiliser un aliment directement depuis le stock</p>
                </div>
              </div>
              
              <div class="mode-option" 
                   :class="{ selected: modeNourrissage === 'plat' }"
                   @click="modeNourrissage = 'plat'">
                <div class="mode-icon">🍽️</div>
                <div class="mode-content">
                  <h4>Plat préparé</h4>
                  <p>Utiliser un plat déjà composé</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Sélection de PLAT (avec lipides et vitamines) -->
          <div v-if="modeNourrissage === 'plat'" class="plat-section">
            <h3>2. Sélectionnez un plat</h3>
            <div v-if="platsDisponibles.length === 0" class="no-data">
              <p>Aucun plat disponible. Créez d'abord des plats dans la section "Plats".</p>
            </div>
            <div v-else class="plat-options">
              <div v-for="plat in platsDisponibles" :key="plat.idPlat" 
                   class="plat-option" 
                   :class="{ 
                     selected: selectedPlatId === plat.idPlat,
                     'unavailable': plat.poidsTotalPlat <= 0
                   }"
                   @click="selectPlat(plat)"
                   :title="plat.poidsTotalPlat <= 0 ? 'Plat épuisé' : ''">
                
                <div class="plat-icon">🍽️</div>
                <div class="plat-info">
                  <h4>{{ plat.nomPlat }}</h4>
                  <div class="plat-status" :class="{
                    'available': plat.poidsTotalPlat > 0,
                    'unavailable': plat.poidsTotalPlat <= 0
                  }">
                    {{ plat.poidsTotalPlat > 0 ? 'Disponible' : 'Épuisé' }}
                  </div>
                  <div class="plat-details">
                    <span><strong>Quantité:</strong> {{ formatStock(plat.poidsTotalPlat) }} kg</span>
                    <span><strong>Protéines:</strong> {{ formatNutrient(plat.proteinesParKgPlat) }} g/kg</span>
                    <span><strong>Glucides:</strong> {{ formatNutrient(plat.glucidesParKgPlat) }} g/kg</span>
                    
                    <!-- AJOUT : Lipides et Vitamines -->
                    <span><strong>Lipides:</strong> {{ formatNutrient(plat.lipidesParKgPlat || 0) }} g/kg</span>
                    <span><strong>Vitamines:</strong> {{ formatNutrient(plat.vitaminesParKgPlat || 0, 2) }} g/kg</span>
                    
                    <span><strong>Coût:</strong> {{ formatPrice(plat.coutTotalPlat) }} MGA</span>
                  </div>
                  
                  <!-- Indicateur de qualité du plat -->
                  <div v-if="platQuality[plat.idPlat]" class="plat-quality-badge" :class="platQuality[plat.idPlat].class">
                    <span class="quality-icon">{{ platQuality[plat.idPlat].icon }}</span>
                    <span class="quality-text">{{ platQuality[plat.idPlat].text }}</span>
                  </div>
                </div>
                
                <!-- Indicateur de sélection -->
                <div v-if="selectedPlatId === plat.idPlat" class="selection-indicator">
                  ✓ Sélectionné
                </div>
              </div>
            </div>
            
            <!-- Détails du plat sélectionné avec tous les nutriments -->
            <div v-if="selectedPlat" class="plat-details-section">
              <h4>Détails du plat sélectionné</h4>
              <div class="plat-details-card">
                <div class="plat-detail-row">
                  <span class="detail-label">Nom:</span>
                  <span class="detail-value">{{ selectedPlat.nomPlat }}</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Quantité totale:</span>
                  <span class="detail-value">{{ formatStock(selectedPlat.poidsTotalPlat) }} kg</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Protéines par kg:</span>
                  <span class="detail-value protein">{{ formatNutrient(selectedPlat.proteinesParKgPlat) }} g</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Glucides par kg:</span>
                  <span class="detail-value carbs">{{ formatNutrient(selectedPlat.glucidesParKgPlat) }} g</span>
                </div>
                
                <!-- AJOUT : Lipides et Vitamines -->
                <div class="plat-detail-row">
                  <span class="detail-label">Lipides par kg:</span>
                  <span class="detail-value lipid">{{ formatNutrient(selectedPlat.lipidesParKgPlat || 0) }} g</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Vitamines par kg:</span>
                  <span class="detail-value vitamin">{{ formatNutrient(selectedPlat.vitaminesParKgPlat || 0, 2) }} g</span>
                </div>
                
                <div class="plat-detail-row">
                  <span class="detail-label">Coût total:</span>
                  <span class="detail-value price">{{ formatPrice(selectedPlat.coutTotalPlat) }} MGA</span>
                </div>
                
                <!-- AJOUT : Score de qualité -->
                <div v-if="platQuality[selectedPlat.idPlat]" class="plat-detail-row quality">
                  <span class="detail-label">Qualité:</span>
                  <span class="detail-value" :class="platQuality[selectedPlat.idPlat].class">
                    {{ platQuality[selectedPlat.idPlat].text }} ({{ platQuality[selectedPlat.idPlat].score }}/100)
                  </span>
                </div>
              </div>
              
              <!-- Composition du plat avec lipides et vitamines -->
              <div v-if="selectedPlat.compositions && selectedPlat.compositions.length > 0" class="composition-section">
                <h5>Composition détaillée:</h5>
                <div class="composition-table">
                  <div class="composition-header">
                    <span>Aliment</span>
                    <span>Quantité</span>
                    <span>Protéines</span>
                    <span>Glucides</span>
                    <span>Lipides</span>
                    <span>Vitamines</span>
                    <span>Coût</span>
                  </div>
                  <div v-for="comp in selectedPlat.compositions" :key="comp.idCompositionPlat" class="composition-row">
                    <span class="composition-name">{{ comp.nomAliment }}</span>
                    <span class="composition-quantity">{{ formatStock(comp.poidsAlimentComposition) }} kg</span>
                    <span class="composition-protein">{{ formatNutrient(comp.proteinesComposition || 0) }} g</span>
                    <span class="composition-carbs">{{ formatNutrient(comp.glucidesComposition || 0) }} g</span>
                    
                    <!-- AJOUT : Lipides et Vitamines -->
                    <span class="composition-lipid">{{ formatNutrient(comp.lipidesComposition || 0) }} g</span>
                    <span class="composition-vitamin">{{ formatNutrient(comp.vitaminesComposition || 0, 2) }} g</span>
                    
                    <span class="composition-cost">{{ formatPrice(comp.coutAlimentComposition || 0) }} MGA</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Sélection d'ALIMENT (avec lipides et vitamines) -->
          <div v-else-if="modeNourrissage === 'aliment'" class="food-section">
            <h3>2. Sélectionnez l'aliment</h3>
            <div class="aliment-options">
              <div v-for="aliment in aliments" :key="aliment.idAliment" 
                   class="aliment-option" 
                   :class="{ selected: selectedAlimentId === aliment.idAliment }"
                   @click="selectAliment(aliment)">
                <div class="aliment-icon">🥫</div>
                <div class="aliment-info">
                  <h4>{{ aliment.nomAliment }}</h4>
                  <div class="aliment-details-grid">
                    <span class="detail protein">💪 P: {{ formatNutrient(aliment.proteinesParKgAliment) }} g/kg</span>
                    <span class="detail carbs">🍚 G: {{ formatNutrient(aliment.glucidesParKgAliment) }} g/kg</span>
                    
                    <!-- AJOUT : Lipides et Vitamines -->
                    <span class="detail lipid">⚡ L: {{ formatNutrient(aliment.lipidesParKgAliment || 5) }} g/kg</span>
                    <span class="detail vitamin">💊 V: {{ formatNutrient(aliment.vitaminesParKgAliment || 0.5, 2) }} g/kg</span>
                    
                    <span class="detail stock">📦 Stock: {{ formatStock(aliment.stockAliment) }} kg</span>
                    <span class="detail price">💰 Prix: {{ formatPrice(aliment.prixKgAliment) }} MGA/kg</span>
                  </div>
                  
                  <!-- AJOUT : Indicateur de qualité -->
                  <div v-if="alimentQuality[aliment.idAliment]" class="aliment-quality-badge" :class="alimentQuality[aliment.idAliment].class">
                    <span class="quality-icon">{{ alimentQuality[aliment.idAliment].icon }}</span>
                    <span class="quality-text">{{ alimentQuality[aliment.idAliment].text }}</span>
                  </div>
                </div>
                <div class="aliment-price-highlight">{{ formatPrice(aliment.prixKgAliment) }} MGA/kg</div>
              </div>
            </div>
            
            <div v-if="showCustomAliment" class="custom-aliment">
              <h4>Aliment personnalisé (avec lipides et vitamines)</h4>
              <div class="custom-inputs-grid">
                <div class="input-group">
                  <label>💪 Protéines (g/kg)</label>
                  <input type="number" v-model="customAliment.proteinesParKg" min="0" step="0.1" class="glass-input">
                </div>
                <div class="input-group">
                  <label>🍚 Glucides (g/kg)</label>
                  <input type="number" v-model="customAliment.glucidesParKg" min="0" step="0.1" class="glass-input">
                </div>
                
                <!-- AJOUT : Lipides et Vitamines pour aliment personnalisé -->
                <div class="input-group">
                  <label>⚡ Lipides (g/kg)</label>
                  <input type="number" v-model="customAliment.lipidesParKg" min="0" step="0.1" class="glass-input">
                  <small class="hint">Valeur par défaut: 5 g/kg</small>
                </div>
                <div class="input-group">
                  <label>💊 Vitamines (g/kg)</label>
                  <input type="number" v-model="customAliment.vitaminesParKg" min="0" step="0.01" class="glass-input">
                  <small class="hint">Valeur par défaut: 0.5 g/kg</small>
                </div>
                
                <div class="input-group">
                  <label>💰 Prix (MGA/kg)</label>
                  <input type="number" v-model="customAliment.prixParKg" min="0" step="100" class="glass-input">
                </div>
              </div>
              
              <!-- AJOUT : Analyse de l'aliment personnalisé -->
              <div v-if="showCustomAlimentPreview" class="custom-preview">
                <h5>Aperçu nutritionnel</h5>
                <div class="preview-stats">
                  <div class="preview-stat">
                    <span class="label">Qualité:</span>
                    <span class="value" :class="customAlimentQuality.class">
                      {{ customAlimentQuality.text }}
                    </span>
                  </div>
                  <div class="preview-stat">
                    <span class="label">Score:</span>
                    <span class="value">{{ customAlimentQuality.score }}/100</span>
                  </div>
                  <div class="preview-stat">
                    <span class="label">Rentabilité:</span>
                    <span class="value" :class="customAlimentRentability.class">
                      {{ customAlimentRentability.text }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <button @click="toggleCustomAliment" class="btn-toggle-custom">
              {{ showCustomAliment ? '← Choisir un aliment existant' : '+ Créer un aliment personnalisé' }}
            </button>
          </div>

          <!-- Quantité et calculs (COMMUN aux deux modes - avec lipides et vitamines) -->
          <div class="quantity-section">
            <h3>3. Définissez la quantité</h3>
            <div class="quantity-control">
              <label>Quantité à utiliser (kg)</label>
              <div class="quantity-input-group">
                <button @click="decrementQuantity" class="btn-quantity" :disabled="quantitePlat <= 0.1">-</button>
                <input type="number" v-model="quantitePlat" min="0.1" step="0.1" 
                       :max="maxQuantite" @input="updateCalculs" class="glass-input">
                <button @click="incrementQuantity" class="btn-quantity" :disabled="quantitePlat >= maxQuantite">+</button>
                <span class="quantity-unit">kg</span>
              </div>
              <div class="quantity-info">
                <small>Valeur actuelle: {{ formatStock(quantitePlat) }} kg ({{ formatStock(quantitePlat * 1000) }} g)</small>
                <small v-if="modeNourrissage === 'plat'" class="stock-warning">
                  Stock plat: {{ formatStock(maxQuantite) }} kg
                </small>
                <small v-else-if="modeNourrissage === 'aliment' && !showCustomAliment" class="stock-warning">
                  Stock disponible: {{ formatStock(maxQuantite) }} kg
                </small>
              </div>
              
              <div class="quantity-slider">
                <input type="range" v-model="quantitePlat" :min="0.1" :max="maxQuantite" 
                       step="0.1" @input="updateCalculs" class="slider">
                <div class="slider-labels">
                  <span>0.1 kg</span>
                  <span>{{ (maxQuantite / 2).toFixed(1) }} kg</span>
                  <span>{{ maxQuantite.toFixed(1) }} kg</span>
                </div>
              </div>
            </div>

            <!-- Calculs nutritionnels complets (avec lipides et vitamines) -->
            <div class="calculations-section">
              <h4>Calculs nutritionnels complets</h4>
              <div class="calculations-grid">
                <div class="calc-item protein">
                  <span class="calc-label">💪 Protéines totales:</span>
                  <span class="calc-value">{{ formatNutrient(proteinesTotales) }} g</span>
                </div>
                <div class="calc-item carbs">
                  <span class="calc-label">🍚 Glucides totales:</span>
                  <span class="calc-value">{{ formatNutrient(glucidesTotales) }} g</span>
                </div>
                
                <!-- AJOUT : Lipides et Vitamines totales -->
                <div class="calc-item lipid">
                  <span class="calc-label">⚡ Lipides totaux:</span>
                  <span class="calc-value">{{ formatNutrient(lipidesTotales) }} g</span>
                </div>
                <div class="calc-item vitamin">
                  <span class="calc-label">💊 Vitamines totales:</span>
                  <span class="calc-value">{{ formatNutrient(vitaminesTotales, 2) }} g</span>
                </div>
                
                <div class="calc-item">
                  <span class="calc-label">🐟 Par poisson:</span>
                  <span class="calc-value">
                    P:{{ formatNutrient(proteinesParPoisson) }}g / 
                    G:{{ formatNutrient(glucidesParPoisson) }}g / 
                    L:{{ formatNutrient(lipidesParPoisson) }}g / 
                    V:{{ formatNutrient(vitaminesParPoisson, 2) }}g
                  </span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">💰 Coût total:</span>
                  <span class="calc-value">{{ formatPrice(coutTotal) }} MGA</span>
                </div>
              </div>
            </div>

            <!-- Gains prévus (avec lipides et vitamines) -->
            <div class="gains-section" v-if="stats.poissonsAffames > 0">
              <h4>Gains de poids prévus</h4>
              <div class="gains-grid">
                <div v-for="scenario in scenariosGainComplets" :key="scenario.id" class="gain-item" :class="scenario.class">
                  <div class="gain-header">
                    <span class="gain-title">{{ scenario.title }}</span>
                    <span class="gain-value">{{ scenario.gain }} g/poisson</span>
                  </div>
                  <div class="gain-details">
                    <span>P:{{ scenario.proteines }}g + G:{{ scenario.glucides }}g + L:{{ scenario.lipides }}g + V:{{ scenario.vitamines }}g</span>
                  </div>
                </div>
              </div>
              
              <!-- AJOUT : Gain actuel calculé -->
              <div class="gain-current">
                <div class="gain-current-header">
                  <span class="gain-current-label">Gain actuel estimé:</span>
                  <span class="gain-current-value">{{ formatNutrient(gainParPoisson) }} g/poisson</span>
                </div>
                <div class="gain-total">
                  <span>Gain total:</span>
                  <span class="total-value">{{ formatNutrient(gainTotalPrevu) }} g</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Distribution (inchangé) -->
          <div class="distribution-section">
            <h3>4. Mode de distribution</h3>
            <div class="distribution-options">
              <div class="distribution-option" 
                   :class="{ selected: distributionMode === 'equitable' }"
                   @click="distributionMode = 'equitable'">
                <div class="option-icon">⚖️</div>
                <div class="option-content">
                  <h5>Répartition équitable</h5>
                  <p>Même quantité pour chaque poisson affamé</p>
                </div>
              </div>
              
              <div class="distribution-option"
                   :class="{ selected: distributionMode === 'proportionnel' }"
                   @click="distributionMode = 'proportionnel'">
                <div class="option-icon">📊</div>
                <div class="option-content">
                  <h5>Proportionnel au poids</h5>
                  <p>Plus pour les gros poissons</p>
                </div>
              </div>
              
              <div class="distribution-option"
                   :class="{ selected: distributionMode === 'prioritaire' }"
                   @click="distributionMode = 'prioritaire'">
                <div class="option-icon">🎯</div>
                <div class="option-content">
                  <h5>Par priorité</h5>
                  <p>Les plus affamés d'abord</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="action-section">
            <button @click="executerNourrissage" :disabled="!canNourrir || loading" class="btn-nourrir">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? 'Nourrissage en cours...' : getActionButtonText() }}
            </button>
            <div class="validation-message" :class="validationClass">
              {{ validationMessage }}
            </div>
          </div>
        </div>
      </div>

      <!-- Panneau de visualisation (avec lipides et vitamines) -->
      <div class="visualization-section">
        <!-- Résumé nutritionnel complet -->
        <div class="card">
          <h2>Résumé nutritionnel complet</h2>
          <div class="nutrition-summary">
            <div class="satisfaction-meters">
              <!-- Protéines -->
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">💪 Protéines</span>
                  <span class="meter-value">{{ satisfaction.proteines }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.proteines + '%' }"
                       :class="getSatisfactionClass(satisfaction.proteines)"></div>
                </div>
                <div class="meter-details">
                  {{ formatNutrient(proteinesParPoisson) }}g reçus / 2g besoins
                </div>
              </div>
              
              <!-- Glucides -->
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">🍚 Glucides</span>
                  <span class="meter-value">{{ satisfaction.glucides }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.glucides + '%' }"
                       :class="getSatisfactionClass(satisfaction.glucides)"></div>
                </div>
                <div class="meter-details">
                  {{ formatNutrient(glucidesParPoisson) }}g reçus / 4g besoins
                </div>
              </div>
              
              <!-- AJOUT : Lipides -->
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">⚡ Lipides</span>
                  <span class="meter-value">{{ satisfaction.lipides }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.lipides + '%' }"
                       :class="getSatisfactionClass(satisfaction.lipides)"></div>
                </div>
                <div class="meter-details">
                  {{ formatNutrient(lipidesParPoisson) }}g reçus / 1g besoins
                </div>
              </div>
              
              <!-- AJOUT : Vitamines -->
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">💊 Vitamines</span>
                  <span class="meter-value">{{ satisfaction.vitamines }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.vitamines + '%' }"
                       :class="getSatisfactionClass(satisfaction.vitamines)"></div>
                </div>
                <div class="meter-details">
                  {{ formatNutrient(vitaminesParPoisson, 2) }}g reçus / 0.5g besoins
                </div>
              </div>
            </div>
            
            <div class="satisfaction-overall">
              <div class="overall-value" :class="getOverallClass(satisfaction.moyenne)">
                {{ satisfaction.moyenne }}%
              </div>
              <div class="overall-label">
                Satisfaction nutritionnelle moyenne
              </div>
              <div class="overall-message">
                {{ getSatisfactionMessage(satisfaction.moyenne) }}
              </div>
              <div v-if="satisfaction.tousSatisfaits" class="overall-success">
                ✅ Tous les besoins nutritionnels sont satisfaits !
              </div>
            </div>
          </div>
        </div>

        <!-- Prévisions de gain (avec analyse complète) -->
        <div class="card">
          <h2>Prévisions de gain</h2>
          <div class="gain-predictions">
            <div class="prediction-chart">
              <div class="chart-bars">
                <div v-for="(gain, index) in gainsParPoisson" :key="index" class="chart-bar-container">
                  <div class="chart-bar" :style="{ height: (gain / 20 * 100) + '%' }"
                       :class="getGainClass(gain)">
                    <span class="bar-value">{{ formatNutrient(gain) }}g</span>
                  </div>
                  <div class="bar-label">P{{ index + 1 }}</div>
                </div>
              </div>
            </div>
            
            <div class="prediction-stats">
              <div class="stat-item">
                <span class="stat-label">Gain minimum:</span>
                <span class="stat-value">{{ formatNutrient(gainMin) }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain maximum:</span>
                <span class="stat-value">{{ formatNutrient(gainMax) }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain moyen:</span>
                <span class="stat-value">{{ formatNutrient(gainMoyen) }} g</span>
              </div>
              <div class="stat-item total">
                <span class="stat-label">Gain total:</span>
                <span class="stat-value">{{ formatNutrient(gainTotalPrevu) }} g</span>
              </div>
            </div>
            
            <!-- AJOUT : Efficacité nutritionnelle -->
            <div class="efficiency-stats">
              <div class="efficiency-item">
                <span class="efficiency-label">Coût par gain:</span>
                <span class="efficiency-value">{{ formatPrice(coutParGain) }} MGA/g</span>
              </div>
              <div class="efficiency-item">
                <span class="efficiency-label">Efficacité:</span>
                <span class="efficiency-value" :class="getEfficiencyClass(efficaciteNutritionnelle)">
                  {{ efficaciteNutritionnelle }}%
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Liste des poissons à nourrir -->
        <div class="card">
          <h2>Poissons à nourrir ({{ poissonsAffames.length }})</h2>
          <div class="fish-list">
            <div v-for="poisson in poissonsAffames" :key="poisson.idPoisson" class="fish-item">
              <div class="fish-main">
                <div class="fish-name">{{ poisson.nomPoisson }}</div>
                <div class="fish-race">{{ poisson.racePoisson?.nomRacePoisson }}</div>
              </div>
              <div class="fish-stats">
                <div class="stat">
                  <span class="label">Poids:</span>
                  <span class="value">{{ formatPoids(poisson.poidsActuelPoisson) }}g</span>
                </div>
                <div class="stat">
                  <span class="label">Max:</span>
                  <span class="value">{{ formatPoids(poisson.poidsMaximalPoisson) }}g</span>
                </div>
                <div class="stat">
                  <span class="label">Prog:</span>
                  <span class="value">{{ getProgression(poisson) }}%</span>
                </div>
              </div>
              <div class="fish-gain">
                <div class="gain-indicator">
                  <div class="gain-bar">
                    <div class="gain-fill" :style="{ width: getAllocation(poisson) + '%' }"></div>
                  </div>
                  <span class="gain-text">{{ formatNutrient(getGainPrevu(poisson)) }}g</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Résultat du nourrissage (avec lipides et vitamines) -->
    <div v-if="resultatNourrissage" class="result-modal">
      <div class="result-content">
        <div class="result-header">
          <h2>✅ Nourrissage terminé</h2>
          <button @click="fermerResultat" class="btn-close">×</button>
        </div>
        
        <div class="result-body">
          <div class="result-icon">🎉</div>
          <div class="result-message">
            <p>{{ resultatNourrissage.message }}</p>
            <p v-if="resultatNourrissage.bassinNom" class="result-bassin">
              Bassin: {{ resultatNourrissage.bassinNom }}
            </p>
          </div>
          
          <div class="result-stats">
            <div class="stat-grid">
              <div class="stat-item">
                <div class="stat-icon">🐟</div>
                <div class="stat-content">
                  <div class="stat-label">Poissons nourris</div>
                  <div class="stat-value">{{ resultatNourrissage.poissonsNourris }}</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">⚖️</div>
                <div class="stat-content">
                  <div class="stat-label">Nourriture utilisée</div>
                  <div class="stat-value">{{ formatStock(resultatNourrissage.nourritureUtilisee) }} kg</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">📈</div>
                <div class="stat-content">
                  <div class="stat-label">Gain total</div>
                  <div class="stat-value">{{ formatNutrient(resultatNourrissage.gainTotal) }} g</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">💰</div>
                <div class="stat-content">
                  <div class="stat-label">Coût total</div>
                  <div class="stat-value">{{ formatPrice(resultatNourrissage.coutTotal) }} MGA</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- AJOUT : Résumé nutritionnel du nourrissage -->
          <div v-if="resultatNourrissage.nutriments" class="result-nutriments">
            <h4>Apports nutritionnels</h4>
            <div class="nutriments-summary">
              <div class="nutriment-item">
                <span class="nutriment-label">💪 Protéines:</span>
                <span class="nutriment-value">{{ formatNutrient(resultatNourrissage.nutriments.proteines) }} g</span>
              </div>
              <div class="nutriment-item">
                <span class="nutriment-label">🍚 Glucides:</span>
                <span class="nutriment-value">{{ formatNutrient(resultatNourrissage.nutriments.glucides) }} g</span>
              </div>
              <div class="nutriment-item">
                <span class="nutriment-label">⚡ Lipides:</span>
                <span class="nutriment-value">{{ formatNutrient(resultatNourrissage.nutriments.lipides) }} g</span>
              </div>
              <div class="nutriment-item">
                <span class="nutriment-label">💊 Vitamines:</span>
                <span class="nutriment-value">{{ formatNutrient(resultatNourrissage.nutriments.vitamines, 2) }} g</span>
              </div>
            </div>
          </div>
          
          <div class="result-details" v-if="resultatNourrissage.details">
            <h4>Détails par poisson</h4>
            <div class="details-list">
              <div v-for="detail in resultatNourrissage.details.slice(0, 5)" :key="detail.id" class="detail-item">
                <span class="detail-name">{{ detail.nom }}</span>
                <span class="detail-gain">{{ formatNutrient(detail.gain) }}g</span>
              </div>
              <div v-if="resultatNourrissage.details.length > 5" class="detail-more">
                et {{ resultatNourrissage.details.length - 5 }} autres...
              </div>
            </div>
          </div>
        </div>
        
        <div class="result-footer">
          <button @click="fermerResultat" class="btn-continue">
            Continuer
          </button>
          <button @click="voirHistorique" class="btn-history">
            Voir l'historique
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import platService from '../services/platService'
import { calculService } from '../services/calculService'

export default {
  name: 'NourrissageCombined',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // Données
    const aliments = ref([])
    const platsDisponibles = ref([])
    const poissonsAffames = ref([])
    const stats = ref({})
    const loading = ref(false)
    const resultatNourrissage = ref(null)
    
    // Caches pour les analyses
    const alimentQuality = ref({})
    const platQuality = ref({})

    // Récupérer l'ID et le nom du bassin depuis les query params
    const bassinId = ref(null)
    const bassinNom = ref('')

    // Sélections
    const modeNourrissage = ref('aliment')
    const selectedAlimentId = ref(null)
    const selectedPlatId = ref(null)
    const showCustomAliment = ref(false)
    const showCustomAlimentPreview = ref(true)
    const quantitePlat = ref(0.1)
    const distributionMode = ref('equitable')
    
    // Aliment personnalisé (avec lipides et vitamines)
    const customAliment = ref({
      proteinesParKg: 10,
      glucidesParKg: 10,
      lipidesParKg: 5,
      vitaminesParKg: 0.5,
      prixParKg: 2000
    })

    // ============== MÉTHODES UTILITAIRES ==============
    
    const formatStock = (stock) => {
      if (!stock && stock !== 0) return '0.00'
      return parseFloat(stock).toFixed(2)
    }

    const formatNutrient = (nutrient, decimals = 1) => {
      if (!nutrient && nutrient !== 0) return '0.0'
      return parseFloat(nutrient).toFixed(decimals)
    }

    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }

    const formatPoids = (poids) => {
      if (!poids) return '0.00'
      return parseFloat(poids).toFixed(2)
    }

    // ============== ANALYSE DE QUALITÉ ==============
    
    const analyserAliment = (aliment) => {
      if (!aliment) return null
      
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: parseFloat(aliment.proteinesParKgAliment || 0),
        glucidesParKg: parseFloat(aliment.glucidesParKgAliment || 0),
        lipidesParKg: parseFloat(aliment.lipidesParKgAliment || 5),
        vitaminesParKg: parseFloat(aliment.vitaminesParKgAliment || 0.5)
      })
      
      let icon = '⚠️'
      if (analysis.score >= 90) icon = '🏆'
      else if (analysis.score >= 75) icon = '👍'
      else if (analysis.score >= 50) icon = '👌'
      else icon = '⚠️'
      
      return {
        score: analysis.score,
        text: analysis.qualite,
        class: analysis.qualite.toLowerCase(),
        icon
      }
    }

    const analyserPlat = (plat) => {
      if (!plat) return null
      
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: parseFloat(plat.proteinesParKgPlat || 0),
        glucidesParKg: parseFloat(plat.glucidesParKgPlat || 0),
        lipidesParKg: parseFloat(plat.lipidesParKgPlat || 0),
        vitaminesParKg: parseFloat(plat.vitaminesParKgPlat || 0)
      })
      
      let icon = '⚠️'
      if (analysis.score >= 90) icon = '🏆'
      else if (analysis.score >= 75) icon = '👍'
      else if (analysis.score >= 50) icon = '👌'
      else icon = '⚠️'
      
      return {
        score: analysis.score,
        text: analysis.qualite,
        class: analysis.qualite.toLowerCase(),
        icon
      }
    }

    // ============== CHARGEMENT DES DONNÉES ==============
    
    const loadData = async () => {
      try {
        // Charger les aliments
        const alimentsData = await nourrissageService.getAliments()
        aliments.value = alimentsData
        
        // Analyser chaque aliment
        aliments.value.forEach(aliment => {
          alimentQuality.value[aliment.idAliment] = analyserAliment(aliment)
        })
        
        if (alimentsData.length > 0 && modeNourrissage.value === 'aliment') {
          selectedAlimentId.value = alimentsData[0].idAliment
        }
        
        // Charger les plats disponibles
        const platsData = await platService.getPlatsDisponibles()
        platsDisponibles.value = platsData
        
        // Analyser chaque plat
        platsDisponibles.value.forEach(plat => {
          platQuality.value[plat.idPlat] = analyserPlat(plat)
        })
        
        if (platsData.length > 0 && modeNourrissage.value === 'plat') {
          selectedPlatId.value = platsData[0].idPlat
        }
        
        // Charger les poissons selon le bassin
        let poissonsData
        if (bassinId.value) {
          try {
            poissonsData = await poissonService.getPoissonsByBassin(bassinId.value)
          } catch (error) {
            console.warn('getPoissonsByBassin non disponible:', error)
            poissonsData = await poissonService.getPoissonsAffames()
          }
        } else {
          poissonsData = await poissonService.getPoissonsAffames()
        }
        
        // Filtrer les poissons affamés
        poissonsAffames.value = poissonsData.filter(poisson => {
          const estAffame = !poisson.estRassasiePoisson && 
                           !poisson.estVenduPoisson && 
                           poisson.estEnViePoisson
          
          if (bassinId.value) {
            const poissonBassinId = poisson.piscineActuelle?.idPiscine || 
                                    poisson.idPiscine || 
                                    poisson.bassinId
            return estAffame && poissonBassinId == bassinId.value
          }
          
          return estAffame
        })
        
        // Mettre à jour les statistiques
        stats.value = {
          ...stats.value,
          poissonsAffames: poissonsAffames.value.length,
          bassinId: bassinId.value,
          bassinNom: bassinNom.value || (bassinId.value ? `Bassin ${bassinId.value}` : '')
        }
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données')
      }
    }

    // Initialiser les paramètres du bassin
    onMounted(() => {
      if (route.query.bassinId) {
        bassinId.value = route.query.bassinId
        bassinNom.value = route.query.bassinNom || ''
      }
      loadData()
    })

    // ============== PROPRIÉTÉS COMPUTED ==============
    
    // Aliment ou Plat sélectionné
    const selectedAliment = computed(() => {
      if (showCustomAliment.value) {
        return customAliment.value
      }
      return aliments.value.find(a => a.idAliment === selectedAlimentId.value)
    })
    
    const selectedPlat = computed(() => {
      return platsDisponibles.value.find(p => p.idPlat === selectedPlatId.value)
    })
    
    // Nutriments selon le mode (AVEC LIPIDES ET VITAMINES)
    const currentNutriments = computed(() => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return {
          proteinesParKg: parseFloat(selectedPlat.value.proteinesParKgPlat || 0),
          glucidesParKg: parseFloat(selectedPlat.value.glucidesParKgPlat || 0),
          lipidesParKg: parseFloat(selectedPlat.value.lipidesParKgPlat || 0),
          vitaminesParKg: parseFloat(selectedPlat.value.vitaminesParKgPlat || 0),
          prixParKg: selectedPlat.value.coutTotalPlat && selectedPlat.value.poidsTotalPlat > 0
            ? parseFloat(selectedPlat.value.coutTotalPlat) / parseFloat(selectedPlat.value.poidsTotalPlat)
            : 0
        }
      } else if (modeNourrissage.value === 'aliment' && selectedAliment.value) {
        return {
          proteinesParKg: parseFloat(selectedAliment.value.proteinesParKg || selectedAliment.value.proteinesParKgAliment || 0),
          glucidesParKg: parseFloat(selectedAliment.value.glucidesParKg || selectedAliment.value.glucidesParKgAliment || 0),
          lipidesParKg: parseFloat(selectedAliment.value.lipidesParKg || selectedAliment.value.lipidesParKgAliment || 5),
          vitaminesParKg: parseFloat(selectedAliment.value.vitaminesParKg || selectedAliment.value.vitaminesParKgAliment || 0.5),
          prixParKg: parseFloat(selectedAliment.value.prixParKg || selectedAliment.value.prixKgAliment || 0)
        }
      }
      return { 
        proteinesParKg: 0, 
        glucidesParKg: 0, 
        lipidesParKg: 0, 
        vitaminesParKg: 0, 
        prixParKg: 0 
      }
    })
    
    // Quantité maximale
    const maxQuantite = computed(() => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return parseFloat(selectedPlat.value.poidsTotalPlat || 0)
      } else if (modeNourrissage.value === 'aliment' && selectedAliment.value) {
        if (showCustomAliment.value) return 10
        return Math.min(parseFloat(selectedAliment.value.stockAliment || 0), 10)
      }
      return 10
    })
    
    // Calculs nutritionnels complets (AVEC LIPIDES ET VITAMINES)
    const proteinesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { proteinesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg,
        nutriments.lipidesParKg,
        nutriments.vitaminesParKg
      )
      return proteinesTotal
    })
    
    const glucidesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { glucidesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg,
        nutriments.lipidesParKg,
        nutriments.vitaminesParKg
      )
      return glucidesTotal
    })
    
    const lipidesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { lipidesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg,
        nutriments.lipidesParKg,
        nutriments.vitaminesParKg
      )
      return lipidesTotal
    })
    
    const vitaminesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { vitaminesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg,
        nutriments.lipidesParKg,
        nutriments.vitaminesParKg
      )
      return vitaminesTotal
    })
    
    // Par poisson
    const nbPoissons = computed(() => stats.value.poissonsAffames || 0)
    
    const proteinesParPoisson = computed(() => {
      if (nbPoissons.value === 0) return 0
      return proteinesTotales.value / nbPoissons.value
    })
    
    const glucidesParPoisson = computed(() => {
      if (nbPoissons.value === 0) return 0
      return glucidesTotales.value / nbPoissons.value
    })
    
    const lipidesParPoisson = computed(() => {
      if (nbPoissons.value === 0) return 0
      return lipidesTotales.value / nbPoissons.value
    })
    
    const vitaminesParPoisson = computed(() => {
      if (nbPoissons.value === 0) return 0
      return vitaminesTotales.value / nbPoissons.value
    })
    
    // Satisfaction complète
    const satisfaction = computed(() => {
      return calculService.calculerSatisfactionComplet(
        proteinesParPoisson.value,
        glucidesParPoisson.value,
        lipidesParPoisson.value,
        vitaminesParPoisson.value
      )
    })
    
    // Gain par poisson (complet)
    const gainParPoisson = computed(() => {
      return calculService.calculerGainPoidsComplet(
        proteinesParPoisson.value,
        glucidesParPoisson.value,
        lipidesParPoisson.value,
        vitaminesParPoisson.value
      )
    })
    
    // Gains totaux
    const gainTotalPrevu = computed(() => {
      return gainParPoisson.value * nbPoissons.value
    })
    
    // Coût total
    const coutTotal = computed(() => {
      if (!currentNutriments.value) return 0
      return calculService.calculerCoutNourrissage(
        quantitePlat.value,
        currentNutriments.value.prixParKg
      )
    })
    
    // Coût par gain
    const coutParGain = computed(() => {
      if (gainParPoisson.value <= 0) return 0
      return coutTotal.value / gainTotalPrevu.value
    })
    
    // Efficacité nutritionnelle
    const efficaciteNutritionnelle = computed(() => {
      if (nbPoissons.value === 0) return 0
      const score = (gainParPoisson.value / 20) * 100 // 20g est le gain maximal
      return Math.min(Math.round(score), 100)
    })
    
    // Besoins totaux
    const besoinTotal = computed(() => {
      // 2g protéines + 4g glucides + 1g lipides + 0.5g vitamines = 7.5g de nutriments
      // Pour 5g de plat (valeur de référence)
      return nbPoissons.value * 5
    })
    
    // Scénarios de gain complets
    const scenariosGainComplets = computed(() => {
      return [
        {
          id: 1,
          title: 'Minimum',
          proteines: 2,
          glucides: 0,
          lipides: 0,
          vitamines: 0,
          gain: 5,
          class: 'low'
        },
        {
          id: 2,
          title: 'Protéines seulement',
          proteines: 2,
          glucides: 0,
          lipides: 0,
          vitamines: 0,
          gain: 5,
          class: 'low'
        },
        {
          id: 3,
          title: 'Glucides seulement',
          proteines: 0,
          glucides: 4,
          lipides: 0,
          vitamines: 0,
          gain: 5,
          class: 'low'
        },
        {
          id: 4,
          title: 'Besoins minimums',
          proteines: 2,
          glucides: 4,
          lipides: 1,
          vitamines: 0.5,
          gain: 10,
          class: 'medium'
        },
        {
          id: 5,
          title: 'Bon',
          proteines: 4,
          glucides: 4,
          lipides: 1,
          vitamines: 0.5,
          gain: 15,
          class: 'good'
        },
        {
          id: 6,
          title: 'Optimal',
          proteines: 4,
          glucides: 8,
          lipides: 2,
          vitamines: 1,
          gain: 20,
          class: 'excellent'
        }
      ]
    })
    
    // Gains prévus pour chaque poisson
    const gainsParPoisson = computed(() => {
      const gains = []
      for (let i = 0; i < Math.min(nbPoissons.value, 10); i++) {
        gains.push(gainParPoisson.value)
      }
      return gains
    })
    
    const gainMin = computed(() => {
      return nbPoissons.value * 5
    })
    
    const gainMax = computed(() => {
      return nbPoissons.value * 20
    })
    
    const gainMoyen = computed(() => {
      return gainParPoisson.value
    })
    
    // Analyse de l'aliment personnalisé
    const customAlimentQuality = computed(() => {
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: parseFloat(customAliment.value.proteinesParKg || 0),
        glucidesParKg: parseFloat(customAliment.value.glucidesParKg || 0),
        lipidesParKg: parseFloat(customAliment.value.lipidesParKg || 5),
        vitaminesParKg: parseFloat(customAliment.value.vitaminesParKg || 0.5)
      })
      return {
        score: analysis.score,
        text: analysis.qualite,
        class: analysis.qualite.toLowerCase()
      }
    })
    
    const customAlimentRentability = computed(() => {
      const analysis = calculService.calculerCoutEfficacite({
        proteinesParKg: parseFloat(customAliment.value.proteinesParKg || 0),
        glucidesParKg: parseFloat(customAliment.value.glucidesParKg || 0),
        lipidesParKg: parseFloat(customAliment.value.lipidesParKg || 5),
        vitaminesParKg: parseFloat(customAliment.value.vitaminesParKg || 0.5),
        prixKgAliment: parseFloat(customAliment.value.prixParKg || 0)
      })
      
      let classname = 'neutral'
      if (analysis.rentabilite === 'Très rentable') classname = 'excellent'
      else if (analysis.rentabilite === 'Rentable') classname = 'good'
      else if (analysis.rentabilite === 'Moyenne') classname = 'medium'
      else if (analysis.rentabilite === 'Peu rentable') classname = 'low'
      else if (analysis.rentabilite === 'Non rentable') classname = 'very-low'
      
      return {
        text: analysis.rentabilite,
        class: classname
      }
    })
    
    // Validation
    const canNourrir = computed(() => {
      return quantitePlat.value > 0 &&
             nbPoissons.value > 0 &&
             quantitePlat.value <= maxQuantite.value &&
             ((modeNourrissage.value === 'aliment' && (selectedAliment.value || showCustomAliment.value)) ||
              (modeNourrissage.value === 'plat' && selectedPlat.value))
    })
    
    const validationMessage = computed(() => {
      if (nbPoissons.value === 0) return 'Aucun poisson affamé à nourrir'
      if (quantitePlat.value <= 0) return 'Veuillez saisir une quantité valide'
      
      if (modeNourrissage.value === 'plat') {
        if (!selectedPlat.value) return 'Veuillez sélectionner un plat'
      } else {
        if (!selectedAliment.value && !showCustomAliment.value) return 'Veuillez sélectionner un aliment'
      }
      
      if (quantitePlat.value > maxQuantite.value) {
        return `Quantité maximale: ${formatStock(maxQuantite.value)} kg`
      }
      return `Prêt à nourrir ${nbPoissons.value} poissons`
    })
    
    const validationClass = computed(() => {
      if (!canNourrir.value) return 'error'
      return 'success'
    })

    // ============== MÉTHODES ==============
    
    const selectAliment = (aliment) => {
      selectedAlimentId.value = aliment.idAliment
      showCustomAliment.value = false
      selectedPlatId.value = null
    }
    
    const selectPlat = (plat) => {
      selectedPlatId.value = plat.idPlat
      selectedAlimentId.value = null
      showCustomAliment.value = false
    }
    
    const toggleCustomAliment = () => {
      showCustomAliment.value = !showCustomAliment.value
      if (showCustomAliment.value) {
        selectedAlimentId.value = null
        selectedPlatId.value = null
      }
    }
    
    const incrementQuantity = () => {
      if (quantitePlat.value < maxQuantite.value) {
        quantitePlat.value = Math.round((quantitePlat.value + 0.1) * 10) / 10
        updateCalculs()
      }
    }
    
    const decrementQuantity = () => {
      if (quantitePlat.value > 0.1) {
        quantitePlat.value = Math.round((quantitePlat.value - 0.1) * 10) / 10
        updateCalculs()
      }
    }
    
    const updateCalculs = () => {
      quantitePlat.value = Math.max(0.1, Math.min(parseFloat(quantitePlat.value) || 0.1, maxQuantite.value))
    }
    
    const getActionButtonText = () => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return `Nourrir avec "${selectedPlat.value.nomPlat}"`
      }
      return `Nourrir ${nbPoissons.value} poissons`
    }
    
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((parseFloat(poisson.poidsActuelPoisson) / parseFloat(poisson.poidsMaximalPoisson)) * 100)
    }
    
    const getAllocation = (poisson) => {
      switch (distributionMode.value) {
        case 'proportionnel':
          const totalPoids = poissonsAffames.value.reduce((sum, p) => sum + parseFloat(p.poidsActuelPoisson || 0), 0)
          return totalPoids > 0 ? (parseFloat(poisson.poidsActuelPoisson) / totalPoids) * 100 : 100 / nbPoissons.value
        default:
          return 100 / nbPoissons.value
      }
    }
    
    const getGainPrevu = (poisson) => {
      const allocation = getAllocation(poisson) / 100
      const proteinesAllouees = proteinesTotales.value * allocation
      const glucidesAlloues = glucidesTotales.value * allocation
      const lipidesAlloues = lipidesTotales.value * allocation
      const vitaminesAllouees = vitaminesTotales.value * allocation
      return calculService.calculerGainPoidsComplet(
        proteinesAllouees, 
        glucidesAlloues, 
        lipidesAlloues, 
        vitaminesAllouees
      )
    }
    
    const getSatisfactionClass = (pourcentage) => {
      if (pourcentage >= 100) return 'excellent'
      if (pourcentage >= 80) return 'good'
      if (pourcentage >= 50) return 'medium'
      return 'low'
    }
    
    const getOverallClass = (pourcentage) => {
      if (pourcentage >= 100) return 'excellent'
      if (pourcentage >= 80) return 'good'
      if (pourcentage >= 50) return 'medium'
      return 'low'
    }
    
    const getSatisfactionMessage = (pourcentage) => {
      if (pourcentage >= 100) return 'Tous les besoins sont satisfaits'
      if (pourcentage >= 80) return 'Besoins presque satisfaits'
      if (pourcentage >= 50) return 'Besoins partiellement satisfaits'
      return 'Besoins insuffisants'
    }
    
    const getGainClass = (gain) => {
      if (gain >= 15) return 'high'
      if (gain >= 10) return 'medium'
      if (gain >= 5) return 'low'
      return 'very-low'
    }
    
    const getEfficiencyClass = (efficacite) => {
      if (efficacite >= 80) return 'excellent'
      if (efficacite >= 60) return 'good'
      if (efficacite >= 40) return 'medium'
      return 'low'
    }
    
    // Exécuter le nourrissage (AVEC LIPIDES ET VITAMINES)
    const executerNourrissage = async () => {
      if (!canNourrir.value) return
      
      loading.value = true
      try {
        let result
        
        if (modeNourrissage.value === 'plat' && selectedPlat.value) {
          // Nourrir avec un plat
          console.log('Nourrissage avec plat:', selectedPlat.value.idPlat)
          result = await nourrissageService.nourrirAvecPlat(selectedPlat.value.idPlat)
          
        } else if (modeNourrissage.value === 'aliment') {
          // Nourrir avec un aliment (personnalisé ou existant)
          const alimentData = showCustomAliment.value ? customAliment.value : selectedAliment.value
          
          // Préparer les données avec lipides et vitamines
          const proteines = parseFloat(alimentData.proteinesParKg || alimentData.proteinesParKgAliment || 0)
          const glucides = parseFloat(alimentData.glucidesParKg || alimentData.glucidesParKgAliment || 0)
          const lipides = parseFloat(alimentData.lipidesParKg || alimentData.lipidesParKgAliment || 5)
          const vitamines = parseFloat(alimentData.vitaminesParKg || alimentData.vitaminesParKgAliment || 0.5)
          
          if (bassinId.value) {
            // Nourrir uniquement les poissons du bassin spécifié
            result = await nourrissageService.nourrirPoissonsDansBassin(
              parseFloat(quantitePlat.value),
              proteines,
              glucides,
              bassinId.value
            )
          } else {
            // Nourrir tous les poissons
            result = await nourrissageService.nourrirPoissons(
              parseFloat(quantitePlat.value),
              proteines,
              glucides
            )
          }
        }
        
        // Personnaliser le message
        let message = result.message || `Nourrissage réussi !`
        if (bassinId.value) {
          message = `${message} (Bassin: ${bassinNom.value || bassinId.value})`
        }
        
        if (modeNourrissage.value === 'plat' && selectedPlat.value) {
          message = `Plat "${selectedPlat.value.nomPlat}" utilisé avec succès !`
          if (bassinId.value) {
            message += ` (Bassin: ${bassinNom.value || bassinId.value})`
          }
        }
        
        // Afficher le résultat
        resultatNourrissage.value = {
          message: message,
          poissonsNourris: result.poissonsNourris || nbPoissons.value,
          nourritureUtilisee: result.nourritureUtilisee || parseFloat(quantitePlat.value),
          gainTotal: result.gainTotal || gainTotalPrevu.value,
          coutTotal: result.coutTotal || coutTotal.value,
          bassinId: bassinId.value,
          bassinNom: bassinNom.value,
          nutriments: {
            proteines: proteinesTotales.value,
            glucides: glucidesTotales.value,
            lipides: lipidesTotales.value,
            vitamines: vitaminesTotales.value
          },
          details: poissonsAffames.value.slice(0, 5).map(p => ({
            id: p.idPoisson,
            nom: p.nomPoisson,
            gain: getGainPrevu(p)
          }))
        }
        
        // Recharger les données
        await loadData()
        
      } catch (error) {
        console.error('Erreur nourrissage:', error)
        alert(error.response?.data?.message || 'Erreur lors du nourrissage')
      } finally {
        loading.value = false
      }
    }
    
    const fermerResultat = () => {
      resultatNourrissage.value = null
    }
    
    const voirHistorique = () => {
      router.push('/historique')
    }
    
    // Watchers
    watch(modeNourrissage, (newMode) => {
      if (newMode === 'plat' && platsDisponibles.value.length > 0) {
        selectedPlatId.value = platsDisponibles.value[0].idPlat
        selectedAlimentId.value = null
      } else if (newMode === 'aliment' && aliments.value.length > 0) {
        selectedAlimentId.value = aliments.value[0].idAliment
        selectedPlatId.value = null
      }
      showCustomAliment.value = false
      updateCalculs()
    })
    
    watch(quantitePlat, updateCalculs)
    
    watch([selectedAlimentId, selectedPlatId, showCustomAliment], () => {
      updateCalculs()
    })

    return {
      // Données
      aliments,
      platsDisponibles,
      poissonsAffames,
      stats,
      loading,
      resultatNourrissage,
      bassinId,
      bassinNom,
      alimentQuality,
      platQuality,
      
      // Sélections
      modeNourrissage,
      selectedAlimentId,
      selectedPlatId,
      showCustomAliment,
      showCustomAlimentPreview,
      quantitePlat,
      distributionMode,
      customAliment,
      
      // Computed
      selectedAliment,
      selectedPlat,
      maxQuantite,
      proteinesTotales,
      glucidesTotales,
      lipidesTotales,
      vitaminesTotales,
      proteinesParPoisson,
      glucidesParPoisson,
      lipidesParPoisson,
      vitaminesParPoisson,
      satisfaction,
      gainParPoisson,
      gainTotalPrevu,
      coutTotal,
      coutParGain,
      efficaciteNutritionnelle,
      besoinTotal,
      scenariosGainComplets,
      gainsParPoisson,
      gainMin,
      gainMax,
      gainMoyen,
      customAlimentQuality,
      customAlimentRentability,
      canNourrir,
      validationMessage,
      validationClass,
      
      // Méthodes utilitaires
      formatStock,
      formatNutrient,
      formatPrice,
      formatPoids,
      
      // Méthodes
      selectAliment,
      selectPlat,
      toggleCustomAliment,
      incrementQuantity,
      decrementQuantity,
      updateCalculs,
      getActionButtonText,
      getProgression,
      getAllocation,
      getGainPrevu,
      getSatisfactionClass,
      getOverallClass,
      getSatisfactionMessage,
      getGainClass,
      getEfficiencyClass,
      executerNourrissage,
      fermerResultat,
      voirHistorique,
      loadData 
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/nourissage.css';
</style>


