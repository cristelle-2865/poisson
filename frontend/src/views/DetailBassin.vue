<template>
  <div class="detail-bassin">
    <!-- En-tête de page -->
    <div class="page-header">
      <div class="header-left">
        <router-link to="/bassins" class="btn-back">
          ← Retour aux bassins
        </router-link>
        <h1>🏊 Détails du Bassin</h1>
        <p class="subtitle">{{ bassin.nomPiscine }}</p>
      </div>
      <div class="header-right">
        <button @click="toggleBassinStatus" class="btn-status" :class="bassin.estActivePiscine ? 'btn-inactive' : 'btn-active'">
          {{ bassin.estActivePiscine ? '⏸️ Désactiver' : '▶️ Activer' }}
        </button>
        <router-link :to="{ path: `/bassins/edit/${bassin.idPiscine}` }" class="btn-edit">
          ✏️ Modifier
        </router-link>
        <button @click="viderBassin" class="btn-empty" :disabled="!nombrePoissonsActuels">
          🚰 Vider le bassin
        </button>
      </div>
    </div>

    <!-- Alertes -->
    <div v-if="!bassin.estActivePiscine" class="alert warning">
      <span class="alert-icon">⚠️</span>
      <span>Ce bassin est actuellement <strong>inactif</strong>. Les poissons ne peuvent pas y être ajoutés.</span>
    </div>

    <!-- Carte principale du bassin -->
    <div class="bassin-overview">
      <div class="overview-card">
        <div class="overview-header">
          <div class="bassin-title">
            <h2>{{ bassin.nomPiscine }}</h2>
            <div class="bassin-status">
              <span class="status-badge" :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
                {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
              </span>
              <span class="date-created">
                Créé le {{ formatDate(bassin.dateCreationPiscine) }}
              </span>
            </div>
          </div>
          <div class="occupation-display">
            <div class="occupation-info">
              <span class="occupation-label">Occupation</span>
              <span class="occupation-value">{{ tauxOccupation }}%</span>
            </div>
            <div class="occupation-bar-large">
              <div 
                class="occupation-fill-large" 
                :style="{ width: tauxOccupation + '%' }"
                :class="getOccupationClass(tauxOccupation)"
              ></div>
            </div>
            <div class="occupation-details">
              <span>{{ nombrePoissonsActuels }} / {{ bassin.capaciteMaxPiscine }} poissons</span>
              <span>{{ capaciteRestante }} places restantes</span>
            </div>
          </div>
        </div>

        <!-- Statistiques du bassin -->
        <div class="bassin-stats">
          <div class="stat-item">
            <div class="stat-icon">🐟</div>
            <div class="stat-content">
              <h3>Poissons en vie</h3>
              <p class="stat-value">{{ stats.poissonsEnVie }}</p>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <h3>Prêts à vendre</h3>
              <p class="stat-value">{{ stats.poissonsPretAVendre }}</p>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">😋</div>
            <div class="stat-content">
              <h3>Poissons affamés</h3>
              <p class="stat-value">{{ stats.poissonsAffames }}</p>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">⚖️</div>
            <div class="stat-content">
              <h3>Poids total</h3>
              <p class="stat-value">{{ stats.poidsTotal }}g</p>
            </div>
          </div>
        </div>

        <!-- Caractéristiques techniques -->
        <div v-if="hasTechnicalSpecs" class="technical-specs">
          <h3>🔧 Caractéristiques techniques</h3>
          <div class="specs-grid">
            <div v-if="bassin.volumePiscine" class="spec-card">
              <div class="spec-icon">💧</div>
              <div class="spec-content">
                <h4>Volume</h4>
                <p>{{ bassin.volumePiscine }} m³</p>
              </div>
            </div>
            <div v-if="bassin.temperaturePiscine" class="spec-card">
              <div class="spec-icon">🌡️</div>
              <div class="spec-content">
                <h4>Température</h4>
                <p>{{ bassin.temperaturePiscine }}°C</p>
              </div>
            </div>
            <div v-if="bassin.phPiscine" class="spec-card">
              <div class="spec-icon">🧪</div>
              <div class="spec-content">
                <h4>pH</h4>
                <p>{{ bassin.phPiscine }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div v-if="bassin.descriptionPiscine" class="description-section">
          <h3>📝 Description</h3>
          <div class="description-content">
            {{ bassin.descriptionPiscine }}
          </div>
        </div>
      </div>
    </div>

    <!-- Section poissons -->
    <div class="poissons-section">
      <div class="section-header">
        <h2>🐟 Poissons dans ce bassin</h2>
        <div class="section-actions">
          <button @click="affecterPoisson" class="btn-primary" :disabled="!bassin.estActivePiscine || isFull">
            ➕ Affecter un poisson
          </button>
          <button @click="refreshData" class="btn-secondary">
            🔄 Actualiser
          </button>
        </div>
      </div>

      <!-- Message si bassin plein -->
      <div v-if="isFull" class="alert info">
        <span class="alert-icon">ℹ️</span>
        <span>Ce bassin a atteint sa capacité maximale. Pour ajouter de nouveaux poissons, vous devez d'abord en retirer ou vider le bassin.</span>
      </div>

      <!-- Liste des poissons -->
      <div v-if="isLoading" class="loading-container">
        <div class="spinner"></div>
        <p>Chargement des poissons...</p>
      </div>

      <div v-else-if="poissons.length > 0" class="poissons-table-container">
        <div class="table-header">
          <div class="table-info">
            <span class="result-count">
              {{ poissons.length }} poisson{{ poissons.length !== 1 ? 's' : '' }}
            </span>
          </div>
          <div class="table-filters">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Rechercher un poisson..."
              class="search-input"
            />
            <select v-model="statusFilter" class="filter-select">
              <option value="">Tous les statuts</option>
              <option value="en-vie">En vie</option>
              <option value="pret-a-vendre">Prêt à vendre</option>
              <option value="affame">Affamé</option>
            </select>
          </div>
        </div>

        <div class="table-responsive">
          <table class="poissons-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Race</th>
                <th>Poids actuel</th>
                <th>Progression</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="poisson in filteredPoissons" :key="poisson.idPoisson">
                <td class="cell-name">
                  <div class="name-container">
                    <span class="fish-icon">🐟</span>
                    <div class="name-details">
                      <strong>{{ poisson.nomPoisson }}</strong>
                      <small class="text-muted">ID: {{ poisson.idPoisson }}</small>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="race-badge">
                    {{ getRaceName(poisson) }}
                  </span>
                </td>
                <td>
                  <div class="weight-info">
                    <strong>{{ poisson.poidsActuelPoisson || poisson.poidsInitialPoisson }}g</strong>
                    <small class="text-muted">
                      Max: {{ poisson.poidsMaximalPoisson }}g
                    </small>
                  </div>
                </td>
                <td>
                  <div class="progression-cell">
                    <div class="progression-bar-container">
                      <div 
                        class="progression-bar" 
                        :style="{ width: getProgression(poisson) + '%' }"
                        :class="getProgressionClass(getProgression(poisson))"
                      ></div>
                    </div>
                    <span class="progression-text">{{ getProgression(poisson).toFixed(1) }}%</span>
                  </div>
                </td>
                <td>
                  <div class="status-container">
                    <span class="status-badge" :class="getPoissonStatusClass(poisson)">
                      {{ getPoissonStatusText(poisson) }}
                    </span>
                    <div class="status-icons">
                      <span v-if="poisson.estRassasiePoisson" title="Rassasié" class="status-icon">🍽️</span>
                      <span v-else title="Affamé" class="status-icon">😋</span>
                      <span v-if="poisson.estEnViePoisson" title="En vie" class="status-icon">❤️</span>
                      <span v-else title="Mort" class="status-icon">💀</span>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="action-buttons">
                    <router-link 
                      :to="{ name: 'DetailPoisson', params: { id: poisson.idPoisson } }" 
                      class="btn-action btn-view"
                      title="Voir détails"
                    >
                      👁️
                    </router-link>
                    <button 
                      @click="retirerPoisson(poisson)"
                      class="btn-action btn-remove"
                      title="Retirer du bassin"
                    >
                      🚪
                    </button>
                    <button 
                      v-if="getProgression(poisson) >= 95 && poisson.estEnViePoisson && !poisson.estVenduPoisson"
                      @click="vendrePoisson(poisson)"
                      class="btn-action btn-sell"
                      title="Vendre"
                    >
                      💰
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Message si aucun résultat après filtrage -->
        <div v-if="filteredPoissons.length === 0 && poissons.length > 0" class="no-filter-results">
          <p>Aucun poisson ne correspond aux critères de recherche.</p>
        </div>
      </div>

      <!-- Message si bassin vide -->
      <div v-else class="no-poissons">
        <div class="no-poissons-icon">🐟</div>
        <h3>Ce bassin est vide</h3>
        <p>Il n'y a actuellement aucun poisson dans ce bassin.</p>
        <button @click="affecterPoisson" class="btn-primary" :disabled="!bassin.estActivePiscine">
          ➕ Affecter un premier poisson
        </button>
      </div>
    </div>

    <!-- Section historique -->
    <div class="historique-section">
      <div class="section-header">
        <h2>📜 Historique du bassin</h2>
        <button @click="toggleHistorique" class="btn-link">
          {{ showHistorique ? 'Masquer' : 'Afficher' }} l'historique
        </button>
      </div>

      <div v-if="showHistorique && historique.length > 0" class="historique-list">
        <div v-for="entry in historique" :key="entry.idAffectation" class="historique-item">
          <div class="historique-date">{{ formatDate(entry.dateEntreeAffectation) }}</div>
          <div class="historique-content">
            <div class="historique-action">
              <strong>{{ entry.poisson?.nomPoisson || 'Poisson inconnu' }}</strong> a été 
              <span v-if="entry.dateSortieAffectation">retiré</span>
              <span v-else>ajouté</span>
              {{ entry.dateSortieAffectation ? 'le ' + formatDate(entry.dateSortieAffectation) : '' }}
            </div>
            <div v-if="entry.raisonSortieAffectation" class="historique-reason">
              Raison: {{ entry.raisonSortieAffectation }}
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="showHistorique" class="no-historique">
        <p>Aucun historique disponible pour ce bassin.</p>
      </div>
    </div>

    <!-- Modale affectation poisson -->
    <div v-if="showAffectModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Affecter un poisson</h3>
          <button @click="showAffectModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Sélectionnez un poisson à affecter au bassin <strong>{{ bassin.nomPiscine }}</strong></p>
          
          <div v-if="isLoadingPoissons" class="loading-modal">
            <div class="spinner-small"></div>
            <p>Chargement des poissons disponibles...</p>
          </div>

          <div v-else-if="poissonsDisponibles.length > 0" class="poissons-list-modal">
            <div v-for="poisson in poissonsDisponibles" :key="poisson.idPoisson" 
                 class="poisson-item" @click="selectPoisson(poisson)"
                 :class="{ selected: selectedPoisson?.idPoisson === poisson.idPoisson }">
              <div class="poisson-info">
                <span class="fish-icon">🐟</span>
                <div>
                  <strong>{{ poisson.nomPoisson }}</strong>
                  <div class="poisson-details">
                    <span>Race: {{ getRaceName(poisson) }}</span>
                    <span>Poids: {{ poisson.poidsActuelPoisson || poisson.poidsInitialPoisson }}g</span>
                    <span>Progression: {{ getProgression(poisson).toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
              <div class="poisson-status">
                <span class="status-badge-small" :class="getPoissonStatusClass(poisson)">
                  {{ getPoissonStatusText(poisson) }}
                </span>
              </div>
            </div>
          </div>

          <div v-else class="no-poissons-modal">
            <p>Aucun poisson disponible pour l'affectation.</p>
            <p>Tous les poissons en vie sont déjà affectés à un bassin.</p>
            <router-link to="/poissons/nouveau" class="btn-primary">
              ➕ Créer un nouveau poisson
            </router-link>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAffectModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmAffectation" class="btn-primary" :disabled="!selectedPoisson">
            Affecter le poisson
          </button>
        </div>
      </div>
    </div>

    <!-- Modale retrait poisson -->
    <div v-if="showRetraitModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Retirer un poisson</h3>
          <button @click="showRetraitModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir retirer <strong>{{ poissonToRemove?.nomPoisson }}</strong> du bassin ?</p>
          
          <div class="form-group">
            <label for="raisonRetrait">Raison du retrait</label>
            <select id="raisonRetrait" v-model="retraitReason" class="filter-select">
              <option value="Transfert">Transfert vers autre bassin</option>
              <option value="Mort">Décès du poisson</option>
              <option value="Vente">Vente du poisson</option>
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
          <button @click="showRetraitModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmRetrait" class="btn-warning">
            Retirer du bassin
          </button>
        </div>
      </div>
    </div>

    <!-- Modale vente poisson -->
    <div v-if="showVenteModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Vendre le poisson</h3>
          <button @click="showVenteModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Confirmez-vous la vente du poisson <strong>{{ poissonToSell?.nomPoisson }}</strong> ?</p>
          
          <div class="sell-details">
            <div class="detail-item">
              <span>Race:</span>
              <strong>{{ getRaceName(poissonToSell) }}</strong>
            </div>
            <div class="detail-item">
              <span>Poids actuel:</span>
              <strong>{{ poissonToSell?.poidsActuelPoisson || poissonToSell?.poidsInitialPoisson }}g</strong>
            </div>
            <div class="detail-item">
              <span>Prix d'achat:</span>
              <strong class="text-cost">{{ formatCurrency(poissonToSell?.prixAchatPoisson) }}</strong>
            </div>
            <div class="detail-item">
              <span>Prix de vente:</span>
              <strong class="text-profit">{{ formatCurrency(poissonToSell?.prixVentePoisson) }}</strong>
            </div>
            <div class="detail-item">
              <span>Bénéfice:</span>
              <strong class="text-success">
                {{ formatCurrency((poissonToSell?.prixVentePoisson || 0) - (poissonToSell?.prixAchatPoisson || 0)) }}
              </strong>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showVenteModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmVente" class="btn-success">
            Confirmer la vente
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import bassinService from '../services/bassinService'
import poissonService from '../services/poissonService'

export default {
  name: 'DetailBassin',
  
  setup() {
    const route = useRoute()
    const router = useRouter()
    const bassinId = route.params.id
    
    // États principaux
    const bassin = ref({
      nomPiscine: '',
      capaciteMaxPiscine: 0,
      estActivePiscine: true,
      dateCreationPiscine: '',
      volumePiscine: null,
      temperaturePiscine: null,
      phPiscine: null,
      descriptionPiscine: ''
    })
    
    const poissons = ref([])
    const historique = ref([])
    const stats = ref({
      poissonsEnVie: 0,
      poissonsPretAVendre: 0,
      poissonsAffames: 0,
      poidsTotal: 0
    })
    
    const isLoading = ref(false)
    const isLoadingPoissons = ref(false)
    
    // États pour les modales
    const showAffectModal = ref(false)
    const showRetraitModal = ref(false)
    const showVenteModal = ref(false)
    const showHistorique = ref(false)
    
    // États pour les sélections
    const selectedPoisson = ref(null)
    const poissonToRemove = ref(null)
    const poissonToSell = ref(null)
    const poissonsDisponibles = ref([])
    
    // États pour les raisons
    const retraitReason = ref('Transfert')
    const customRetraitReason = ref('')
    
    // États pour les filtres
    const searchQuery = ref('')
    const statusFilter = ref('')
    
    // Charger les données du bassin
    const loadBassinData = async () => {
      isLoading.value = true
      try {
        // Charger les infos du bassin
        const bassinData = await bassinService.getById(bassinId)
        console.log('Données bassin:', bassinData)
        bassin.value = bassinData
        
        // Charger les poissons du bassin
        await loadPoissons()
        
        // Charger l'historique
        await loadHistorique()
        
      } catch (error) {
        console.error('Erreur chargement bassin:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // Charger les poissons du bassin
    const loadPoissons = async () => {
      try {
        const poissonsData = await bassinService.getPoissons(bassinId)
        console.log('Poissons du bassin:', poissonsData)
        
        // S'assurer que c'est un tableau
        poissons.value = Array.isArray(poissonsData) ? poissonsData : []
        
        // Calculer les statistiques
        calculateStats()
        
      } catch (error) {
        console.error('Erreur chargement poissons:', error)
        poissons.value = []
      }
    }
    
    // Charger l'historique
    const loadHistorique = async () => {
      try {
        const historiqueData = await bassinService.getHistorique(bassinId)
        historique.value = Array.isArray(historiqueData) ? historiqueData : []
      } catch (error) {
        console.error('Erreur chargement historique:', error)
        historique.value = []
      }
    }
    
    // Charger les poissons disponibles pour affectation
    const loadPoissonsDisponibles = async () => {
      isLoadingPoissons.value = true
      try {
        // Récupérer tous les poissons en vie
        const allPoissons = await poissonService.getAllPoissons()
        console.log('Tous les poissons:', allPoissons)
        
        // Filtrer les poissons disponibles
        poissonsDisponibles.value = allPoissons.filter(poisson => {
          // Le poisson doit être en vie et non vendu
          const estDisponible = poisson.estEnViePoisson && !poisson.estVenduPoisson
          
          // Vérifier si le poisson n'est pas déjà dans ce bassin
          let pasDansCeBassin = true
          
          // Vérifier de différentes manières selon la structure des données
          if (poisson.piscineActuelle) {
            // Si piscineActuelle est un objet avec idPiscine
            if (poisson.piscineActuelle.idPiscine) {
              pasDansCeBassin = poisson.piscineActuelle.idPiscine !== parseInt(bassinId)
            }
            // Si piscineActuelle est directement l'ID
            else if (typeof poisson.piscineActuelle === 'number') {
              pasDansCeBassin = poisson.piscineActuelle !== parseInt(bassinId)
            }
          }
          
          return estDisponible && pasDansCeBassin
        })
        
        console.log('Poissons disponibles:', poissonsDisponibles.value)
        
      } catch (error) {
        console.error('Erreur chargement poissons disponibles:', error)
        poissonsDisponibles.value = []
      } finally {
        isLoadingPoissons.value = false
      }
    }
    
    // Calculer les statistiques du bassin
    const calculateStats = () => {
      const statsData = {
        poissonsEnVie: 0,
        poissonsPretAVendre: 0,
        poissonsAffames: 0,
        poidsTotal: 0
      }
      
      if (poissons.value && poissons.value.length > 0) {
        poissons.value.forEach(poisson => {
          // Déterminer si le poisson est en vie (par défaut true si non défini)
          const estEnVie = poisson.estEnViePoisson !== false
          const estVendu = poisson.estVenduPoisson === true
          
          if (estEnVie && !estVendu) {
            statsData.poissonsEnVie++
            
            // Vérifier si prêt à vendre (≥95% du poids max)
            if (getProgression(poisson) >= 95) {
              statsData.poissonsPretAVendre++
            }
            
            // Vérifier si affamé (par défaut false si non défini)
            if (poisson.estRassasiePoisson === false) {
              statsData.poissonsAffames++
            }
            
            // Ajouter au poids total
            const poidsActuel = poisson.poidsActuelPoisson || poisson.poidsInitialPoisson || 0
            statsData.poidsTotal += poidsActuel
          }
        })
      }
      
      stats.value = statsData
      console.log('Statistiques calculées:', stats.value)
    }
    
    // Computed properties
    const nombrePoissonsActuels = computed(() => {
      return poissons.value.length
    })
    
    const capaciteRestante = computed(() => {
      return Math.max(0, bassin.value.capaciteMaxPiscine - nombrePoissonsActuels.value)
    })
    
    const tauxOccupation = computed(() => {
      if (!bassin.value.capaciteMaxPiscine || bassin.value.capaciteMaxPiscine <= 0) return 0
      const taux = (nombrePoissonsActuels.value / bassin.value.capaciteMaxPiscine) * 100
      return Math.min(taux, 100).toFixed(1)
    })
    
    const isFull = computed(() => {
      return capaciteRestante.value === 0
    })
    
    const hasTechnicalSpecs = computed(() => {
      return bassin.value.volumePiscine || bassin.value.temperaturePiscine || bassin.value.phPiscine
    })
    
    const filteredPoissons = computed(() => {
      return poissons.value.filter(poisson => {
        // Filtre recherche
        if (searchQuery.value) {
          const searchLower = searchQuery.value.toLowerCase()
          const nomPoisson = poisson.nomPoisson || ''
          const raceNom = getRaceName(poisson) || ''
          
          if (!nomPoisson.toLowerCase().includes(searchLower) &&
              !raceNom.toLowerCase().includes(searchLower)) {
            return false
          }
        }
        
        // Filtre statut
        if (statusFilter.value) {
          const estEnVie = poisson.estEnViePoisson !== false
          const estVendu = poisson.estVenduPoisson === true
          const estRassasie = poisson.estRassasiePoisson !== false
          const progression = getProgression(poisson)
          
          switch (statusFilter.value) {
            case 'en-vie':
              if (!estEnVie || estVendu) return false
              break
            case 'pret-a-vendre':
              if (!estEnVie || estVendu || progression < 95) return false
              break
            case 'affame':
              if (!estEnVie || estVendu || estRassasie) return false
              break
          }
        }
        
        return true
      })
    })
    
    // Méthodes utilitaires
    const getProgression = (poisson) => {
      const poidsMax = poisson.poidsMaximalPoisson || 0
      if (poidsMax <= 0) return 0
      
      const poidsActuel = poisson.poidsActuelPoisson || poisson.poidsInitialPoisson || 0
      const progression = (poidsActuel / poidsMax) * 100
      return Math.min(progression, 100)
    }
    
    const getProgressionClass = (progression) => {
      if (progression >= 95) return 'progress-high'
      if (progression >= 75) return 'progress-medium-high'
      if (progression >= 50) return 'progress-medium'
      if (progression >= 25) return 'progress-low'
      return 'progress-very-low'
    }
    
    const getOccupationClass = (taux) => {
      if (taux === 0) return 'occupation-empty'
      if (taux < 33) return 'occupation-low'
      if (taux < 66) return 'occupation-medium'
      if (taux < 100) return 'occupation-high'
      return 'occupation-full'
    }
    
    const getRaceName = (poisson) => {
      if (!poisson) return 'Inconnue'
      
      // Vérifier différentes structures possibles
      if (poisson.racePoisson) {
        if (typeof poisson.racePoisson === 'object') {
          return poisson.racePoisson.nomRacePoisson || 'Inconnue'
        }
      }
      
      return 'Inconnue'
    }
    
    const getPoissonStatusClass = (poisson) => {
      if (poisson.estVenduPoisson === true) return 'status-sold'
      if (poisson.estEnViePoisson === false) return 'status-dead'
      if (getProgression(poisson) >= 95) return 'status-ready'
      if (poisson.estRassasiePoisson === false) return 'status-hungry'
      return 'status-alive'
    }
    
    const getPoissonStatusText = (poisson) => {
      if (poisson.estVenduPoisson === true) return 'Vendu'
      if (poisson.estEnViePoisson === false) return 'Mort'
      if (getProgression(poisson) >= 95) return 'Prêt à vendre'
      if (poisson.estRassasiePoisson === false) return 'Affamé'
      return 'En vie'
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Date inconnue'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('fr-FR', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        })
      } catch (e) {
        return dateString
      }
    }
    
    const formatCurrency = (amount) => {
      if (!amount && amount !== 0) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(amount)
    }
    
    // Méthodes d'actions
    const refreshData = async () => {
      await loadBassinData()
    }
    
    const toggleBassinStatus = async () => {
      try {
        await bassinService.toggleStatus(bassinId)
        await loadBassinData()
      } catch (error) {
        console.error('Erreur changement statut:', error)
        alert(error.response?.data?.message || 'Erreur lors du changement de statut')
      }
    }
    
    const viderBassin = () => {
      if (nombrePoissonsActuels.value === 0) {
        alert('Ce bassin est déjà vide !')
        return
      }
      
      if (confirm(`Êtes-vous sûr de vouloir vider ce bassin ? Cette action retirera ${nombrePoissonsActuels.value} poisson(s).`)) {
        viderBassinConfirme()
      }
    }
    
    const viderBassinConfirme = async () => {
      try {
        await bassinService.viderBassin(bassinId, 'Vidage manuel')
        await loadBassinData()
        alert('Bassin vidé avec succès !')
      } catch (error) {
        console.error('Erreur vidage bassin:', error)
        alert(error.response?.data?.message || 'Erreur lors du vidage du bassin')
      }
    }
    
    const affecterPoisson = async () => {
      if (bassin.value.estActivePiscine === false) {
        alert('Impossible d\'affecter un poisson à un bassin inactif')
        return
      }
      
      if (isFull.value) {
        alert('Ce bassin a atteint sa capacité maximale')
        return
      }
      
      await loadPoissonsDisponibles()
      selectedPoisson.value = null
      showAffectModal.value = true
    }
    
    const selectPoisson = (poisson) => {
      selectedPoisson.value = poisson
    }
    
    const confirmAffectation = async () => {
      if (!selectedPoisson.value) return
      
      try {
        await bassinService.affecterPoisson(bassinId, selectedPoisson.value.idPoisson)
        await loadBassinData()
        showAffectModal.value = false
        alert('Poisson affecté avec succès !')
      } catch (error) {
        console.error('Erreur affectation:', error)
        alert(error.response?.data?.message || 'Erreur lors de l\'affectation du poisson')
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
        await loadBassinData()
        showRetraitModal.value = false
        alert('Poisson retiré avec succès !')
      } catch (error) {
        console.error('Erreur retrait:', error)
        alert(error.response?.data?.message || 'Erreur lors du retrait du poisson')
      }
    }
    
    const vendrePoisson = (poisson) => {
      poissonToSell.value = poisson
      showVenteModal.value = true
    }
    
    const confirmVente = async () => {
      if (!poissonToSell.value) return
      
      try {
        await poissonService.vendrePoisson(poissonToSell.value.idPoisson)
        await loadBassinData()
        showVenteModal.value = false
        alert('Poisson vendu avec succès !')
      } catch (error) {
        console.error('Erreur vente:', error)
        alert(error.response?.data?.message || 'Erreur lors de la vente du poisson')
      }
    }
    
    const toggleHistorique = () => {
      showHistorique.value = !showHistorique.value
    }
    
    // Initialisation
    onMounted(() => {
      loadBassinData()
    })
    
    return {
      // États
      bassin,
      poissons,
      historique,
      stats,
      isLoading,
      isLoadingPoissons,
      showAffectModal,
      showRetraitModal,
      showVenteModal,
      showHistorique,
      selectedPoisson,
      poissonToRemove,
      poissonToSell,
      poissonsDisponibles,
      retraitReason,
      customRetraitReason,
      searchQuery,
      statusFilter,
      
      // Computed
      nombrePoissonsActuels,
      capaciteRestante,
      tauxOccupation,
      isFull,
      hasTechnicalSpecs,
      filteredPoissons,
      
      // Méthodes
      refreshData,
      toggleBassinStatus,
      viderBassin,
      affecterPoisson,
      selectPoisson,
      confirmAffectation,
      retirerPoisson,
      confirmRetrait,
      vendrePoisson,
      confirmVente,
      toggleHistorique,
      getProgression,
      getProgressionClass,
      getOccupationClass,
      getRaceName,
      getPoissonStatusClass,
      getPoissonStatusText,
      formatDate,
      formatCurrency
    }
  }
}
</script>

<style scoped>
/* Les styles restent exactement les mêmes que dans la version précédente */
.detail-bassin {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

/* En-tête */
.page-header {
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
  flex-direction: column;
  gap: 10px;
}

.btn-back {
  color: #4a5568;
  text-decoration: none;
  font-weight: 600;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
  width: fit-content;
}

.btn-back:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.page-header h1 {
  margin: 0;
  color: #2d3748;
  font-size: 28px;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-status,
.btn-edit,
.btn-empty {
  padding: 10px 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-status {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-status:hover {
  background: #cbd5e0;
}

.btn-inactive {
  background: #fed7d7;
  color: #742a2a;
}

.btn-inactive:hover {
  background: #feb2b2;
}

.btn-active {
  background: #c6f6d5;
  color: #22543d;
}

.btn-active:hover {
  background: #9ae6b4;
}

.btn-edit {
  background: #bee3f8;
  color: #2c5282;
  text-decoration: none;
}

.btn-edit:hover {
  background: #90cdf4;
}

.btn-empty {
  background: #feebc8;
  color: #744210;
}

.btn-empty:hover:not(:disabled) {
  background: #fbd38d;
}

.btn-empty:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Alertes */
.alert {
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert.warning {
  background: #fffaf0;
  color: #744210;
  border-left: 4px solid #ed8936;
}

.alert.info {
  background: #ebf8ff;
  color: #2c5282;
  border-left: 4px solid #4299e1;
}

.alert-icon {
  font-size: 20px;
}

/* Vue d'ensemble du bassin */
.bassin-overview {
  margin-bottom: 30px;
}

.overview-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.bassin-title h2 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 24px;
}

.bassin-status {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
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

.date-created {
  color: #718096;
  font-size: 14px;
}

.occupation-display {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 300px;
}

.occupation-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.occupation-label {
  color: #718096;
  font-size: 14px;
}

.occupation-value {
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

.occupation-bar-large {
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.occupation-fill-large {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s;
}

.occupation-empty { background: #a0aec0; }
.occupation-low { background: #48bb78; }
.occupation-medium { background: #ed8936; }
.occupation-high { background: #f56565; }
.occupation-full { background: #742a2a; }

.occupation-details {
  display: flex;
  justify-content: space-between;
  color: #718096;
  font-size: 14px;
}

/* Statistiques du bassin */
.bassin-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  background: #f7fafc;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
}

.stat-content h3 {
  margin: 0;
  color: #718096;
  font-size: 14px;
  font-weight: 600;
}

.stat-value {
  margin: 5px 0 0 0;
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

/* Caractéristiques techniques */
.technical-specs {
  margin-bottom: 30px;
}

.technical-specs h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.spec-card {
  background: #f7fafc;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.spec-icon {
  font-size: 32px;
}

.spec-content h4 {
  margin: 0;
  color: #4a5568;
  font-size: 14px;
  font-weight: 600;
}

.spec-content p {
  margin: 5px 0 0 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: bold;
}

/* Description */
.description-section {
  margin-bottom: 30px;
}

.description-section h3 {
  margin: 0 0 15px 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.description-content {
  background: #f7fafc;
  padding: 20px;
  border-radius: 10px;
  color: #4a5568;
  line-height: 1.6;
}

/* Section poissons */
.poissons-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-actions {
  display: flex;
  gap: 15px;
}

.btn-primary {
  padding: 10px 20px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #3182ce;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
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

.btn-secondary:hover {
  background: #cbd5e0;
}

/* Tableau des poissons */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-info {
  color: #718096;
}

.result-count {
  font-weight: 600;
}

.table-filters {
  display: flex;
  gap: 15px;
}

.search-input,
.filter-select {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

/* Table responsive */
.table-responsive {
  overflow-x: auto;
}

.poissons-table {
  width: 100%;
  border-collapse: collapse;
}

.poissons-table thead {
  background: #f7fafc;
}

.poissons-table th {
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.poissons-table td {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}

.poissons-table tr:hover {
  background: #f7fafc;
}

/* Styles des cellules */
.cell-name .name-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fish-icon {
  font-size: 24px;
}

.name-details {
  display: flex;
  flex-direction: column;
}

.text-muted {
  color: #718096;
  font-size: 12px;
}

.race-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  background: #bee3f8;
  color: #2c5282;
}

.weight-info {
  display: flex;
  flex-direction: column;
}

.progression-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progression-bar-container {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.progression-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-very-low { background: #f56565; }
.progress-low { background: #ed8936; }
.progress-medium { background: #ecc94b; }
.progress-medium-high { background: #48bb78; }
.progress-high { background: #38b2ac; }

.progression-text {
  min-width: 45px;
  text-align: right;
  font-weight: 600;
  color: #2d3748;
}

.status-container {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.status-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  width: fit-content;
}

.status-alive { background: #c6f6d5; color: #22543d; }
.status-hungry { background: #feebc8; color: #744210; }
.status-ready { background: #d6bcfa; color: #553c9a; }
.status-dead { background: #fed7d7; color: #742a2a; }
.status-sold { background: #bee3f8; color: #2c5282; }

.status-icons {
  display: flex;
  gap: 5px;
}

.status-icon {
  font-size: 14px;
}

/* Boutons d'action dans le tableau */
.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  text-decoration: none;
}

.btn-view {
  background: #bee3f8;
  color: #2c5282;
}

.btn-view:hover {
  background: #90cdf4;
}

.btn-remove {
  background: #feebc8;
  color: #744210;
}

.btn-remove:hover {
  background: #fbd38d;
}

.btn-sell {
  background: #d6bcfa;
  color: #553c9a;
}

.btn-sell:hover {
  background: #b794f4;
}

/* Section historique */
.historique-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.historique-section .section-header {
  margin-bottom: 20px;
}

.btn-link {
  background: none;
  border: none;
  color: #4299e1;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.btn-link:hover {
  text-decoration: underline;
}

.historique-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.historique-item {
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  gap: 20px;
  border-left: 4px solid #4299e1;
}

.historique-date {
  min-width: 200px;
  font-weight: 600;
  color: #2d3748;
}

.historique-content {
  flex: 1;
}

.historique-action {
  margin-bottom: 5px;
}

.historique-reason {
  color: #718096;
  font-size: 14px;
  font-style: italic;
}

.no-historique {
  text-align: center;
  padding: 30px;
  color: #718096;
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

/* Styles spécifiques pour les modales */
.loading-modal {
  text-align: center;
  padding: 40px;
}

.spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

.poissons-list-modal {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.poisson-item {
  padding: 15px;
  cursor: pointer;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s;
}

.poisson-item:last-child {
  border-bottom: none;
}

.poisson-item:hover {
  background: #f7fafc;
}

.poisson-item.selected {
  background: #ebf8ff;
  border-left: 4px solid #4299e1;
}

.poisson-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.poisson-details {
  display: flex;
  gap: 15px;
  margin-top: 5px;
  color: #718096;
  font-size: 14px;
}

.poisson-status {
  margin-left: auto;
}

.status-badge-small {
  padding: 3px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.no-poissons-modal {
  text-align: center;
  padding: 30px;
}

/* Styles pour les détails de vente */
.sell-details {
  background: #f7fafc;
  padding: 20px;
  border-radius: 8px;
  margin-top: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e2e8f0;
}

.detail-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.text-cost { color: #f56565; }
.text-profit { color: #48bb78; }
.text-success { color: #38a169; }

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

.btn-warning {
  padding: 10px 20px;
  background: #ed8936;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-warning:hover {
  background: #dd6b20;
}

.btn-success {
  padding: 10px 20px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-success:hover {
  background: #38a169;
}

/* États de chargement et messages */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
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

.no-filter-results {
  text-align: center;
  padding: 30px;
  color: #718096;
  border-top: 1px solid #e2e8f0;
}

.no-poissons {
  text-align: center;
  padding: 60px 20px;
}

.no-poissons-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.no-poissons h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

.no-poissons p {
  color: #718096;
  margin-bottom: 20px;
}

/* Responsive */
@media (max-width: 1024px) {
  .overview-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .occupation-display {
    min-width: 100%;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-right {
    justify-content: flex-start;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .table-filters {
    width: 100%;
    flex-direction: column;
  }
  
  .historique-item {
    flex-direction: column;
    gap: 10px;
  }
  
  .historique-date {
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .bassin-stats {
    grid-template-columns: 1fr;
  }
  
  .specs-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
}
</style>