<template>
  <div class="nouveau-poisson">
    <div class="page-header">
      <div class="header-left">
        <router-link to="/poissons" class="btn-back">
          ← Retour à la liste
        </router-link>
        <h1>Ajouter un nouveau poisson</h1>
      </div>
      <div class="header-right">
        <button @click="resetForm" class="btn-reset" :disabled="isSubmitting">
          🔄 Réinitialiser
        </button>
      </div>
    </div>

    <!-- Messages d'alerte -->
    <div v-if="errorMessage" class="alert error">
      <span class="alert-icon">⚠️</span>
      <span>{{ errorMessage }}</span>
      <button @click="errorMessage = ''" class="alert-close">×</button>
    </div>

    <div v-if="successMessage" class="alert success">
      <span class="alert-icon">✅</span>
      <span>{{ successMessage }}</span>
      <button @click="successMessage = ''" class="alert-close">×</button>
    </div>

    <div class="form-container">
      <form @submit.prevent="submitForm" class="poisson-form">
        
        <!-- Section informations de base -->
        <div class="form-section">
          <h2>📝 Informations de base</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="nom">Nom du poisson *</label>
              <input
                type="text"
                id="nom"
                v-model="form.nomPoisson"
                :class="{ 'error': errors.nomPoisson }"
                placeholder="Ex: Tilapia-001"
                required
              />
              <span v-if="errors.nomPoisson" class="error-message">{{ errors.nomPoisson }}</span>
            </div>

            <div class="form-group">
              <label for="race">Race *</label>
              <select
                id="race"
                v-model="form.idRacePoisson"
                :class="{ 'error': errors.idRacePoisson }"
                required
              >
                <option value="">Sélectionner une race</option>
                <option v-for="race in races" :key="race.idRacePoisson" :value="race.idRacePoisson">
                  {{ race.nomRacePoisson }}
                </option>
              </select>
              <span v-if="errors.idRacePoisson" class="error-message">{{ errors.idRacePoisson }}</span>
              <button type="button" @click="showAddRaceModal = true" class="btn-link">
                + Ajouter une nouvelle race
              </button>
            </div>

            <div class="form-group">
              <label for="dateArrivee">Date d'arrivée *</label>
              <input
                type="date"
                id="dateArrivee"
                v-model="form.dateArriveePoisson"
                :class="{ 'error': errors.dateArriveePoisson }"
                required
              />
              <span v-if="errors.dateArriveePoisson" class="error-message">{{ errors.dateArriveePoisson }}</span>
            </div>

            <!-- AJOUT: Sélection du bassin -->
            <div class="form-group">
              <label for="bassin">Bassin d'affectation</label>
              <select
                id="bassin"
                v-model="form.idPiscineActuelle"
                :class="{ 'error': errors.idPiscineActuelle }"
                :disabled="loadingPiscines"
              >
                <option value="">Sélectionner un bassin (optionnel)</option>
                <option v-for="piscine in piscinesDisponibles" :key="piscine.idPiscine" :value="piscine.idPiscine">
                  {{ getPiscineOptionLabel(piscine) }}
                </option>
                <option v-if="piscinesDisponibles.length === 0 && !loadingPiscines" value="" disabled>
                  Aucun bassin disponible
                </option>
                <option v-if="loadingPiscines" value="" disabled>
                  Chargement des bassins...
                </option>
              </select>
              <span v-if="errors.idPiscineActuelle" class="error-message">{{ errors.idPiscineActuelle }}</span>
              <div class="input-info">
                <button type="button" @click="loadPiscines" class="btn-link" :disabled="loadingPiscines">
                  🔄 Actualiser la liste
                </button>
                <span v-if="loadingPiscines">Chargement...</span>
                <span v-else-if="form.idPiscineActuelle">
                  Bassin sélectionné: {{ getPiscineNom() }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Section poids -->
        <div class="form-section">
          <h2>⚖️ Informations sur le poids</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="poidsInitial">Poids initial (g) *</label>
              <input
                type="number"
                id="poidsInitial"
                v-model="form.poidsInitialPoisson"
                :class="{ 'error': errors.poidsInitialPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.poidsInitialPoisson" class="error-message">{{ errors.poidsInitialPoisson }}</span>
              <div class="input-info">
                Poids du poisson à son arrivée
              </div>
            </div>

            <div class="form-group">
              <label for="poidsMaximal">Poids maximal (g) *</label>
              <input
                type="number"
                id="poidsMaximal"
                v-model="form.poidsMaximalPoisson"
                :class="{ 'error': errors.poidsMaximalPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.poidsMaximalPoisson" class="error-message">{{ errors.poidsMaximalPoisson }}</span>
              <div class="input-info">
                Poids maximum que le poisson peut atteindre
              </div>
            </div>

            <div class="form-group">
              <label for="capaciteAugmentation">Capacité augmentation/jour (g) *</label>
              <input
                type="number"
                id="capaciteAugmentation"
                v-model="form.capaciteAugmentationPoisson"
                :class="{ 'error': errors.capaciteAugmentationPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.capaciteAugmentationPoisson" class="error-message">{{ errors.capaciteAugmentationPoisson }}</span>
              <div class="input-info">
                Gain de poids maximal par jour
              </div>
            </div>

            <div class="form-group">
              <div class="progress-indicator">
                <label>Progression initiale</label>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: progressionInitiale + '%' }"
                    :class="getProgressionClass(progressionInitiale)"
                  ></div>
                </div>
                <span class="progress-text">{{ progressionInitiale }}%</span>
              </div>
              <div class="input-info">
                {{ poidsRestant }} g restant pour atteindre le poids maximal
              </div>
            </div>
          </div>
        </div>

        <!-- Section financière -->
        <div class="form-section">
          <h2>💰 Informations financières</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="prixAchat">Prix d'achat (MGA) *</label>
              <input
                type="number"
                id="prixAchat"
                v-model="form.prixAchatPoisson"
                :class="{ 'error': errors.prixAchatPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.prixAchatPoisson" class="error-message">{{ errors.prixAchatPoisson }}</span>
              <div class="input-info">
                Coût d'achat du poisson
              </div>
            </div>

            <div class="form-group">
              <label for="prixVente">Prix de vente estimé (MGA) *</label>
              <input
                type="number"
                id="prixVente"
                v-model="form.prixVentePoisson"
                :class="{ 'error': errors.prixVentePoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.prixVentePoisson" class="error-message">{{ errors.prixVentePoisson }}</span>
              <div class="input-info">
                Prix de vente lorsque le poisson atteint son poids maximal
              </div>
            </div>

            <div class="form-group">
              <div class="profit-preview">
                <label>Bénéfice potentiel</label>
                <div class="profit-value" :class="getProfitClass">
                  {{ formatCurrency(beneficePotentiel) }}
                </div>
                <div class="profit-details">
                  <span>Marge: {{ margePourcentage }}%</span>
                  <span>Retour sur investissement: {{ roiPourcentage }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section statut -->
        <div class="form-section">
          <h2>📊 Statut initial</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="form.estRassasiePoisson"
                />
                <span class="checkmark"></span>
                <span>Poisson rassasié à l'arrivée</span>
              </label>
              <div class="input-info">
                Si décoché, le poisson sera marqué comme affamé
              </div>
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="form.estEnViePoisson"
                />
                <span class="checkmark"></span>
                <span>Poisson en vie</span>
              </label>
              <div class="input-info">
                Décochez si le poisson arrive déjà mort
              </div>
            </div>
          </div>
        </div>

        <!-- Résumé -->
        <div class="form-section summary">
          <h2>📋 Résumé</h2>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">Nom</span>
              <span class="summary-value">{{ form.nomPoisson || 'Non défini' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Race</span>
              <span class="summary-value">{{ getRaceName() || 'Non sélectionnée' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Bassin</span>
              <span class="summary-value">{{ getPiscineNom() || 'Aucun' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Poids</span>
              <span class="summary-value">{{ form.poidsInitialPoisson || 0 }}g → {{ form.poidsMaximalPoisson || 0 }}g</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Progression</span>
              <span class="summary-value">{{ progressionInitiale }}%</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Investissement</span>
              <span class="summary-value">{{ formatCurrency(form.prixAchatPoisson) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Bénéfice potentiel</span>
              <span class="summary-value" :class="getProfitClass">
                {{ formatCurrency(beneficePotentiel) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Actions du formulaire -->
        <div class="form-actions">
          <router-link to="/poissons" class="btn-cancel">
            Annuler
          </router-link>
          <button 
            type="submit" 
            class="btn-submit"
            :disabled="isSubmitting || !isFormValid || loadingPiscines"
          >
            <span v-if="isSubmitting" class="spinner"></span>
            {{ isSubmitting ? 'Création en cours...' : 'Créer le poisson' }}
          </button>
        </div>
      </form>

      <!-- Aperçu -->
      <div class="preview-container">
        <div class="preview-card">
          <h3>Aperçu du poisson</h3>
          <div class="preview-content">
            <div class="preview-header">
              <div class="preview-name">{{ form.nomPoisson || 'Nouveau poisson' }}</div>
              <div class="preview-race">{{ getRaceName() || 'Race non définie' }}</div>
            </div>
            
            <!-- AJOUT: Information du bassin dans l'aperçu -->
            <div class="preview-location" v-if="form.idPiscineActuelle">
              <div class="location-item">
                <span class="location-label">📍 Bassin</span>
                <span class="location-value">{{ getPiscineNom() }}</span>
              </div>
              <div class="location-item" v-if="getPiscineInfo()">
                <span class="location-label">Capacité</span>
                <span class="location-value">{{ getPiscineInfo() }}</span>
              </div>
            </div>
            <div class="preview-location" v-else>
              <div class="location-item">
                <span class="location-label">📍 Bassin</span>
                <span class="location-value warning">Aucun bassin sélectionné</span>
              </div>
            </div>

            <div class="preview-stats">
              <div class="stat-item">
                <span class="stat-label">Poids actuel</span>
                <span class="stat-value">{{ form.poidsInitialPoisson || 0 }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Poids max</span>
                <span class="stat-value">{{ form.poidsMaximalPoisson || 0 }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Progression</span>
                <div class="progress-container">
                  <div class="progress-bar-small">
                    <div 
                      class="progress-fill-small"
                      :style="{ width: progressionInitiale + '%' }"
                    ></div>
                  </div>
                  <span class="progress-percent">{{ progressionInitiale }}%</span>
                </div>
              </div>
            </div>

            <div class="preview-financial">
              <div class="financial-item">
                <span class="financial-label">Coût d'achat</span>
                <span class="financial-value cost">{{ formatCurrency(form.prixAchatPoisson) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">Vente estimée</span>
                <span class="financial-value sale">{{ formatCurrency(form.prixVentePoisson) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">Bénéfice</span>
                <span class="financial-value profit" :class="getProfitClass">
                  {{ formatCurrency(beneficePotentiel) }}
                </span>
              </div>
            </div>

            <div class="preview-status">
              <div class="status-badges">
                <span class="status-badge" :class="getStatusClass('vie')">
                  {{ form.estEnViePoisson ? 'En vie' : 'Mort' }}
                </span>
                <span class="status-badge" :class="getStatusClass('nourri')">
                  {{ form.estRassasiePoisson ? 'Rassasié' : 'Affamé' }}
                </span>
                <span class="status-badge" :class="getStatusClass('vente')">
                  {{ estPretAVendre ? 'Prêt à vendre' : 'En croissance' }}
                </span>
                <span v-if="form.idPiscineActuelle" class="status-badge status-assigned">
                  Bassin assigné
                </span>
                <span v-else class="status-badge status-unassigned">
                  Sans bassin
                </span>
              </div>
            </div>

            <div class="preview-dates">
              <div class="date-item">
                <span class="date-label">Date d'arrivée</span>
                <span class="date-value">{{ formatDatePreview(form.dateArriveePoisson) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Conseils -->
        <div class="tips-card">
          <h3>💡 Conseils</h3>
          <ul class="tips-list">
            <li>Nommez vos poissons de façon identifiable (ex: Tilapia-001, Carpe-A)</li>
            <li>Le poids initial doit être inférieur au poids maximal</li>
            <li>Le prix de vente doit être supérieur au prix d'achat pour faire un bénéfice</li>
            <li>La capacité d'augmentation dépend de la race (20g/jour pour Tilapia, 15g/jour pour Carpe)</li>
            <li>Un poisson est prêt à vendre quand il atteint 95% de son poids maximal</li>
            <li>Affectez immédiatement le poisson à un bassin pour suivre son historique</li>
            <li>Les bassins avec places restantes sont indiqués par leur capacité disponible</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Modale ajout race -->
    <div v-if="showAddRaceModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Ajouter une nouvelle race</h3>
          <button @click="showAddRaceModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="newRaceName">Nom de la race *</label>
            <input
              type="text"
              id="newRaceName"
              v-model="newRaceName"
              placeholder="Ex: Tilapia du Nil"
            />
          </div>
          <div class="form-group">
            <label for="newRaceDescription">Description</label>
            <textarea
              id="newRaceDescription"
              v-model="newRaceDescription"
              placeholder="Description de la race..."
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddRaceModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="addNewRace" class="btn-submit">
            Ajouter la race
          </button>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import poissonService from '../services/poissonService'
import bassinService from '../services/bassinService'

export default {
  name: 'NouveauPoisson',
  setup() {
    const router = useRouter()
    
    // État du formulaire
    const form = ref({
      nomPoisson: '',
      idRacePoisson: '',
      prixAchatPoisson: 0,
      prixVentePoisson: 0,
      poidsMaximalPoisson: 0,
      capaciteAugmentationPoisson: 20,
      poidsInitialPoisson: 0,
      dateArriveePoisson: new Date().toISOString().split('T')[0],
      estRassasiePoisson: true,
      estEnViePoisson: true,
      idPiscineActuelle: ''
    })

    // État de l'application
    const races = ref([])
    const piscinesDisponibles = ref([])
    const errors = ref({})
    const isSubmitting = ref(false)
    const loadingPiscines = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')
    const showAddRaceModal = ref(false)
    const newRaceName = ref('')
    const newRaceDescription = ref('')

    // Charger les données initiales
    const loadInitialData = async () => {
      try {
        // Charger les races
        const racesData = await poissonService.getRacesPoisson()
        races.value = racesData
        
        // Charger les bassins disponibles
        await loadPiscines()
        
        // Si aucune race, proposer d'en créer une
        if (races.value.length === 0) {
          showAddRaceModal.value = true
        }
      } catch (error) {
        console.error('Erreur chargement données initiales:', error)
        errorMessage.value = 'Impossible de charger les données initiales'
      }
    }

    // Fonction pour extraire les données bassins du JSON tronqué
    const extractPiscinesFromString = (str) => {
      if (!str || typeof str !== 'string') return []
      
      console.log('Tentative d\'extraction depuis chaîne de longueur:', str.length)
      
      try {
        // Chercher le début du tableau de bassins
        const startIndex = str.indexOf('[{')
        if (startIndex === -1) {
          console.warn('Aucun tableau trouvé dans la chaîne')
          return []
        }
        
        // Prendre un morceau de la chaîne (les premiers 5000 caractères après le début)
        // Cela évite les problèmes de récursion infinie
        const chunk = str.substring(startIndex, startIndex + 5000)
        
        // Trouver la fin du premier objet bassin
        let depth = 0
        let endIndex = -1
        
        for (let i = 0; i < chunk.length; i++) {
          if (chunk[i] === '{') depth++
          if (chunk[i] === '}') depth--
          
          if (depth === 0 && i > 0) {
            endIndex = i + 1
            break
          }
        }
        
        if (endIndex === -1) {
          console.warn('Impossible de trouver la fin du premier objet')
          return []
        }
        
        // Extraire le premier objet complet
        const firstObject = chunk.substring(0, endIndex)
        console.log('Premier objet extrait:', firstObject.substring(0, 200) + '...')
        
        // Parser cet objet
        const parsedObject = JSON.parse(firstObject)
        
        // Créer un tableau avec seulement cet objet (nous en avons besoin d'un seul pour le test)
        return [parsedObject]
        
      } catch (error) {
        console.error('Erreur extraction JSON:', error)
        return []
      }
    }

    // Charger les bassins disponibles avec une approche simplifiée
    const loadPiscines = async () => {
      loadingPiscines.value = true
      errorMessage.value = ''
      
      try {
        let piscinesData = []
        
        // OPTION 1: Essayer un endpoint simplifié (si disponible)
        try {
          // Créer un endpoint temporaire simple
          const response = await fetch('/api/piscines/simple-list')
          if (response.ok) {
            piscinesData = await response.json()
            console.log('Données endpoint simple:', piscinesData)
          } else {
            throw new Error('Endpoint simple non disponible')
          }
        } catch (simpleError) {
          console.warn('Endpoint simple non disponible, tentative avec méthode standard:', simpleError)
          
          // OPTION 2: Utiliser getAll mais avec gestion des erreurs
          piscinesData = await bassinService.getAll()
          console.log('Type de données reçu:', typeof piscinesData)
          
          // Si c'est une chaîne tronquée, essayer d'extraire
          if (typeof piscinesData === 'string' && piscinesData.includes('[{')) {
            console.log('Données sont une chaîne JSON, tentative d\'extraction...')
            piscinesData = extractPiscinesFromString(piscinesData)
          }
        }
        
        // S'assurer que nous avons un tableau
        if (!Array.isArray(piscinesData)) {
          console.warn('Forçage en tableau:', piscinesData)
          piscinesData = piscinesData ? [piscinesData] : []
        }
        
        console.log('Bassins à traiter:', piscinesData.length)
        
        // Créer une liste simplifiée de bassins
        const bassinsSimplifies = piscinesData.map(p => {
          // Extraire uniquement les informations essentielles
          const bassin = {
            idPiscine: p.idPiscine || 0,
            nomPiscine: p.nomPiscine || 'Bassin inconnu',
            capaciteMaxPiscine: p.capaciteMaxPiscine || 0,
            estActivePiscine: p.estActivePiscine !== false,
            descriptionPiscine: p.descriptionPiscine || ''
          }
          
          // Estimer le nombre de poissons (simplifié)
          // Si nous avons des poissons dans les données, compter
          let nombrePoissons = 0
          if (p.poissons && Array.isArray(p.poissons)) {
            nombrePoissons = p.poissons.length
          }
          
          bassin.nombrePoissons = nombrePoissons
          bassin.placesRestantes = Math.max(0, bassin.capaciteMaxPiscine - nombrePoissons)
          bassin.occupationText = `${nombrePoissons}/${bassin.capaciteMaxPiscine}`
          
          return bassin
        })
        
        // Filtrer seulement les bassins actifs avec de la place
        piscinesDisponibles.value = bassinsSimplifies.filter(p => 
          p.estActivePiscine && 
          p.placesRestantes > 0 &&
          p.idPiscine > 0
        )
        
        console.log('Bassins disponibles après filtrage:', piscinesDisponibles.value.length)
        
        // Si aucun bassin disponible, créer une liste de secours
        if (piscinesDisponibles.value.length === 0) {
          console.log('Aucun bassin disponible dans les données, création liste de secours...')
          
          // Liste de secours basée sur les données de la base
          piscinesDisponibles.value = [
            { idPiscine: 1, nomPiscine: 'Bassin Nord', capaciteMaxPiscine: 50, placesRestantes: 50, estActivePiscine: true, occupationText: '0/50' },
            { idPiscine: 2, nomPiscine: 'Bassin Sud', capaciteMaxPiscine: 30, placesRestantes: 29, estActivePiscine: true, occupationText: '1/30' },
            { idPiscine: 3, nomPiscine: 'Nurserie', capaciteMaxPiscine: 100, placesRestantes: 100, estActivePiscine: true, occupationText: '0/100' },
            { idPiscine: 4, nomPiscine: 'Bassin Quarantaine', capaciteMaxPiscine: 50, placesRestantes: 50, estActivePiscine: true, occupationText: '0/50' }
          ]
        }
        
        // Trier par nom
        piscinesDisponibles.value.sort((a, b) => {
          return (a.nomPiscine || '').localeCompare(b.nomPiscine || '')
        })
        
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
        
        // Liste de secours en cas d'erreur
        errorMessage.value = 'Impossible de charger les bassins, utilisation liste de secours'
        
        piscinesDisponibles.value = [
          { idPiscine: 1, nomPiscine: 'Bassin Nord', capaciteMaxPiscine: 50, placesRestantes: 50, estActivePiscine: true, occupationText: '0/50' },
          { idPiscine: 2, nomPiscine: 'Bassin Sud', capaciteMaxPiscine: 30, placesRestantes: 29, estActivePiscine: true, occupationText: '1/30' },
          { idPiscine: 3, nomPiscine: 'Nurserie', capaciteMaxPiscine: 100, placesRestantes: 100, estActivePiscine: true, occupationText: '0/100' },
          { idPiscine: 4, nomPiscine: 'Bassin Quarantaine', capaciteMaxPiscine: 50, placesRestantes: 50, estActivePiscine: true, occupationText: '0/50' }
        ]
        
      } finally {
        loadingPiscines.value = false
      }
    }

    // Computed properties
    const progressionInitiale = computed(() => {
      if (!form.value.poidsInitialPoisson || !form.value.poidsMaximalPoisson) return 0
      const progression = (form.value.poidsInitialPoisson / form.value.poidsMaximalPoisson) * 100
      return Math.min(Math.round(progression * 100) / 100, 100)
    })

    const poidsRestant = computed(() => {
      if (!form.value.poidsMaximalPoisson) return 0
      const restant = form.value.poidsMaximalPoisson - (form.value.poidsInitialPoisson || 0)
      return Math.max(0, Math.round(restant * 100) / 100)
    })

    const beneficePotentiel = computed(() => {
      if (!form.value.prixVentePoisson || !form.value.prixAchatPoisson) return 0
      return form.value.prixVentePoisson - form.value.prixAchatPoisson
    })

    const margePourcentage = computed(() => {
      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) return 0
      const marge = (beneficePotentiel.value / form.value.prixAchatPoisson) * 100
      return Math.round(marge * 100) / 100
    })

    const roiPourcentage = computed(() => {
      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) return 0
      const roi = (beneficePotentiel.value / form.value.prixAchatPoisson) * 100
      return Math.round(roi)
    })

    const estPretAVendre = computed(() => {
      return progressionInitiale.value >= 95
    })

    const getProfitClass = computed(() => {
      if (beneficePotentiel.value > 0) return 'profit-positive'
      if (beneficePotentiel.value < 0) return 'profit-negative'
      return 'profit-neutral'
    })

    const isFormValid = computed(() => {
      return form.value.nomPoisson &&
             form.value.idRacePoisson &&
             form.value.poidsInitialPoisson > 0 &&
             form.value.poidsMaximalPoisson > 0 &&
             form.value.prixAchatPoisson > 0 &&
             form.value.prixVentePoisson > 0 &&
             form.value.dateArriveePoisson &&
             form.value.poidsInitialPoisson <= form.value.poidsMaximalPoisson &&
             form.value.prixVentePoisson > form.value.prixAchatPoisson
    })

    // Méthodes
    const getRaceName = () => {
      const race = races.value.find(r => r.idRacePoisson == form.value.idRacePoisson)
      return race ? race.nomRacePoisson : ''
    }

    const getPiscineNom = () => {
      if (!form.value.idPiscineActuelle) return ''
      const piscine = piscinesDisponibles.value.find(p => p.idPiscine == form.value.idPiscineActuelle)
      return piscine ? piscine.nomPiscine : ''
    }

    const getPiscineInfo = () => {
      if (!form.value.idPiscineActuelle) return ''
      const piscine = piscinesDisponibles.value.find(p => p.idPiscine == form.value.idPiscineActuelle)
      if (!piscine) return ''
      
      return piscine.occupationText || `Capacité: ${piscine.capaciteMaxPiscine || 0}`
    }

    const getPiscineOptionLabel = (piscine) => {
      if (!piscine) return ''
      
      let label = piscine.nomPiscine || 'Bassin sans nom'
      
      if (piscine.placesRestantes !== undefined) {
        label += ` - ${piscine.occupationText} (${piscine.placesRestantes} places)`
      } else if (piscine.capaciteMaxPiscine) {
        label += ` - ${piscine.capaciteMaxPiscine} places`
      }
      
      return label
    }

    const getProgressionClass = (progression) => {
      if (progression >= 95) return 'progress-high'
      if (progression >= 70) return 'progress-medium'
      if (progression >= 40) return 'progress-low'
      return 'progress-very-low'
    }

    const getStatusClass = (type) => {
      switch (type) {
        case 'vie':
          return form.value.estEnViePoisson ? 'status-alive' : 'status-dead'
        case 'nourri':
          return form.value.estRassasiePoisson ? 'status-fed' : 'status-hungry'
        case 'vente':
          return estPretAVendre.value ? 'status-ready' : 'status-growing'
        default:
          return ''
      }
    }

    const formatCurrency = (amount) => {
      if (!amount) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 2
      }).format(amount)
    }

    const formatDatePreview = (dateString) => {
      if (!dateString) return 'Non définie'
      return new Date(dateString).toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const validateForm = () => {
      errors.value = {}

      // Validation standard
      if (!form.value.nomPoisson.trim()) {
        errors.value.nomPoisson = 'Le nom est requis'
      }

      if (!form.value.idRacePoisson) {
        errors.value.idRacePoisson = 'La race est requise'
      }

      if (!form.value.poidsInitialPoisson || form.value.poidsInitialPoisson <= 0) {
        errors.value.poidsInitialPoisson = 'Le poids initial doit être positif'
      }

      if (!form.value.poidsMaximalPoisson || form.value.poidsMaximalPoisson <= 0) {
        errors.value.poidsMaximalPoisson = 'Le poids maximal doit être positif'
      }

      if (form.value.poidsInitialPoisson > form.value.poidsMaximalPoisson) {
        errors.value.poidsInitialPoisson = 'Le poids initial doit être inférieur au poids maximal'
      }

      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) {
        errors.value.prixAchatPoisson = 'Le prix d\'achat doit être positif'
      }

      if (!form.value.prixVentePoisson || form.value.prixVentePoisson <= 0) {
        errors.value.prixVentePoisson = 'Le prix de vente doit être positif'
      }

      if (form.value.prixVentePoisson <= form.value.prixAchatPoisson) {
        errors.value.prixVentePoisson = 'Le prix de vente doit être supérieur au prix d\'achat'
      }

      if (!form.value.dateArriveePoisson) {
        errors.value.dateArriveePoisson = 'La date d\'arrivée est requise'
      }

      if (!form.value.capaciteAugmentationPoisson || form.value.capaciteAugmentationPoisson <= 0) {
        errors.value.capaciteAugmentationPoisson = 'La capacité d\'augmentation doit être positive'
      }

      return Object.keys(errors.value).length === 0
    }

    const submitForm = async () => {
      if (!validateForm()) {
        errorMessage.value = 'Veuillez corriger les erreurs dans le formulaire'
        return
      }

      isSubmitting.value = true
      errorMessage.value = ''

      try {
        const poissonData = {
          nomPoisson: form.value.nomPoisson,
          racePoisson: {
            idRacePoisson: form.value.idRacePoisson
          },
          prixAchatPoisson: parseFloat(form.value.prixAchatPoisson),
          prixVentePoisson: parseFloat(form.value.prixVentePoisson),
          poidsMaximalPoisson: parseFloat(form.value.poidsMaximalPoisson),
          capaciteAugmentationPoisson: parseFloat(form.value.capaciteAugmentationPoisson),
          poidsInitialPoisson: parseFloat(form.value.poidsInitialPoisson),
          poidsActuelPoisson: parseFloat(form.value.poidsInitialPoisson),
          dateArriveePoisson: form.value.dateArriveePoisson,
          estRassasiePoisson: form.value.estRassasiePoisson,
          estEnViePoisson: form.value.estEnViePoisson,
          estVenduPoisson: false
        }

        // Inclure le bassin si sélectionné
        if (form.value.idPiscineActuelle) {
          poissonData.piscineActuelle = {
            idPiscine: form.value.idPiscineActuelle
          }
        }

        await poissonService.createPoisson(poissonData)
        
        successMessage.value = 'Poisson créé avec succès !'
        
        setTimeout(() => {
          router.push('/poissons')
        }, 2000)

      } catch (error) {
        console.error('Erreur création poisson:', error)
        
        let errorMsg = 'Erreur lors de la création du poisson'
        if (error.response?.data?.message) {
          errorMsg = error.response.data.message
        } else if (error.response?.data) {
          if (typeof error.response.data === 'object') {
            const errors = Object.values(error.response.data).flat()
            if (errors.length > 0) {
              errorMsg = errors.join(', ')
            }
          }
        } else if (error.message) {
          errorMsg = error.message
        }
        
        errorMessage.value = errorMsg
      } finally {
        isSubmitting.value = false
      }
    }

    const resetForm = () => {
      if (!confirm('Réinitialiser le formulaire ? Toutes les données seront perdues.')) return
      
      form.value = {
        nomPoisson: '',
        idRacePoisson: '',
        prixAchatPoisson: 0,
        prixVentePoisson: 0,
        poidsMaximalPoisson: 0,
        capaciteAugmentationPoisson: 20,
        poidsInitialPoisson: 0,
        dateArriveePoisson: new Date().toISOString().split('T')[0],
        estRassasiePoisson: true,
        estEnViePoisson: true,
        idPiscineActuelle: ''
      }
      
      errors.value = {}
      errorMessage.value = ''
      successMessage.value = ''
    }

    const addNewRace = async () => {
      if (!newRaceName.value.trim()) {
        alert('Le nom de la race est requis')
        return
      }

      try {
        const raceData = {
          nomRacePoisson: newRaceName.value,
          descriptionRacePoisson: newRaceDescription.value,
          estActifRacePoisson: true
        }

        if (poissonService.createRace) {
          const newRace = await poissonService.createRace(raceData)
          races.value.push(newRace)
          form.value.idRacePoisson = newRace.idRacePoisson
        } else {
          const newRace = {
            idRacePoisson: races.value.length + 1,
            ...raceData
          }
          
          races.value.push(newRace)
          form.value.idRacePoisson = newRace.idRacePoisson
        }
        
        newRaceName.value = ''
        newRaceDescription.value = ''
        showAddRaceModal.value = false
        
        successMessage.value = 'Race ajoutée avec succès'
      } catch (error) {
        console.error('Erreur ajout race:', error)
        errorMessage.value = 'Erreur lors de l\'ajout de la race'
      }
    }

    // Initialisation
    onMounted(() => {
      loadInitialData()
    })

    return {
      form,
      races,
      piscinesDisponibles,
      errors,
      isSubmitting,
      loadingPiscines,
      errorMessage,
      successMessage,
      showAddRaceModal,
      newRaceName,
      newRaceDescription,
      
      progressionInitiale,
      poidsRestant,
      beneficePotentiel,
      margePourcentage,
      roiPourcentage,
      estPretAVendre,
      getProfitClass,
      isFormValid,
      
      getRaceName,
      getPiscineNom,
      getPiscineInfo,
      getPiscineOptionLabel,
      getProgressionClass,
      getStatusClass,
      formatCurrency,
      formatDatePreview,
      submitForm,
      resetForm,
      addNewRace,
      loadPiscines
    }
  }
}
</script>

<style scoped>
/* AJOUT: Styles pour la section location */
.preview-location {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #4299e1;
}

.location-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.location-item:last-child {
  margin-bottom: 0;
}

.location-label {
  color: #2c5282;
  font-weight: 600;
}

.location-value {
  color: #2d3748;
  font-weight: 500;
}

.location-value.warning {
  color: #ed8936;
  font-style: italic;
}

/* AJOUT: Styles pour les badges de statut bassin */
.status-assigned {
  background: #c6f6d5;
  color: #22543d;
}

.status-unassigned {
  background: #feebc8;
  color: #744210;
}

/* AJOUT: Style pour le bouton d'actualisation */
.btn-link {
  background: none;
  border: none;
  color: #4299e1;
  cursor: pointer;
  padding: 5px 0;
  text-align: left;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Style pour le select désactivé */
select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #f7fafc;
}

/* Autres styles restent inchangés... */
.nouveau-poisson {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

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
  align-items: center;
  gap: 20px;
}

.btn-back {
  color: #4a5568;
  text-decoration: none;
  font-weight: 600;
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
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

.btn-reset {
  padding: 10px 20px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-reset:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-reset:disabled {
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

.alert.error {
  background: #fed7d7;
  color: #742a2a;
  border-left: 4px solid #f56565;
}

.alert.success {
  background: #c6f6d5;
  color: #22543d;
  border-left: 4px solid #48bb78;
}

.alert-icon {
  font-size: 20px;
}

.alert-close {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: inherit;
}

/* Formulaire */
.form-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.poisson-form {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e2e8f0;
}

.form-section:last-child {
  border-bottom: none;
}

.form-section h2 {
  margin: 0 0 25px 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input.error,
.form-group select.error {
  border-color: #f56565;
}

.error-message {
  color: #f56565;
  font-size: 14px;
  margin-top: 5px;
}

.input-info {
  font-size: 12px;
  color: #718096;
  margin-top: 5px;
}

/* Indicateur de progression */
.progress-indicator {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.progress-bar {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
  margin: 10px 0;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s;
}

.progress-high { background: #48bb78; }
.progress-medium { background: #ed8936; }
.progress-low { background: #ecc94b; }
.progress-very-low { background: #f56565; }

.progress-text {
  font-weight: bold;
  color: #2d3748;
}

/* Prévisualisation bénéfice */
.profit-preview {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.profit-value {
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
}

.profit-positive { color: #48bb78; }
.profit-negative { color: #f56565; }
.profit-neutral { color: #4a5568; }

.profit-details {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #718096;
}

/* Checkboxes */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e0;
  border-radius: 4px;
  position: relative;
  transition: all 0.2s;
}

.checkbox-label input:checked + .checkmark {
  background: #4299e1;
  border-color: #4299e1;
}

.checkbox-label input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 14px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* Résumé */
.summary {
  background: #f7fafc;
  padding: 25px;
  border-radius: 8px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.summary-label {
  font-size: 14px;
  color: #718096;
}

.summary-value {
  font-weight: 600;
  color: #2d3748;
  font-size: 16px;
}

/* Actions du formulaire */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel {
  padding: 12px 24px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  text-decoration: none;
  text-align: center;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-submit {
  padding: 12px 24px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #38a169;
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Aperçu */
.preview-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.preview-card h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
}

.preview-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.preview-name {
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 5px;
}

.preview-race {
  color: #718096;
  font-style: italic;
}

.preview-stats {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stat-label {
  color: #718096;
}

.stat-value {
  font-weight: 600;
  color: #2d3748;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar-small {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill-small {
  height: 100%;
  background: #48bb78;
  transition: width 0.3s;
}

.progress-percent {
  font-weight: 600;
  color: #2d3748;
  min-width: 45px;
}

.preview-financial {
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.financial-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.financial-item:last-child {
  margin-bottom: 0;
}

.financial-label {
  color: #4a5568;
}

.financial-value {
  font-weight: bold;
}

.financial-value.cost { color: #f56565; }
.financial-value.sale { color: #48bb78; }

.preview-status {
  margin-bottom: 20px;
}

.status-badges {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-alive { background: #c6f6d5; color: #22543d; }
.status-dead { background: #fed7d7; color: #742a2a; }
.status-fed { background: #bee3f8; color: #2c5282; }
.status-hungry { background: #feebc8; color: #744210; }
.status-ready { background: #d6bcfa; color: #553c9a; }
.status-growing { background: #c6f6d5; color: #22543d; }

.preview-dates {
  padding-top: 15px;
  border-top: 1px solid #e2e8f0;
}

.date-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-label {
  color: #718096;
  font-size: 14px;
}

.date-value {
  font-weight: 600;
  color: #2d3748;
}

/* Conseils */
.tips-card {
  background: #e6fffa;
  padding: 25px;
  border-radius: 12px;
  border-left: 4px solid #38b2ac;
}

.tips-card h3 {
  margin: 0 0 15px 0;
  color: #2d3748;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #4a5568;
}

.tips-list li {
  margin-bottom: 10px;
  line-height: 1.5;
}

.tips-list li:last-child {
  margin-bottom: 0;
}

/* Modale */
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
  max-width: 500px;
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
}

/* Responsive */
@media (max-width: 1024px) {
  .form-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .summary-grid {
    grid-template-columns: 1fr;
  }
  
  .modal {
    width: 95%;
    margin: 10px;
  }
}
</style>
