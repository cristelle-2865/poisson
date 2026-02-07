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
                <div class="stat-value">{{ nombrePoissonsActuels|| 0 }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Capacité restante</div>
                <div class="stat-value">{{ capaciteRestante || 0 }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Taux d'occupation</div>
                <div class="stat-value">{{ (tauxOccupation || 0).toFixed(1) }}%</div>
              </div>
            </div>

            <!-- Barre de progression -->
            <div class="occupation-bar-section">
              <div class="bar-header">
                <span>Niveau d'occupation</span>
                <span class="bar-percent">{{ (bassin.tauxOccupation || 0).toFixed(1) }}%</span>
              </div>
              <div class="occupation-bar">
                <div 
                  class="occupation-fill" 
                  :style="{ width: (bassin.tauxOccupation || 0) + '%' }"
                  :class="getOccupationClass(bassin.tauxOccupation || 0)"
                ></div>
              </div>
              <div class="bar-text">
                {{ bassin.nombrePoissonsActuels || 0 }} / {{ bassin.capaciteMaxPiscine }} poissons
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
              @click="viderBassin" 
              class="btn-warning"
              :disabled="!bassin.nombrePoissonsActuels"
            >
              🚰 Vider le bassin
            </button>
            <button @click="showAffectationModal = true" class="btn-primary">
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
                  v-if="poisson.estPretAVendre && !poisson.estVenduPoisson" 
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
                  <span class="info-value">{{ poisson.poidsActuelPoisson }} kg</span>
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
                  <span class="info-value">{{ poisson.prixVentePoisson }} Ar</span>
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
                <div class="status-item" :class="{ 'status-active': poisson.estPretAVendre }">
                  <span class="status-icon">{{ poisson.estPretAVendre ? '🎯' : '📈' }}</span>
                  <span class="status-label">{{ poisson.estPretAVendre ? 'Prêt' : 'En croissance' }}</span>
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
          <button @click="showAffectationModal = true" class="btn-primary">
            ➕ Ajouter un poisson
          </button>
        </div>
      </div>

      <!-- Section historique -->
      <div class="historique-section">
        <div class="section-header">
          <h2>📜 Historique du bassin</h2>
          <button @click="toggleHistorique" class="btn-link">
            {{ showHistorique ? 'Masquer' : 'Afficher' }}
          </button>
        </div>

        <div v-if="showHistorique" class="historique-content">
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
            <p>Aucun historique disponible pour ce bassin.</p>
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
                    <span>{{ poisson.prixVentePoisson }} €</span>
                  </div>
                </div>
                <div class="poisson-status">
                  <span v-if="poisson.estPretAVendre" class="status-badge ready">Prêt à vendre</span>
                  <span v-if="!poisson.estRassasiePoisson" class="status-badge hungry">Affamé</span>
                </div>
              </div>
            </div>

            <div class="capacity-warning" v-if="bassin && bassin.capaciteRestante <= 0">
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
            :disabled="!selectedPoisson || (bassin && bassin.capaciteRestante <= 0)"
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
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import bassinService from '../services/bassinService'
import poissonService from '../services/poissonService'

export default {
  name: 'DetailBassin',
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // États principaux
    const bassin = ref(null)
    const poissons = ref([])
    const historique = ref([])
    const loading = ref(false)
    const historiqueLoading = ref(false)
    const error = ref(null)
    const showHistorique = ref(false)
    
    // Filtres et tri pour les poissons
    const poissonFilter = ref({
      search: '',
      status: ''
    })
    const poissonSort = ref('nomAsc')
    
    // États pour les modales
    const showAffectationModal = ref(false)
    const showRetraitModal = ref(false)
    const poissonsDisponibles = ref([])
    const poissonsDisponiblesLoading = ref(false)
    const selectedPoisson = ref(null)
    const poissonToRemove = ref(null)
    const retraitReason = ref('Transfert')
    const customRetraitReason = ref('')

   
    
    // Charger les données du bassin
  const loadData = async () => {
  loading.value = true
  error.value = null
  
  try {
    // Charger les informations du bassin
    const bassinData = await bassinService.getById(route.params.id)
    bassin.value = bassinData
    
    // Charger les poissons du bassin
    const poissonsData = await bassinService.getPoissons(route.params.id)
    poissons.value = Array.isArray(poissonsData) ? poissonsData : []
    
    // NE PAS modifier l'objet bassin.value directement
    // Les métriques seront calculées via computed properties
    
  } catch (err) {
    console.error('Erreur chargement détail bassin:', err)
    error.value = err.response?.data?.message || 'Erreur lors du chargement du bassin'
  } finally {
    loading.value = false
  }
}

    
    // Charger l'historique
    const loadHistorique = async () => {
      if (!showHistorique.value) return
      
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
        // Récupérer tous les poissons
        const allPoissons = await poissonService.getPoissonsEnVie()
        
        // Filtrer ceux sans bassin
        poissonsDisponibles.value = allPoissons.filter(poisson => 
          !poisson.piscineActuelle || poisson.piscineActuelle.idPiscine !== parseInt(route.params.id)
        )
      } catch (err) {
        console.error('Erreur chargement poissons disponibles:', err)
        poissonsDisponibles.value = []
      } finally {
        poissonsDisponiblesLoading.value = false
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
            // Utilisez la méthode estPretAVendre si elle existe, sinon calculez
            const estPret = poisson.estPretAVendre || 
              (poisson.poidsActuelPoisson && poisson.poidsMaximalPoisson && 
               (poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) >= 0.95)
            if (!estPret) return false
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
    
     const nombrePoissonsActuels = computed(() => poissons.value.length)
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
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const formatDateShort = (dateString) => {
      if (!dateString) return 'Date inconnue'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR')
    }
    
    const formatDateLong = (dateString) => {
      if (!dateString) return 'Date inconnue'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR') + ' ' + 
             date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
    }
    
    // Navigation
    const goBack = () => {
      router.push('/bassins')
    }
    
    const refreshData = () => {
      loadData()
    }
    
    const toggleHistorique = () => {
      showHistorique.value = !showHistorique.value
      if (showHistorique.value) {
        loadHistorique()
      }
    }
    
    // Gestion des poissons
    const voirDetailsPoisson = (poisson) => {
      router.push(`/poissons/${poisson.idPoisson}`)
    }
    
    const vendrePoisson = async (poisson) => {
      if (!confirm(`Vendre ${poisson.nomPoisson} pour ${poisson.prixVentePoisson} € ?`)) return
      
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
  if (!nombrePoissonsActuels.value) {
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
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    return {
      // États
      bassin,
      poissons,
      historique,
      loading,
      historiqueLoading,
      error,
      showHistorique,
      
      // Filtres et tri
      poissonFilter,
      poissonSort,
      
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
      filteredPoissons,
      hasTechnicalSpecs,
      
      // Méthodes
      loadData,
      refreshData,
      toggleHistorique,
      voirDetailsPoisson,
      vendrePoisson,
      retirerPoisson,
      confirmRetrait,
      viderBassin,
      openAffectationModal,
      closeAffectationModal,
      closeRetraitModal,
      affecterPoisson,
      
      // Méthodes utilitaires
      calculateGrowthPercent,
      getGrowthClass,
      getOccupationClass,
      formatDate,
      formatDateShort,
      formatDateLong,
      goBack
    }
  }
}
</script>

<style scoped>
.detail-bassin {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  padding: 10px 20px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-back:hover {
  background: #cbd5e0;
}

.page-title h1 {
  margin: 0;
  color: #2d3748;
  font-size: 28px;
}

.subtitle {
  margin: 5px 0 0 0;
  color: #718096;
  font-size: 18px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 15px;
}

/* Boutons communs */
.btn-primary, .btn-secondary, .btn-warning, .btn-link {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  text-decoration: none;
}

.btn-primary {
  background: #4299e1;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #3182ce;
}

.btn-secondary {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-secondary:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-warning {
  background: #ed8936;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background: #dd6b20;
}

.btn-warning:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-link {
  background: none;
  color: #4299e1;
}

.btn-link:hover {
  text-decoration: underline;
}

/* Chargement et erreur */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.error-container h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

.error-container p {
  color: #718096;
  margin-bottom: 20px;
}

/* Cartes d'informations du bassin */
.bassin-info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.main-info-card, .tech-info-card, .occupation-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px;
  background: #f7fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #c6f6d5;
  color: #22543d;
}

.status-inactive {
  background: #fed7d7;
  color: #742a2a;
}

.card-body {
  padding: 20px;
}

/* Grilles d'informations */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-label {
  font-size: 12px;
  color: #718096;
  font-weight: 600;
}

.info-value {
  font-size: 16px;
  color: #2d3748;
  font-weight: 600;
}

/* Description */
.description-section {
  margin-top: 20px;
}

.description-section h4 {
  margin: 0 0 10px 0;
  color: #4a5568;
  font-size: 16px;
}

.description-text {
  margin: 0;
  color: #718096;
  font-size: 14px;
  line-height: 1.5;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

/* Caractéristiques techniques */
.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

.spec-icon {
  font-size: 24px;
}

.spec-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.spec-label {
  font-size: 12px;
  color: #718096;
}

.spec-value {
  font-size: 16px;
  color: #2d3748;
  font-weight: 600;
}

/* Occupation */
.occupation-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

.stat-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  color: #2d3748;
  font-weight: bold;
}

.occupation-bar-section {
  margin-top: 20px;
}

.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.bar-percent {
  font-weight: bold;
  color: #2d3748;
  font-size: 16px;
}

.occupation-bar {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 5px;
}

.occupation-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s;
}

.occupation-empty { background: #a0aec0; }
.occupation-low { background: #48bb78; }
.occupation-medium { background: #ed8936; }
.occupation-high { background: #f56565; }
.occupation-full { background: #742a2a; }

.bar-text {
  text-align: center;
  font-size: 14px;
  color: #718096;
}

/* Section poissons */
.poissons-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  overflow: hidden;
}

.section-header {
  padding: 20px;
  background: #f7fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-actions {
  display: flex;
  gap: 15px;
}

/* Filtres */
.filters-section {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  gap: 20px;
  background: #fff;
}

.filter-group {
  flex: 1;
}

.search-input, .filter-select {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  width: 100%;
}

.search-input:focus, .filter-select:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

/* Grille des poissons */
.poissons-grid {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.poisson-card {
  background: #f7fafc;
  border-radius: 10px;
  padding: 15px;
  border: 1px solid #e2e8f0;
  transition: transform 0.2s, box-shadow 0.2s;
}

.poisson-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.poisson-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.poisson-title h3 {
  margin: 0 0 5px 0;
  color: #2d3748;
  font-size: 18px;
}

.race-badge {
  padding: 3px 8px;
  background: #bee3f8;
  color: #2c5282;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.poisson-actions {
  display: flex;
  gap: 5px;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 6px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  color: #4a5568;
}

.btn-icon:hover {
  background: #e2e8f0;
}

.btn-sell:hover {
  background: #c6f6d5;
  color: #22543d;
}

/* Barre de croissance */
.growth-section {
  margin: 15px 0;
}

.growth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #718096;
}

.growth-percent {
  font-weight: bold;
  color: #2d3748;
}

.growth-bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.growth-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s;
}

.growth-low { background: #48bb78; }
.growth-medium { background: #ed8936; }
.growth-high { background: #f56565; }
.growth-full { background: #742a2a; }

.growth-text {
  text-align: center;
  font-size: 12px;
  color: #718096;
}

/* Statuts */
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 15px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.status-item.status-active {
  background: #c6f6d5;
  border-color: #9ae6b4;
}

.status-icon {
  font-size: 14px;
}

.status-label {
  font-size: 12px;
  color: #4a5568;
  font-weight: 500;
}

/* Aucun poisson */
.no-poissons, .no-historique {
  padding: 40px 20px;
  text-align: center;
  color: #718096;
}

.no-data-icon {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.no-poissons h3, .no-historique h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

/* Section historique */
.historique-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  overflow: hidden;
}

.historique-content {
  padding: 20px;
}

.historique-list {
  max-height: 400px;
  overflow-y: auto;
}

.historique-item {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s;
}

.historique-item:hover {
  background: #f7fafc;
}

.historique-item:last-child {
  border-bottom: none;
}

.historique-date {
  font-size: 12px;
  color: #718096;
  margin-bottom: 5px;
}

.historique-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.historique-poisson {
  color: #2d3748;
  font-size: 14px;
}

.historique-raison {
  font-size: 12px;
  color: #718096;
  font-style: italic;
}

/* Chargement petit */
.loading-small {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

/* Modales */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  animation: modalSlide 0.3s ease;
}

@keyframes modalSlide {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #718096;
  line-height: 1;
}

.modal-close:hover {
  color: #f56565;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  position: sticky;
  bottom: 0;
  background: white;
}

.btn-cancel {
  padding: 10px 20px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

/* Poissons disponibles */
.poissons-disponibles-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin: 15px 0;
}

.poisson-disponible-item {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: background 0.2s;
}

.poisson-disponible-item:hover {
  background: #f7fafc;
}

.poisson-disponible-item.selected {
  background: #ebf8ff;
  border-left: 4px solid #4299e1;
}

.poisson-disponible-item:last-child {
  border-bottom: none;
}

.poisson-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.poisson-name {
  font-weight: bold;
  color: #2d3748;
}

.poisson-details {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #718096;
}

.poisson-status {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.status-badge {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.ready {
  background: #c6f6d5;
  color: #22543d;
}

.status-badge.hungry {
  background: #fed7d7;
  color: #742a2a;
}

.capacity-warning {
  padding: 10px;
  background: #fffaf0;
  border: 1px solid #ed8936;
  border-radius: 6px;
  color: #744210;
  font-weight: 600;
  text-align: center;
  margin: 15px 0;
}

/* Aucun poisson disponible */
.no-poissons-disponibles {
  text-align: center;
  padding: 20px;
}

.no-poissons-disponibles h4 {
  margin: 10px 0;
  color: #2d3748;
}

.no-poissons-disponibles p {
  color: #718096;
  margin-bottom: 20px;
}

/* Responsive */
@media (max-width: 1024px) {
  .bassin-info-cards {
    grid-template-columns: 1fr;
  }
  
  .poissons-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .section-actions {
    justify-content: flex-start;
  }
  
  .filters-section {
    flex-direction: column;
  }
  
  .occupation-stats {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .specs-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .poissons-grid {
    grid-template-columns: 1fr;
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
  
  .modal {
    width: 95%;
    margin: 10px;
  }
}
</style>