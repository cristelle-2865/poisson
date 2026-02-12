<template>
  <div class="detail-bassin">
    <!-- Header avec navigation -->
    <div class="header-section">
      <div class="header-left">
        <button @click="goBack" class="btn-back">
          ⬅️ Retour
        </button>
        <div class="page-title">
          <h1>🏊 Détails du Bassin</h1>
          <p v-if="bassin" class="subtitle">{{ bassin.nomPiscine }}</p>
        </div>
      </div>
      <div class="header-right">
        <router-link :to="`/bassins/edit/${$route.params.id}`" class="btn-secondary">
          ✏️ Modifier le bassin
        </router-link>
        <button @click="refreshData" class="btn-secondary" :disabled="loading">
          🔄 Actualiser
        </button>
      </div>
    </div>

    <!-- Chargement -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Chargement des données du bassin...</p>
    </div>

    <!-- Erreur -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">❌</div>
      <h3>Erreur de chargement</h3>
      <p>{{ error }}</p>
      <button @click="loadData" class="btn-primary">
        Réessayer
      </button>
    </div>

    <!-- Contenu principal -->
    <div v-else-if="bassin" class="main-content">
      <!-- Cartes d'informations du bassin -->
      <div class="bassin-info-cards">
        <!-- Carte principale -->
        <div class="main-info-card">
          <div class="card-header">
            <h2>📋 Informations générales</h2>
            <div class="status-badge" :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
              {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
            </div>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">Nom</span>
                <span class="info-value">{{ bassin.nomPiscine }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Capacité maximale</span>
                <span class="info-value">{{ bassin.capaciteMaxPiscine }} poissons</span>
              </div>
              <div class="info-item">
                <span class="info-label">Date de création</span>
                <span class="info-value">{{ formatDate(bassin.dateCreationPiscine) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Dernière modification</span>
                <span class="info-value">{{ formatDate(bassin.dateModificationPiscine) || 'Jamais modifié' }}</span>
              </div>
            </div>

            <!-- Description -->
            <div v-if="bassin.descriptionPiscine" class="description-section">
              <h4>Description</h4>
              <p class="description-text">{{ bassin.descriptionPiscine }}</p>
            </div>
          </div>
        </div>

        <!-- Carte des caractéristiques techniques -->
        <div v-if="hasTechnicalSpecs" class="tech-info-card">
          <div class="card-header">
            <h2>⚙️ Caractéristiques techniques</h2>
          </div>
          <div class="card-body">
            <div class="specs-grid">
              <div v-if="bassin.volumePiscine" class="spec-item">
                <div class="spec-icon">💧</div>
                <div class="spec-content">
                  <span class="spec-label">Volume</span>
                  <span class="spec-value">{{ bassin.volumePiscine }} m³</span>
                </div>
              </div>
              <div v-if="bassin.temperaturePiscine" class="spec-item">
                <div class="spec-icon">🌡️</div>
                <div class="spec-content">
                  <span class="spec-label">Température</span>
                  <span class="spec-value">{{ bassin.temperaturePiscine }}°C</span>
                </div>
              </div>
              <div v-if="bassin.phPiscine" class="spec-item">
                <div class="spec-icon">🧪</div>
                <div class="spec-content">
                  <span class="spec-label">pH</span>
                  <span class="spec-value">{{ bassin.phPiscine }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Carte d'occupation -->
        <div class="occupation-card">
          <div class="card-header">
            <h2>📊 Occupation du bassin</h2>
          </div>
          <div class="card-body">
            <div class="occupation-stats">
              <div class="stat-item">
                <div class="stat-label">Poissons actuels</div>
                <div class="stat-value">{{ nombrePoissonsActuels }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Capacité restante</div>
                <div class="stat-value">{{ capaciteRestante }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Taux d'occupation</div>
                <div class="stat-value">{{ tauxOccupation.toFixed(1) }}%</div>
              </div>
            </div>

            <!-- Barre de progression -->
            <div class="occupation-bar-section">
              <div class="bar-header">
                <span>Niveau d'occupation</span>
                <span class="bar-percent">{{ tauxOccupation.toFixed(1) }}%</span>
              </div>
              <div class="occupation-bar">
                <div 
                  class="occupation-fill" 
                  :style="{ width: tauxOccupation + '%' }"
                  :class="getOccupationClass(tauxOccupation)"
                ></div>
              </div>
              <div class="bar-text">
                {{ nombrePoissonsActuels }} / {{ bassin.capaciteMaxPiscine }} poissons
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Section des poissons -->
      <div class="poissons-section">
        <div class="section-header">
          <h2>🐟 Poissons dans le bassin</h2>
          <div class="section-actions">
             <button 
              v-if="poissonsAChangerBassin.length > 0"
              @click="openTransfertMassifModal" 
              class="btn-transfert-massif"
              :title="`Transférer les ${poissonsAChangerBassin.length} poissons de plus de ${SEUIL_POIDS_CHANGEMENT_BASSIN}kg`"
            >
              🔄 Transférer les poissons ({{ poissonsAChangerBassin.length }})
            </button>
            <button 
              @click="viderBassin" 
              class="btn-warning"
              :disabled="nombrePoissonsActuels === 0"
            >
              🚰 Vider le bassin
            </button>
            <button @click="openAffectationModal" class="btn-primary">
              ➕ Ajouter un poisson
            </button>
          </div>
        </div>

        <!-- Filtres pour les poissons -->
        <div class="filters-section">
          <div class="filter-group">
            <input 
              type="text" 
              v-model="poissonFilter.search" 
              placeholder="Rechercher un poisson..."
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <select v-model="poissonFilter.status" class="filter-select">
              <option value="">Tous les statuts</option>
              <option value="rassasie">Rassasié</option>
              <option value="affame">Affamé</option>
              <option value="pret">Prêt à vendre</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="poissonSort" class="filter-select">
              <option value="nomAsc">Nom A-Z</option>
              <option value="nomDesc">Nom Z-A</option>
              <option value="poidsDesc">Poids ▼</option>
              <option value="poidsAsc">Poids ▲</option>
              <option value="arriveeDesc">Date arrivée ▼</option>
              <option value="arriveeAsc">Date arrivée ▲</option>
            </select>
          </div>
        </div>

        <!-- Liste des poissons -->
        <div v-if="filteredPoissons.length > 0" class="poissons-grid">
          <div v-for="poisson in filteredPoissons" :key="poisson.idPoisson" class="poisson-card">
            <div class="poisson-header">
              <div class="poisson-title">
                <h3>{{ poisson.nomPoisson }}</h3>
                <div class="poisson-race">
                  <span class="race-badge">{{ poisson.racePoisson?.nomRacePoisson || 'Inconnue' }}</span>
                </div>
              </div>
              <div class="poisson-actions">
                 <div v-if="doitChangerDeBassin(poisson)" class="transfert-alert">
                    <span class="alert-badge">⚠️ À déplacer</span>
                    <button @click="openTransfertModal(poisson)" class="btn-transfert" title="Changer de bassin">
                      🔄 Changer de bassin
                    </button>
                  </div>
                <button @click="retirerPoisson(poisson)" class="btn-icon" title="Retirer du bassin">
                  🚫
                </button>
                <button @click="voirDetailsPoisson(poisson)" class="btn-icon" title="Voir détails">
                  👁️
                </button>
                <router-link 
                  :to="`/poissons/edit/${poisson.idPoisson}`" 
                  class="btn-icon"
                  title="Modifier"
                >
                  ✏️
                </router-link>
                <button 
                  v-if="isPretAVendre(poisson) && !poisson.estVenduPoisson" 
                  @click="vendrePoisson(poisson)" 
                  class="btn-icon btn-sell"
                  title="Vendre"
                >
                  💰
                </button>
              </div>
            </div>

            <div class="poisson-body">
              <!-- Informations principales -->
              <div class="info-grid">
                <div class="info-item">
                <span class="info-label">Poids actuel</span>
                <span class="info-value" :class="getPoidsClass(poisson)">
                  {{ poisson.poidsActuelPoisson }} kg
                  <span v-if="doitChangerDeBassin(poisson)" class="poids-warning">
                    (À déplacer - >{{ SEUIL_POIDS_CHANGEMENT_BASSIN }}kg)
                  </span>
                </span>
              </div>
                <div class="info-item">
                  <span class="info-label">Poids max</span>
                  <span class="info-value">{{ poisson.poidsMaximalPoisson }} kg</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Date arrivée</span>
                  <span class="info-value">{{ formatDateShort(poisson.dateArriveePoisson) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Prix vente</span>
                  <span class="info-value">{{ poisson.prixVentePoisson }} MGA</span>
                </div>
              </div>

              <!-- Barre de croissance -->
              <div class="growth-section">
                <div class="growth-header">
                  <span>Croissance</span>
                  <span class="growth-percent">
                    {{ calculateGrowthPercent(poisson) }}%
                  </span>
                </div>
                <div class="growth-bar">
                  <div 
                    class="growth-fill" 
                    :style="{ width: calculateGrowthPercent(poisson) + '%' }"
                    :class="getGrowthClass(poisson)"
                  ></div>
                </div>
                <div class="growth-text">
                  {{ poisson.poidsActuelPoisson }} kg / {{ poisson.poidsMaximalPoisson }} kg
                </div>
              </div>

              <!-- Statuts -->
              <div class="status-grid">
                <div class="status-item" :class="{ 'status-active': poisson.estRassasiePoisson }">
                  <span class="status-icon">{{ poisson.estRassasiePoisson ? '✅' : '❌' }}</span>
                  <span class="status-label">Rassasié</span>
                </div>
                <div class="status-item" :class="{ 'status-active': poisson.estEnViePoisson }">
                  <span class="status-icon">{{ poisson.estEnViePoisson ? '❤️' : '💀' }}</span>
                  <span class="status-label">{{ poisson.estEnViePoisson ? 'En vie' : 'Mort' }}</span>
                </div>
                <div class="status-item" :class="{ 'status-active': poisson.estVenduPoisson }">
                  <span class="status-icon">{{ poisson.estVenduPoisson ? '💰' : '🏷️' }}</span>
                  <span class="status-label">{{ poisson.estVenduPoisson ? 'Vendu' : 'À vendre' }}</span>
                </div>
                <div class="status-item" :class="{ 'status-active': isPretAVendre(poisson) }">
                  <span class="status-icon">{{ isPretAVendre(poisson) ? '🎯' : '📈' }}</span>
                  <span class="status-label">{{ isPretAVendre(poisson) ? 'Prêt' : 'En croissance' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Aucun poisson -->
        <div v-else class="no-poissons">
          <div class="no-data-icon">🐟</div>
          <h3>Aucun poisson dans ce bassin</h3>
          <p v-if="poissonFilter.search || poissonFilter.status">
            Aucun poisson ne correspond aux filtres sélectionnés.
          </p>
          <p v-else>
            Ce bassin est vide. Ajoutez des poissons pour commencer.
          </p>
          <button @click="openAffectationModal" class="btn-primary">
            ➕ Ajouter un poisson
          </button>
        </div>
      </div>

      <!-- Section historique des nourrissages -->
      <div class="historique-section">
        <div class="section-header">
          <h2>📜 Historique des Nourrissages</h2>
          <div class="section-actions">
            <button @click="toggleViewMode" class="btn-toggle-view">
              {{ historiqueViewMode === 'table' ? '📊 Vue graphique' : '📋 Vue tableau' }}
            </button>
            <button @click="loadHistoriqueNourrissage" class="btn-secondary" :disabled="historiqueNourrissageLoading">
              🔄 Actualiser
            </button>
          </div>
        </div>

        <!-- Filtres pour l'historique des nourrissages -->
        <div class="filters-section" v-if="historiqueViewMode === 'table'">
          <div class="filter-group">
            <input 
              type="text" 
              v-model="historiqueFilter.search" 
              placeholder="Rechercher un poisson..."
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <select v-model="historiqueFilter.dateRange" @change="onDateRangeChange" class="filter-select">
              <option value="7">7 derniers jours</option>
              <option value="30">30 derniers jours</option>
              <option value="90">3 derniers mois</option>
              <option value="365">1 an</option>
              <option value="all">Tout l'historique</option>
            </select>
          </div>
          <div class="filter-group">
            <select v-model="historiqueSort" class="filter-select">
              <option value="dateDesc">Date ▼</option>
              <option value="dateAsc">Date ▲</option>
              <option value="poissonAsc">Poisson A-Z</option>
              <option value="poissonDesc">Poisson Z-A</option>
              <option value="gainDesc">Gain ▼</option>
              <option value="gainAsc">Gain ▲</option>
            </select>
          </div>
        </div>

        <!-- Vue tableau de l'historique -->
        <div v-if="historiqueViewMode === 'table'" class="historique-content">
          <div v-if="historiqueNourrissageLoading" class="loading-small">
            <div class="spinner-small"></div>
            <p>Chargement de l'historique des nourrissages...</p>
          </div>

          <div v-else-if="filteredHistoriqueNourrissage.length > 0" class="historique-table-container">
            <table class="historique-table">
              <thead>
                <tr>
                  <th @click="sortHistoriqueBy('dateNourrissageFisakafoanana')">
                    Date 📅
                    <span v-if="historiqueSortColumn === 'dateNourrissageFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th @click="sortHistoriqueBy('nomPoisson')">
                    Poisson 🐟
                    <span v-if="historiqueSortColumn === 'nomPoisson'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th>Race</th>
                  <th @click="sortHistoriqueBy('ancienPoidsFisakafoanana')">
                    Poids avant ⚖️
                    <span v-if="historiqueSortColumn === 'ancienPoidsFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th @click="sortHistoriqueBy('nouveauPoidsFisakafoanana')">
                    Poids après ⚖️
                    <span v-if="historiqueSortColumn === 'nouveauPoidsFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th @click="sortHistoriqueBy('gainPoidsFisakafoanana')">
                    Gain 📈
                    <span v-if="historiqueSortColumn === 'gainPoidsFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th @click="sortHistoriqueBy('quantiteNourritureFisakafoanana')">
                    Nourriture 🍽️
                    <span v-if="historiqueSortColumn === 'quantiteNourritureFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                  <th @click="sortHistoriqueBy('tauxSatisfactionFisakafoanana')">
                    Satisfaction
                    <span v-if="historiqueSortColumn === 'tauxSatisfactionFisakafoanana'">
                      {{ historiqueSortDirection === 'asc' ? '↑' : '↓' }}
                    </span>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in filteredHistoriqueNourrissage" :key="item.idFisakafoanana">
                  <td>
                    <div class="date-cell">
                      <span class="date">{{ formatDateShort(item.dateNourrissageFisakafoanana) }}</span>
                      <span class="time">{{ formatTime(item.heureNourrissageFisakafoanana) }}</span>
                    </div>
                  </td>
                  <td>
                    <div class="poisson-cell">
                      <span class="poisson-name">{{ item.poisson?.nomPoisson || 'N/A' }}</span>
                      <span class="poisson-id">#{{ item.poisson?.idPoisson }}</span>
                    </div>
                  </td>
                  <td>{{ item.poisson?.racePoisson?.nomRacePoisson || 'Non défini' }}</td>
                  <td>{{ formatPoids(item.ancienPoidsFisakafoanana) }} g</td>
                  <td>
                    <div class="weight-cell">
                      {{ formatPoids(item.nouveauPoidsFisakafoanana) }} g
                      <span class="weight-diff positive" v-if="item.gainPoidsFisakafoanana > 0">
                        +{{ formatPoids(item.gainPoidsFisakafoanana) }}g
                      </span>
                    </div>
                  </td>
                  <td>
                    <span class="gain-badge" :class="getGainClass(item.gainPoidsFisakafoanana)">
                      {{ formatPoids(item.gainPoidsFisakafoanana) }} g
                    </span>
                  </td>
                  <td>{{ formatPoids(item.quantiteNourritureFisakafoanana) }} g</td>
                  <td>
                    <div class="satisfaction-cell">
                      <div class="satisfaction-bar">
                        <div 
                          class="satisfaction-fill" 
                          :style="{ width: item.tauxSatisfactionFisakafoanana + '%' }"
                          :class="getSatisfactionClass(item.tauxSatisfactionFisakafoanana)"
                        ></div>
                      </div>
                      <span class="satisfaction-text">{{ formatNumber(item.tauxSatisfactionFisakafoanana) }}%</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            
            <!-- Pagination -->
            <div class="pagination" v-if="filteredHistoriqueNourrissage.length > historiqueItemsPerPage">
              <button @click="prevHistoriquePage" :disabled="historiqueCurrentPage === 1">← Précédent</button>
              <span class="page-info">
                Page {{ historiqueCurrentPage }} sur {{ historiqueTotalPages }}
              </span>
              <button @click="nextHistoriquePage" :disabled="historiqueCurrentPage === historiqueTotalPages">Suivant →</button>
            </div>
          </div>

          <div v-else class="no-historique">
            <p>Aucun historique de nourrissage disponible pour ce bassin.</p>
          </div>
        </div>

        <!-- Vue graphique de l'historique -->
        <div v-else class="graph-view">
          <div class="graph-container">
            <h3>Évolution du gain de poids moyen par jour</h3>
            <canvas id="gainChartBassin" ref="gainChartBassin"></canvas>
          </div>
          
          <div class="graph-container">
            <h3>Distribution des taux de satisfaction</h3>
            <canvas id="satisfactionChartBassin" ref="satisfactionChartBassin"></canvas>
          </div>
          
          <div class="graph-container">
            <h3>Consommation de nourriture par poisson</h3>
            <canvas id="nourritureChartBassin" ref="nourritureChartBassin"></canvas>
          </div>
        </div>
      </div>

      <!-- Section historique des affectations (ancienne) -->
      <div class="affectations-section">
        <div class="section-header">
          <h2>📜 Historique des Affectations</h2>
          <button @click="toggleHistoriqueAffectations" class="btn-link">
            {{ showHistoriqueAffectations ? 'Masquer' : 'Afficher' }}
          </button>
        </div>

        <div v-if="showHistoriqueAffectations" class="historique-content">
          <div v-if="historiqueLoading" class="loading-small">
            <div class="spinner-small"></div>
            <p>Chargement de l'historique...</p>
          </div>

          <div v-else-if="historique.length > 0" class="historique-list">
            <div v-for="entry in historique" :key="entry.idAffectationPiscine" class="historique-item">
              <div class="historique-date">
                {{ formatDateLong(entry.dateEntreeAffectation) }}
                <span v-if="entry.dateSortieAffectation">
                  → {{ formatDateLong(entry.dateSortieAffectation) }}
                </span>
              </div>
              <div class="historique-details">
                <div class="historique-poisson">
                  <strong>{{ entry.poisson?.nomPoisson || 'Poisson inconnu' }}</strong>
                  ({{ entry.poisson?.racePoisson?.nomRacePoisson || 'Race inconnue' }})
                </div>
                <div v-if="entry.raisonSortieAffectation" class="historique-raison">
                  <em>Raison : {{ entry.raisonSortieAffectation }}</em>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="no-historique">
            <p>Aucun historique d'affectation disponible pour ce bassin.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modale d'ajout de poisson -->
    <div v-if="showAffectationModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Ajouter un poisson au bassin</h3>
          <button @click="closeAffectationModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div v-if="poissonsDisponiblesLoading" class="loading-small">
            <div class="spinner-small"></div>
            <p>Chargement des poissons disponibles...</p>
          </div>

          <div v-else-if="poissonsDisponibles.length === 0" class="no-poissons-disponibles">
            <div class="no-data-icon">🐟</div>
            <h4>Aucun poisson disponible</h4>
            <p>Tous les poissons sont déjà affectés à un bassin.</p>
            <router-link to="/poissons/nouveau" class="btn-primary">
              Créer un nouveau poisson
            </router-link>
          </div>

          <div v-else>
            <p>Sélectionnez un poisson à ajouter au bassin :</p>
            
            <div class="poissons-disponibles-list">
              <div 
                v-for="poisson in poissonsDisponibles" 
                :key="poisson.idPoisson"
                class="poisson-disponible-item"
                :class="{ 'selected': selectedPoisson === poisson.idPoisson }"
                @click="selectedPoisson = poisson.idPoisson"
              >
                <div class="poisson-info">
                  <div class="poisson-name">{{ poisson.nomPoisson }}</div>
                  <div class="poisson-details">
                    <span>{{ poisson.racePoisson?.nomRacePoisson || 'Race inconnue' }}</span>
                    <span>{{ poisson.poidsActuelPoisson }} kg</span>
                    <span>{{ poisson.prixVentePoisson }} MGA</span>
                  </div>
                </div>
                <div class="poisson-status">
                  <span v-if="isPretAVendre(poisson)" class="status-badge ready">Prêt à vendre</span>
                  <span v-if="!poisson.estRassasiePoisson" class="status-badge hungry">Affamé</span>
                </div>
              </div>
            </div>

            <div class="capacity-warning" v-if="bassin && capaciteRestante <= 0">
              ⚠️ Ce bassin a atteint sa capacité maximale !
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeAffectationModal" class="btn-cancel">
            Annuler
          </button>
          <button 
            @click="affecterPoisson" 
            class="btn-primary"
            :disabled="!selectedPoisson || (bassin && capaciteRestante <= 0)"
          >
            Ajouter au bassin
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de confirmation de retrait -->
    <div v-if="showRetraitModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Retirer un poisson du bassin</h3>
          <button @click="closeRetraitModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir retirer <strong>{{ poissonToRemove?.nomPoisson }}</strong> du bassin ?</p>
          
          <div class="form-group">
            <label for="raisonRetrait">Raison du retrait</label>
            <select id="raisonRetrait" v-model="retraitReason" class="filter-select">
              <option value="Transfert">Transfert vers autre bassin</option>
              <option value="Vente">Vente du poisson</option>
              <option value="Décès">Décès du poisson</option>
              <option value="Maladie">Maladie</option>
              <option value="Autre">Autre raison</option>
            </select>
            <input 
              v-if="retraitReason === 'Autre'" 
              v-model="customRetraitReason" 
              placeholder="Spécifiez la raison..."
              class="search-input"
              style="margin-top: 10px;"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeRetraitModal" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmRetrait" class="btn-warning">
            Retirer du bassin
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de transfert de poisson vers autre bassin -->
<div v-if="showTransfertModal" class="modal-overlay">
  <div class="modal modal-large">
    <div class="modal-header">
      <h3>🔄 Transférer {{ poissonATransfererNom }} vers un autre bassin</h3>
      <button @click="closeTransfertModal" class="modal-close">×</button>
    </div>
    
    <div class="modal-body">
      <div class="transfert-info">
        <div class="info-panel warning">
          <div class="info-icon">⚠️</div>
          <div class="info-content">
            <h4>Poisson à déplacer</h4>
            <p>
              <strong>{{ poissonATransfererNom }}</strong> 
              ({{ poissonATransfererPoids }} kg) dépasse le seuil de 
              <strong>{{ SEUIL_POIDS_CHANGEMENT_BASSIN }} kg</strong>.
            </p>
            <p class="info-reason">
              Pour optimiser la croissance, les poissons de plus de {{ SEUIL_POIDS_CHANGEMENT_BASSIN }}kg 
              doivent être transférés vers un bassin moins chargé.
            </p>
          </div>
        </div>
      </div>
      
      <div v-if="bassinsDisponiblesLoading" class="loading-small">
        <div class="spinner-small"></div>
        <p>Chargement des bassins disponibles...</p>
      </div>
      
      <div v-else-if="bassinsDisponibles.length === 0" class="no-bassins-disponibles">
        <div class="no-data-icon">🏊</div>
        <h4>Aucun bassin disponible</h4>
        <p>Tous les bassins actifs sont pleins ou inaccessibles.</p>
        <p class="small">Veuillez libérer de l'espace dans un bassin existant.</p>
      </div>
      
      <div v-else>
        <div class="form-group">
          <label for="bassinTransfert">Sélectionnez le bassin de destination</label>
          <div class="bassins-transfert-list">
            <div 
              v-for="bassin in bassinsDisponibles" 
              :key="bassin.idPiscine"
              class="bassin-transfert-item"
              :class="{ 'selected': selectedBassinTransfert === bassin.idPiscine }"
              @click="selectedBassinTransfert = bassin.idPiscine"
            >
              <div class="bassin-info">
                <div class="bassin-name">{{ bassin.nomPiscine }}</div>
                <div class="bassin-details">
                  <span class="bassin-capacity">
                    Capacité: {{ bassin.capaciteMaxPiscine }} poissons
                  </span>
                  <span class="bassin-occupation">
                    Occupation: {{ getTauxOccupation(bassin) }}%
                  </span>
                  <span class="bassin-places">
                    Places libres: {{ getCapaciteRestante(bassin) }}
                  </span>
                </div>
              </div>
              <div class="bassin-status">
                <span class="status-badge" :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
                  {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label for="raisonTransfert">Raison du transfert</label>
          <select id="raisonTransfert" v-model="raisonTransfert" class="filter-select">
            <option value="Transfert vers autre bassin">Transfert vers autre bassin</option>
            <option value="Densité excessive">Densité excessive</option>
            <option value="Optimisation croissance">Optimisation croissance</option>
            <option value="Autre">Autre raison</option>
          </select>
          <input 
            v-if="raisonTransfert === 'Autre'" 
            v-model="customRaisonTransfert" 
            placeholder="Spécifiez la raison..."
            class="search-input"
            style="margin-top: 10px;"
          />
        </div>
      </div>
    </div>
    
    <div class="modal-footer">
      <button @click="closeTransfertModal" class="btn-cancel">
        Annuler
      </button>
      <button 
        @click="confirmTransfert" 
        class="btn-primary btn-transfert-confirm"
        :disabled="!selectedBassinTransfert || bassinsDisponibles.length === 0"
      >
        🔄 Confirmer le transfert
      </button>
    </div>
  </div>
</div>

 <div v-if="showTransfertMassifModal" class="modal-overlay">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>🔄 Transférer {{ poissonsAChangerBassin.length }} poisson(s) de plus de {{ SEUIL_POIDS_CHANGEMENT_BASSIN }}kg</h3>
          <button @click="closeTransfertMassifModal" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="transfert-info">
            <div class="info-panel warning">
              <div class="info-icon">⚠️</div>
              <div class="info-content">
                <h4>Transfert en masse</h4>
                <p>
                  Vous allez transférer <strong>{{ poissonsAChangerBassin.length }} poisson(s)</strong> 
                  dépassant le seuil de <strong>{{ SEUIL_POIDS_CHANGEMENT_BASSIN }} kg</strong>.
                </p>
                
                <!-- Liste des poissons à transférer -->
                <div class="poissons-liste" v-if="poissonsAChangerBassin.length > 0">
                  <p><strong>Poissons concernés :</strong></p>
                  <ul>
                    <li v-for="poisson in poissonsAChangerBassin" :key="poisson.idPoisson">
                      {{ poisson.nomPoisson }} - {{ poisson.poidsActuelPoisson }}kg
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="bassinsDisponiblesPourTransfertMassif.length === 0" class="no-bassins-disponibles">
            <div class="no-data-icon">🏊</div>
            <h4>Aucun bassin disponible</h4>
            <p>Aucun bassin actif n'a suffisamment de places libres pour recevoir ces poissons.</p>
            <p class="small">Veuillez libérer de l'espace dans un bassin existant.</p>
          </div>
          
          <div v-else>
            <div class="form-group">
              <label for="bassinTransfertMassif">Sélectionnez le bassin de destination</label>
              <div class="bassins-transfert-list">
                <div 
                  v-for="bassin in bassinsDisponiblesPourTransfertMassif" 
                  :key="bassin.idPiscine"
                  class="bassin-transfert-item"
                  :class="{ 'selected': selectedBassinTransfertMassif === bassin.idPiscine }"
                  @click="selectedBassinTransfertMassif = bassin.idPiscine"
                >
                  <div class="bassin-info">
                    <div class="bassin-name">{{ bassin.nomPiscine }}</div>
                    <div class="bassin-details">
                      <span class="bassin-capacity">
                        Capacité: {{ bassin.capaciteMaxPiscine }} poissons
                      </span>
                      <span class="bassin-occupation">
                        Occupation: {{ getTauxOccupation(bassin) }}%
                      </span>
                      <span class="bassin-places">
                        Places libres: {{ getCapaciteRestante(bassin) }}
                      </span>
                      <span v-if="getCapaciteRestante(bassin) < poissonsAChangerBassin.length" class="warning-text">
                        ⚠️ Places insuffisantes (besoin de {{ poissonsAChangerBassin.length }})
                      </span>
                    </div>
                  </div>
                  <div class="bassin-status">
                    <span class="status-badge" :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
                      {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="form-group">
              <label for="raisonTransfertMassif">Raison du transfert</label>
              <select id="raisonTransfertMassif" v-model="raisonTransfertMassif" class="filter-select">
                <option value="Transfert automatique (poids > 700g)">Transfert automatique (poids > 700g)</option>
                <option value="Densité excessive">Densité excessive</option>
                <option value="Optimisation croissance">Optimisation croissance</option>
                <option value="Autre">Autre raison</option>
              </select>
              <input 
                v-if="raisonTransfertMassif === 'Autre'" 
                v-model="customRaisonTransfertMassif" 
                placeholder="Spécifiez la raison..."
                class="search-input"
                style="margin-top: 10px;"
              />
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeTransfertMassifModal" class="btn-cancel">
            Annuler
          </button>
          <button 
            @click="confirmTransfertMassif" 
            class="btn-primary btn-transfert-confirm"
            :disabled="!selectedBassinTransfertMassif || bassinsDisponiblesPourTransfertMassif.length === 0"
          >
            🔄 Transférer {{ poissonsAChangerBassin.length }} poisson(s)
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import bassinService from '../services/bassinService'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'

Chart.register(...registerables)

export default {
  name: 'DetailBassin',
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // États principaux
    const bassin = ref(null)
    const poissons = ref([])
    const historique = ref([])
    const historiqueNourrissage = ref([])
    const loading = ref(false)
    const historiqueLoading = ref(false)
    const historiqueNourrissageLoading = ref(false)
    const error = ref(null)
    const showHistoriqueAffectations = ref(false)
    const historiqueViewMode = ref('table') // 'table' ou 'graph'

    // Seuil de poids pour changement de bassin
    const SEUIL_POIDS_CHANGEMENT_BASSIN = 700

     // États pour le transfert massif
    const showTransfertMassifModal = ref(false)
    const bassinsDisponiblesPourTransfertMassif = ref([])
    const selectedBassinTransfertMassif = ref(null)
    const raisonTransfertMassif = ref('Transfert automatique (poids > 700g)')
    const customRaisonTransfertMassif = ref('')
    const transfertMassifLoading = ref(false)


   // États pour le transfert individuel
    const showTransfertModal = ref(false)
    const poissonATransferer = ref(null)
    const bassinsDisponibles = ref([])
    const bassinsDisponiblesLoading = ref(false)
    const selectedBassinTransfert = ref(null)
    const raisonTransfert = ref('Transfert vers autre bassin')
    const customRaisonTransfert = ref('')

    // Filtres et tri pour les poissons
    const poissonFilter = ref({
      search: '',
      status: ''
    })
    const poissonSort = ref('nomAsc')
    
    // Filtres et tri pour l'historique des nourrissages
    const historiqueFilter = ref({
      search: '',
      dateRange: '30'
    })
    const historiqueSort = ref('dateDesc')
    const historiqueSortColumn = ref('dateNourrissageFisakafoanana')
    const historiqueSortDirection = ref('desc')
    const historiqueCurrentPage = ref(1)
    const historiqueItemsPerPage = ref(10)
    
    // États pour les modales
    const showAffectationModal = ref(false)
    const showRetraitModal = ref(false)
    const poissonsDisponibles = ref([])
    const poissonsDisponiblesLoading = ref(false)
    const selectedPoisson = ref(null)
    const poissonToRemove = ref(null)
    const retraitReason = ref('Transfert')
    const customRetraitReason = ref('')
    
    // Références pour les graphiques
    const gainChartBassin = ref(null)
    const satisfactionChartBassin = ref(null)
    const nourritureChartBassin = ref(null)

    const poissonATransfererId = ref(null)
    const poissonATransfererNom = ref('')
    const poissonATransfererPoids = ref(0)
    
    // Instances des graphiques
    let gainChartInstance = null
    let satisfactionChartInstance = null
    let nourritureChartInstance = null

    
// Computed pour détecter les poissons à changer de bassin
  const poissonsAChangerBassin = computed(() => {
      return poissons.value.filter(poisson => {
        if (poisson.estVenduPoisson || !poisson.estEnViePoisson) return false
        const poidsActuel = parseFloat(poisson.poidsActuelPoisson)
        return poidsActuel >= SEUIL_POIDS_CHANGEMENT_BASSIN
      })
    })

    // Calcul des statistiques
    const nombrePoissonsActuels = computed(() => {
      if (bassin.value?.nombrePoissonsActuels !== undefined) {
        return bassin.value.nombrePoissonsActuels
      }
      if (bassin.value?.poissons?.length !== undefined) {
        return bassin.value.poissons.length
      }
      return poissons.value.length
    })
    
    const capaciteRestante = computed(() => {
      if (!bassin.value?.capaciteMaxPiscine) return 0
      return Math.max(0, bassin.value.capaciteMaxPiscine - nombrePoissonsActuels.value)
    })
    
    const tauxOccupation = computed(() => {
      if (!bassin.value?.capaciteMaxPiscine || bassin.value.capaciteMaxPiscine === 0) return 0
      return (nombrePoissonsActuels.value / bassin.value.capaciteMaxPiscine) * 100
    })
    
    // Vérifier si le bassin a des caractéristiques techniques
    const hasTechnicalSpecs = computed(() => {
      return bassin.value && (
          bassin.value.volumePiscine || 
          bassin.value.temperaturePiscine || 
          bassin.value.phPiscine
      )
    })
    

    // Méthode pour ouvrir la modale de transfert
// MODIFIEZ LA FONCTION openTransfertModal
const openTransfertModal = (poisson) => {
    // Sauvegarder les données du poisson individuellement
    poissonATransfererId.value = poisson.idPoisson
    poissonATransfererNom.value = poisson.nomPoisson
    poissonATransfererPoids.value = poisson.poidsActuelPoisson
    
    selectedBassinTransfert.value = null
    raisonTransfert.value = 'Transfert vers autre bassin'
    customRaisonTransfert.value = ''
    showTransfertModal.value = true
    
    // Charger les bassins disponibles avec l'ID du poisson
    loadBassinsDisponiblesPourTransfert(poisson.idPoisson)
}

// Méthode pour charger les bassins disponibles pour transfert
const loadBassinsDisponiblesPourTransfert = async (idPoisson) => {
    bassinsDisponiblesLoading.value = true
    try {
        bassinsDisponibles.value = await bassinService.getBassinsDisponiblesPourTransfert(idPoisson)
    } catch (error) {
        console.error('Erreur chargement bassins disponibles:', error)
        bassinsDisponibles.value = []
    } finally {
        bassinsDisponiblesLoading.value = false
    }
}

// Méthode pour confirmer le transfert
const confirmTransfert = async () => {
    if (!poissonATransfererId.value) {
        console.error('❌ Aucun poisson sélectionné pour le transfert')
        alert('Erreur : poisson non identifié')
        closeTransfertModal()
        return
    }
    
    if (!selectedBassinTransfert.value) {
        alert('Veuillez sélectionner un bassin de destination')
        return
    }
    
    try {
        const raison = raisonTransfert.value === 'Autre' 
            ? customRaisonTransfert.value 
            : raisonTransfert.value
        
        await bassinService.affecterPoissonANouveauBassin(
            poissonATransfererId.value,
            selectedBassinTransfert.value,
            raison
        )
        
        await loadData()
        closeTransfertModal()
        alert(`✅ Poisson ${poissonATransfererNom.value} transféré avec succès !`)
        
    } catch (err) {
        console.error('❌ Erreur transfert poisson:', err)
        alert(err.response?.data?.message || 'Erreur lors du transfert du poisson')
    }
}

// Méthode pour fermer la modale de transfert
const closeTransfertModal = () => {
    showTransfertModal.value = false
    poissonATransfererId.value = null
    poissonATransfererNom.value = ''
    poissonATransfererPoids.value = 0
    selectedBassinTransfert.value = null
}

// Méthode pour vérifier si un poisson doit changer de bassin
const doitChangerDeBassin = (poisson) => {
    if (poisson.estVenduPoisson || !poisson.estEnViePoisson) {
        return false
    }
    const poidsActuel = parseFloat(poisson.poidsActuelPoisson)
    const doitChanger = poidsActuel >= SEUIL_POIDS_CHANGEMENT_BASSIN
    
    // Log pour déboguer
    console.log(`🐟 ${poisson.nomPoisson}: poids=${poidsActuel}kg, seuil=${SEUIL_POIDS_CHANGEMENT_BASSIN}kg, doitChanger=${doitChanger}`)
    
    return doitChanger
}

// Méthode pour obtenir la classe CSS du poids
const getPoidsClass = (poisson) => {
    if (doitChangerDeBassin(poisson)) {
        return 'poids-critique'
    }
    return ''
}

/**
 * Calculer le taux d'occupation d'un bassin
 */
const getTauxOccupation = (bassin) => {
    if (!bassin || !bassin.capaciteMaxPiscine || bassin.capaciteMaxPiscine === 0) return 0
    
    let nombrePoissons = 0
    if (bassin.nombrePoissonsActuels !== undefined) {
        nombrePoissons = bassin.nombrePoissonsActuels
    } else if (bassin.poissons && Array.isArray(bassin.poissons)) {
        nombrePoissons = bassin.poissons.length
    } else {
        nombrePoissons = 0
    }
    
    const taux = (nombrePoissons / bassin.capaciteMaxPiscine) * 100
    return parseFloat(taux.toFixed(1))
}

/**
 * Calculer la capacité restante d'un bassin
 */
const getCapaciteRestante = (bassin) => {
    if (!bassin || !bassin.capaciteMaxPiscine) return 0
    
    let nombrePoissons = 0
    if (bassin.nombrePoissonsActuels !== undefined) {
        nombrePoissons = bassin.nombrePoissonsActuels
    } else if (bassin.poissons && Array.isArray(bassin.poissons)) {
        nombrePoissons = bassin.poissons.length
    } else {
        nombrePoissons = 0
    }
    
    return Math.max(0, bassin.capaciteMaxPiscine - nombrePoissons)
}

    // Méthode pour vérifier si un poisson est prêt à vendre
    const isPretAVendre = (poisson) => {
      if (poisson.estVenduPoisson || !poisson.estEnViePoisson) {
        return false
      }
      
      if (poisson.estPretAVendre !== undefined) {
        return poisson.estPretAVendre
      }
      
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) {
        return false
      }
      
      const pourcentage = (poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100
      return pourcentage >= 95
    }
    
    // Charger les données du bassin
     const loadData = async () => {
      loading.value = true
      error.value = null
      
      try {
        try {
          const bassinData = await bassinService.getByIdWithStats(route.params.id)
          bassin.value = bassinData
        } catch {
          const bassinData = await bassinService.getById(route.params.id)
          bassin.value = bassinData
        }
        
        const poissonsData = await bassinService.getPoissons(route.params.id)
        poissons.value = Array.isArray(poissonsData) ? poissonsData.map(p => ({
          ...p,
          poidsActuelPoisson: parseFloat(p.poidsActuelPoisson)
        })) : []
        
        await loadHistoriqueNourrissage()
      } catch (err) {
        console.error('Erreur chargement détail bassin:', err)
        error.value = err.response?.data?.message || 'Erreur lors du chargement du bassin'
      } finally {
        loading.value = false
      }
    }
    
    
    // Charger l'historique des nourrissages
    const loadHistoriqueNourrissage = async () => {
      historiqueNourrissageLoading.value = true
      
      try {
        const allData = []
        
        // Récupérer l'historique pour chaque poisson du bassin
        for (const poisson of poissons.value) {
          try {
            const data = await nourrissageService.getHistoriquePoisson(poisson.idPoisson)
            if (Array.isArray(data)) {
              allData.push(...data)
            }
          } catch (error) {
            console.warn(`Pas d'historique pour le poisson ${poisson.idPoisson}`)
          }
        }
        
        historiqueNourrissage.value = allData
        console.log(`Historique nourrissage chargé: ${historiqueNourrissage.value.length} entrées`)
        
        // Initialiser les graphiques si on est en mode graphique
        if (historiqueViewMode.value === 'graph') {
          await nextTick()
          initCharts()
        }
        
      } catch (error) {
        console.error('Erreur chargement historique nourrissage:', error)
        historiqueNourrissage.value = []
      } finally {
        historiqueNourrissageLoading.value = false
      }
    }
    
    // Charger l'historique des affectations
    const loadHistorique = async () => {
      if (!showHistoriqueAffectations.value) return
      
      historiqueLoading.value = true
      try {
        const historiqueData = await bassinService.getHistorique(route.params.id)
        historique.value = Array.isArray(historiqueData) ? historiqueData : []
      } catch (err) {
        console.error('Erreur chargement historique:', err)
      } finally {
        historiqueLoading.value = false
      }
    }
    
    // Charger les poissons disponibles pour affectation
    const loadPoissonsDisponibles = async () => {
      if (!showAffectationModal.value) return
      
      poissonsDisponiblesLoading.value = true
      try {
        // Récupérer tous les poissons en vie
        const allPoissons = await poissonService.getPoissonsEnVie()
        
        // Filtrer ceux sans bassin ou dans un autre bassin
        poissonsDisponibles.value = allPoissons.filter(poisson => {
          if (!poisson.piscineActuelle) return true
          return poisson.piscineActuelle.idPiscine !== parseInt(route.params.id)
        })
      } catch (err) {
        console.error('Erreur chargement poissons disponibles:', err)
        poissonsDisponibles.value = []
      } finally {
        poissonsDisponiblesLoading.value = false
      }
    }
    
    // Gestion du changement de période pour l'historique
    const onDateRangeChange = () => {
      historiqueCurrentPage.value = 1
    }
    
    // Filtrer et trier l'historique des nourrissages
    const filteredHistoriqueNourrissage = computed(() => {
      let filtered = [...historiqueNourrissage.value]
      
      // Filtre de recherche
      if (historiqueFilter.value.search) {
        const query = historiqueFilter.value.search.toLowerCase()
        filtered = filtered.filter(item => {
          const poissonName = item.poisson?.nomPoisson?.toLowerCase() || ''
          const raceName = item.poisson?.racePoisson?.nomRacePoisson?.toLowerCase() || ''
          return poissonName.includes(query) || raceName.includes(query)
        })
      }
      
      // Filtre par date
      if (historiqueFilter.value.dateRange !== 'all') {
        const days = parseInt(historiqueFilter.value.dateRange)
        const cutoffDate = new Date()
        cutoffDate.setDate(cutoffDate.getDate() - days)
        
        filtered = filtered.filter(item => {
          const itemDate = new Date(item.dateNourrissageFisakafoanana)
          return itemDate >= cutoffDate
        })
      }
      
      // Trier les données
      filtered.sort((a, b) => {
        let aValue, bValue
        
        switch (historiqueSortColumn.value) {
          case 'dateNourrissageFisakafoanana':
            aValue = new Date(a.dateNourrissageFisakafoanana)
            bValue = new Date(b.dateNourrissageFisakafoanana)
            break
          case 'nomPoisson':
            aValue = a.poisson?.nomPoisson || ''
            bValue = b.poisson?.nomPoisson || ''
            break
          default:
            aValue = a[historiqueSortColumn.value] || 0
            bValue = b[historiqueSortColumn.value] || 0
        }
        
        if (typeof aValue === 'string') {
          return historiqueSortDirection.value === 'asc' 
            ? aValue.localeCompare(bValue)
            : bValue.localeCompare(aValue)
        } else {
          return historiqueSortDirection.value === 'asc'
            ? aValue - bValue
            : bValue - aValue
        }
      })
      
      // Pagination
      const startIndex = (historiqueCurrentPage.value - 1) * historiqueItemsPerPage.value
      return filtered.slice(startIndex, startIndex + historiqueItemsPerPage.value)
    })
    
    const historiqueTotalPages = computed(() => {
      return Math.ceil(historiqueNourrissage.value.length / historiqueItemsPerPage.value)
    })
    
    // Pagination pour l'historique
    const nextHistoriquePage = () => {
      if (historiqueCurrentPage.value < historiqueTotalPages.value) {
        historiqueCurrentPage.value++
      }
    }
    
    const prevHistoriquePage = () => {
      if (historiqueCurrentPage.value > 1) {
        historiqueCurrentPage.value--
      }
    }
    
    // Trier l'historique par colonne
    const sortHistoriqueBy = (column) => {
      if (historiqueSortColumn.value === column) {
        historiqueSortDirection.value = historiqueSortDirection.value === 'asc' ? 'desc' : 'asc'
      } else {
        historiqueSortColumn.value = column
        historiqueSortDirection.value = 'asc'
      }
    }
    
    // Poissons filtrés
    const filteredPoissons = computed(() => {
      return poissons.value
        .filter(poisson => {
          // Filtre recherche
          if (poissonFilter.value.search) {
            const searchLower = poissonFilter.value.search.toLowerCase()
            if (!poisson.nomPoisson?.toLowerCase().includes(searchLower) &&
                !poisson.racePoisson?.nomRacePoisson?.toLowerCase().includes(searchLower)) {
              return false
            }
          }
          
          // Filtre statut
          if (poissonFilter.value.status) {
            switch (poissonFilter.value.status) {
              case 'rassasie':
                if (!poisson.estRassasiePoisson) return false
                break
              case 'affame':
                if (poisson.estRassasiePoisson) return false
                break
              case 'pret':
                if (!isPretAVendre(poisson)) return false
                break
            }
          }
          
          return true
        })
        .sort((a, b) => {
          switch (poissonSort.value) {
            case 'nomDesc':
              return b.nomPoisson?.localeCompare(a.nomPoisson || '') || 0
            case 'poidsDesc':
              return (b.poidsActuelPoisson || 0) - (a.poidsActuelPoisson || 0)
            case 'poidsAsc':
              return (a.poidsActuelPoisson || 0) - (b.poidsActuelPoisson || 0)
            case 'arriveeDesc':
              return new Date(b.dateArriveePoisson) - new Date(a.dateArriveePoisson)
            case 'arriveeAsc':
              return new Date(a.dateArriveePoisson) - new Date(b.dateArriveePoisson)
            default: // 'nomAsc'
              return a.nomPoisson?.localeCompare(b.nomPoisson || '') || 0
          }
        })
    })

     /**
     * Ouvrir la modale de transfert massif
     */
    const openTransfertMassifModal = async () => {
      if (poissonsAChangerBassin.value.length === 0) {
        alert('Aucun poisson à transférer dans ce bassin.')
        return
      }
      
      showTransfertMassifModal.value = true
      selectedBassinTransfertMassif.value = null
      raisonTransfertMassif.value = 'Transfert automatique (poids > 700g)'
      customRaisonTransfertMassif.value = ''
      
      await loadBassinsDisponiblesPourTransfertMassif()
    }
    
    /**
     * Charger les bassins disponibles pour le transfert massif
     */
    const loadBassinsDisponiblesPourTransfertMassif = async () => {
      transfertMassifLoading.value = true
      try {
        const tousLesBassins = await bassinService.getBassinsDisponibles()
        
        // Filtrer pour garder :
        // 1. Bassins actifs
        // 2. Exclure le bassin actuel
        // 3. Capacité restante >= nombre de poissons à transférer
        const besoin = poissonsAChangerBassin.value.length
        
        bassinsDisponiblesPourTransfertMassif.value = tousLesBassins.filter(b => {
          if (!b.estActivePiscine) return false
          if (b.idPiscine === parseInt(route.params.id)) return false
          
          const nbPoissons = b.nombrePoissonsActuels || 
                           (b.poissons?.length) || 
                           0
          const placesLibres = b.capaciteMaxPiscine - nbPoissons
          
          return placesLibres >= besoin
        })
      } catch (error) {
        console.error('❌ Erreur chargement bassins disponibles:', error)
        bassinsDisponiblesPourTransfertMassif.value = []
        alert('Erreur lors du chargement des bassins disponibles.')
      } finally {
        transfertMassifLoading.value = false
      }
    }
    
    /**
     * Confirmer le transfert massif
     */
    const confirmTransfertMassif = async () => {
      if (!selectedBassinTransfertMassif.value) {
        alert('Veuillez sélectionner un bassin de destination.')
        return
      }
      
      if (poissonsAChangerBassin.value.length === 0) {
        alert('Aucun poisson à transférer.')
        closeTransfertMassifModal()
        return
      }
      
      const raison = raisonTransfertMassif.value === 'Autre' 
        ? customRaisonTransfertMassif.value 
        : raisonTransfertMassif.value
      
      // Confirmation
      if (!confirm(`Transférer ${poissonsAChangerBassin.value.length} poisson(s) vers le bassin sélectionné ?`)) {
        return
      }
      
      transfertMassifLoading.value = true
      
      try {
        let successCount = 0
        let errorCount = 0
        
        // Transférer chaque poisson un par un
        for (const poisson of poissonsAChangerBassin.value) {
          try {
            await bassinService.affecterPoissonANouveauBassin(
              poisson.idPoisson,
              selectedBassinTransfertMassif.value,
              raison
            )
            successCount++
          } catch (err) {
            console.error(`❌ Erreur transfert poisson ${poisson.nomPoisson}:`, err)
            errorCount++
          }
        }
        
        await loadData()
        closeTransfertMassifModal()
        
        // Afficher le résultat
        if (errorCount === 0) {
          alert(`✅ ${successCount} poisson(s) transféré(s) avec succès !`)
        } else {
          alert(`⚠️ ${successCount} poisson(s) transféré(s), ${errorCount} échec(s).`)
        }
      } catch (err) {
        console.error('❌ Erreur transfert massif:', err)
        alert('Erreur lors du transfert des poissons.')
      } finally {
        transfertMassifLoading.value = false
      }
    }
    
    /**
     * Fermer la modale de transfert massif
     */
    const closeTransfertMassifModal = () => {
      showTransfertMassifModal.value = false
      selectedBassinTransfertMassif.value = null
    }
    
    // Méthodes utilitaires pour l'historique
    const formatPoids = (poids) => {
      if (!poids) return '0'
      return Math.round(poids * 100) / 100
    }
    
    const formatNumber = (num) => {
      if (!num) return '0'
      return Math.round(num * 100) / 100
    }
    
    const getGainClass = (gain) => {
      if (gain > 15) return 'gain-high'
      if (gain > 10) return 'gain-medium'
      if (gain > 5) return 'gain-low'
      return 'gain-very-low'
    }
    
    const getSatisfactionClass = (taux) => {
      if (taux >= 100) return 'satisfaction-high'
      if (taux >= 80) return 'satisfaction-medium'
      if (taux >= 60) return 'satisfaction-low'
      return 'satisfaction-very-low'
    }
    
    // Initialiser les graphiques
    const initCharts = () => {
      console.log('Initialisation des graphiques pour le bassin...')
      
      if (historiqueNourrissage.value.length === 0) {
        console.log('Aucune donnée pour les graphiques')
        return
      }
      
      destroyCharts()
      createGainChart()
      createSatisfactionChart()
      createNourritureChart()
    }
    
    const destroyCharts = () => {
      console.log('Destruction des graphiques existants...')
      
      const charts = [gainChartInstance, satisfactionChartInstance, nourritureChartInstance]
      charts.forEach(chart => {
        if (chart && typeof chart.destroy === 'function') {
          try {
            chart.destroy()
          } catch (e) {
            console.warn('Erreur lors de la destruction d\'un graphique:', e)
          }
        }
      })
      
      gainChartInstance = null
      satisfactionChartInstance = null
      nourritureChartInstance = null
    }
    
    const createGainChart = () => {
      const canvas = document.getElementById('gainChartBassin')
      if (!canvas) {
        console.error('Canvas gainChartBassin non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        // Grouper par date
        const gainsByDate = {}
        historiqueNourrissage.value.forEach(item => {
          const date = item.dateNourrissageFisakafoanana
          if (!date) return
          
          const dateKey = formatDateShort(date)
          if (!gainsByDate[dateKey]) {
            gainsByDate[dateKey] = { total: 0, count: 0 }
          }
          gainsByDate[dateKey].total += item.gainPoidsFisakafoanana || 0
          gainsByDate[dateKey].count++
        })
        
        const dates = Object.keys(gainsByDate).sort()
        const avgGains = dates.map(date => {
          const data = gainsByDate[date]
          return data.count > 0 ? data.total / data.count : 0
        })
        
        gainChartInstance = new Chart(ctx, {
          type: 'line',
          data: {
            labels: dates,
            datasets: [{
              label: 'Gain moyen (g)',
              data: avgGains,
              borderColor: '#48bb78',
              backgroundColor: 'rgba(72, 187, 120, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { position: 'top' }
            },
            scales: {
              y: { 
                beginAtZero: true, 
                title: { display: true, text: 'Grammes' }
              },
              x: { 
                title: { display: true, text: 'Date' },
                ticks: {
                  maxRotation: 45,
                  minRotation: 45
                }
              }
            }
          }
        })
        
        console.log('Graphique gain créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique gain:', error)
      }
    }
    
    const createSatisfactionChart = () => {
      const canvas = document.getElementById('satisfactionChartBassin')
      if (!canvas) {
        console.error('Canvas satisfactionChartBassin non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        const categories = {
          'Excellent (100%)': 0,
          'Bon (80-99%)': 0,
          'Moyen (60-79%)': 0,
          'Faible (<60%)': 0
        }
        
        historiqueNourrissage.value.forEach(item => {
          const taux = item.tauxSatisfactionFisakafoanana || 0
          if (taux >= 100) categories['Excellent (100%)']++
          else if (taux >= 80) categories['Bon (80-99%)']++
          else if (taux >= 60) categories['Moyen (60-79%)']++
          else categories['Faible (<60%)']++
        })
        
        satisfactionChartInstance = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: Object.keys(categories),
            datasets: [{
              data: Object.values(categories),
              backgroundColor: [
                '#48bb78', // Vert
                '#4299e1', // Bleu
                '#ed8936', // Orange
                '#f56565'  // Rouge
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { 
                position: 'right',
                labels: {
                  padding: 20
                }
              }
            }
          }
        })
        
        console.log('Graphique satisfaction créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique satisfaction:', error)
      }
    }
    
    const createNourritureChart = () => {
      const canvas = document.getElementById('nourritureChartBassin')
      if (!canvas) {
        console.error('Canvas nourritureChartBassin non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        // Grouper par poisson
        const nourritureByPoisson = {}
        historiqueNourrissage.value.forEach(item => {
          const poissonName = item.poisson?.nomPoisson || 'Inconnu'
          if (!nourritureByPoisson[poissonName]) {
            nourritureByPoisson[poissonName] = 0
          }
          nourritureByPoisson[poissonName] += item.quantiteNourritureFisakafoanana || 0
        })
        
        // Prendre les 10 premiers ou tous si moins de 10
        const sorted = Object.entries(nourritureByPoisson)
          .sort(([, a], [, b]) => b - a)
          .slice(0, 10)
        
        nourritureChartInstance = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: sorted.map(([name]) => name),
            datasets: [{
              label: 'Nourriture consommée (g)',
              data: sorted.map(([, value]) => value),
              backgroundColor: '#667eea',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
              legend: { display: false }
            },
            scales: {
              x: { 
                beginAtZero: true, 
                title: { display: true, text: 'Grammes' }
              }
            }
          }
        })
        
        console.log('Graphique nourriture créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique nourriture:', error)
      }
    }
    
    // Méthodes utilitaires
    const calculateGrowthPercent = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.min(100, (poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
    }
    
    const getGrowthClass = (poisson) => {
      const percent = calculateGrowthPercent(poisson)
      if (percent >= 95) return 'growth-full'
      if (percent >= 70) return 'growth-high'
      if (percent >= 40) return 'growth-medium'
      return 'growth-low'
    }
    
    const getOccupationClass = (taux) => {
      if (taux === 0) return 'occupation-empty'
      if (taux < 33) return 'occupation-low'
      if (taux < 66) return 'occupation-medium'
      if (taux < 100) return 'occupation-high'
      return 'occupation-full'
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Date inconnue'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('fr-FR', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch {
        return 'Date invalide'
      }
    }
    
    const formatDateShort = (dateString) => {
      if (!dateString) return 'Date inconnue'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('fr-FR')
      } catch {
        return 'Date invalide'
      }
    }
    
    const formatTime = (timeString) => {
      if (!timeString) return 'N/A'
      return timeString.substring(0, 5) // Format HH:mm
    }
    
    const formatDateLong = (dateString) => {
      if (!dateString) return 'Date inconnue'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('fr-FR') + ' ' + 
               date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
      } catch {
        return 'Date invalide'
      }
    }
    
    // Navigation
    const goBack = () => {
      router.push('/bassins')
    }
    
    const refreshData = () => {
      loadData()
    }
    
    const toggleHistoriqueAffectations = () => {
      showHistoriqueAffectations.value = !showHistoriqueAffectations.value
      if (showHistoriqueAffectations.value) {
        loadHistorique()
      }
    }
    
    const toggleViewMode = () => {
      historiqueViewMode.value = historiqueViewMode.value === 'table' ? 'graph' : 'table'
      if (historiqueViewMode.value === 'graph') {
        nextTick(() => {
          initCharts()
        })
      }
    }
    
    // Gestion des poissons
    const voirDetailsPoisson = (poisson) => {
      router.push(`/poissons/${poisson.idPoisson}`)
    }
    
    const vendrePoisson = async (poisson) => {
      if (!confirm(`Vendre ${poisson.nomPoisson} pour ${poisson.prixVentePoisson} MGA ?`)) return
      
      try {
        await poissonService.vendrePoisson(poisson.idPoisson)
        await loadData()
      } catch (err) {
        console.error('Erreur vente poisson:', err)
        alert(err.response?.data?.message || 'Erreur lors de la vente du poisson')
      }
    }
    
    const retirerPoisson = (poisson) => {
      poissonToRemove.value = poisson
      retraitReason.value = 'Transfert'
      customRetraitReason.value = ''
      showRetraitModal.value = true
    }
    
    const confirmRetrait = async () => {
      if (!poissonToRemove.value) return
      
      try {
        const raison = retraitReason.value === 'Autre' ? customRetraitReason.value : retraitReason.value
        await bassinService.retirerPoisson(poissonToRemove.value.idPoisson, raison)
        await loadData()
        closeRetraitModal()
      } catch (err) {
        console.error('Erreur retrait poisson:', err)
        alert(err.response?.data?.message || 'Erreur lors du retrait du poisson')
      }
    }
    
    const viderBassin = async () => {
      if (nombrePoissonsActuels.value === 0) {
        alert('Ce bassin est déjà vide !')
        return
      }
      
      if (!confirm(`Vider le bassin ${bassin.value.nomPiscine} (${nombrePoissonsActuels.value} poissons) ?`)) return
      
      try {
        await bassinService.viderBassin(bassin.value.idPiscine, 'Transfert')
        await loadData()
      } catch (err) {
        console.error('Erreur vidage bassin:', err)
        alert(err.response?.data?.message || 'Erreur lors du vidage du bassin')
      }
    }
    
    // Gestion de l'affectation
    const openAffectationModal = () => {
      selectedPoisson.value = null
      showAffectationModal.value = true
    }
    
    const closeAffectationModal = () => {
      showAffectationModal.value = false
      selectedPoisson.value = null
    }
    
    const closeRetraitModal = () => {
      showRetraitModal.value = false
      poissonToRemove.value = null
      customRetraitReason.value = ''
    }
    
    const affecterPoisson = async () => {
      if (!selectedPoisson.value) return
      
      try {
        await bassinService.affecterPoisson(bassin.value.idPiscine, selectedPoisson.value)
        await loadData()
        closeAffectationModal()
      } catch (err) {
        console.error('Erreur affectation poisson:', err)
        alert(err.response?.data?.message || 'Erreur lors de l\'affectation du poisson')
      }
    }
    
    // Watchers
    watch(showAffectationModal, (newVal) => {
      if (newVal) {
        loadPoissonsDisponibles()
      }
    })
    
    watch(historiqueViewMode, (newVal) => {
      if (newVal === 'graph') {
        nextTick(() => {
          initCharts()
        })
      }
    })
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    return {
      // États
      bassin,
      poissons,
      historique,
      historiqueNourrissage,
      loading,
      historiqueLoading,
      historiqueNourrissageLoading,
      error,
      showHistoriqueAffectations,
      historiqueViewMode,
      
      // Filtres et tri
      poissonFilter,
      poissonSort,
      historiqueFilter,
      historiqueSort,
      historiqueSortColumn,
      historiqueSortDirection,
      historiqueCurrentPage,
      historiqueTotalPages,
      
      // Modales
      showAffectationModal,
      showRetraitModal,
      poissonsDisponibles,
      poissonsDisponiblesLoading,
      selectedPoisson,
      poissonToRemove,
      retraitReason,
      customRetraitReason,
      
      // Computed
      nombrePoissonsActuels,
      capaciteRestante,
      tauxOccupation,
      hasTechnicalSpecs,
      filteredPoissons,
      filteredHistoriqueNourrissage,
      
      // Méthodes utilitaires
      isPretAVendre,
      calculateGrowthPercent,
      getGrowthClass,
      getOccupationClass,
      formatDate,
      formatDateShort,
      formatDateLong,
      formatTime,
      formatPoids,
      formatNumber,
      getGainClass,
      getSatisfactionClass,
      
      // Méthodes
      loadData,
      refreshData,
      loadHistoriqueNourrissage,
      toggleHistoriqueAffectations,
      toggleViewMode,
      voirDetailsPoisson,
      vendrePoisson,
      retirerPoisson,
      confirmRetrait,
      viderBassin,
      openAffectationModal,
      closeAffectationModal,
      closeRetraitModal,
      affecterPoisson,
      goBack,
      onDateRangeChange,
      sortHistoriqueBy,
      nextHistoriquePage,
      prevHistoriquePage,

       SEUIL_POIDS_CHANGEMENT_BASSIN,
  poissonsAChangerBassin,
  doitChangerDeBassin,
  getPoidsClass,
  openTransfertModal,
  closeTransfertModal,
  confirmTransfert,
  showTransfertModal,
  // poissonATransferer,
   poissonATransfererId,
    poissonATransfererNom,
    poissonATransfererPoids,
  bassinsDisponibles,
  bassinsDisponiblesLoading,
  selectedBassinTransfert,
  raisonTransfert,
  customRaisonTransfert,

  getTauxOccupation,
    getCapaciteRestante,

    showTransfertMassifModal,
  bassinsDisponiblesPourTransfertMassif,
  selectedBassinTransfertMassif,
  raisonTransfertMassif,
  customRaisonTransfertMassif,
  transfertMassifLoading,
  openTransfertMassifModal,
  closeTransfertMassifModal,
  confirmTransfertMassif,
  
    }

  }
}
</script>

<style scoped>
@import '../assets/styles/bassin-detail';
</style>



