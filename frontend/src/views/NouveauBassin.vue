<template>
  <div class="nouveau-bassin">
    <div class="page-header">
      <div class="header-left">
        <router-link to="/bassins" class="btn-back">
          ← Retour à la liste des bassins
        </router-link>
        <h1>Ajouter un nouveau bassin</h1>
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
      <form @submit.prevent="submitForm" class="bassin-form">
        
        <!-- Section informations de base -->
        <div class="form-section">
          <h2>📝 Informations de base</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="nom">Nom du bassin *</label>
              <input
                type="text"
                id="nom"
                v-model="form.nomPiscine"
                :class="{ 'error': errors.nomPiscine }"
                placeholder="Ex: Bassin Nord, Nurserie..."
                required
              />
              <span v-if="errors.nomPiscine" class="error-message">{{ errors.nomPiscine }}</span>
            </div>

            <div class="form-group">
              <label for="capacite">Capacité maximale *</label>
              <input
                type="number"
                id="capacite"
                v-model="form.capaciteMaxPiscine"
                :class="{ 'error': errors.capaciteMaxPiscine }"
                min="1"
                max="1000"
                required
              />
              <span v-if="errors.capaciteMaxPiscine" class="error-message">{{ errors.capaciteMaxPiscine }}</span>
              <div class="input-info">
                Nombre maximum de poissons que le bassin peut contenir
              </div>
            </div>

            <div class="form-group full-width">
              <label for="description">Description</label>
              <textarea
                id="description"
                v-model="form.descriptionPiscine"
                :class="{ 'error': errors.descriptionPiscine }"
                placeholder="Description du bassin, localisation, caractéristiques..."
                rows="3"
              ></textarea>
              <span v-if="errors.descriptionPiscine" class="error-message">{{ errors.descriptionPiscine }}</span>
            </div>
          </div>
        </div>

        <!-- Section caractéristiques techniques -->
        <div class="form-section">
          <h2>🔧 Caractéristiques techniques</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="volume">Volume (m³)</label>
              <input
                type="number"
                id="volume"
                v-model="form.volumePiscine"
                step="0.01"
                min="0"
                placeholder="Ex: 100.50"
              />
              <div class="input-info">
                Volume total d'eau en mètres cubes
              </div>
            </div>

            <div class="form-group">
              <label for="temperature">Température (°C)</label>
              <input
                type="number"
                id="temperature"
                v-model="form.temperaturePiscine"
                step="0.1"
                min="0"
                max="40"
                placeholder="Ex: 25.5"
              />
              <div class="input-info">
                Température idéale de l'eau
              </div>
            </div>

            <div class="form-group">
              <label for="ph">Niveau de pH</label>
              <input
                type="number"
                id="ph"
                v-model="form.phPiscine"
                step="0.01"
                min="0"
                max="14"
                placeholder="Ex: 7.2"
              />
              <div class="input-info">
                pH optimal (généralement entre 6.5 et 8.5)
              </div>
            </div>
          </div>
        </div>

        <!-- Section statut -->
        <div class="form-section">
          <h2>📊 Statut du bassin</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="form.estActivePiscine"
                  checked
                />
                <span class="checkmark"></span>
                <span>Bassin actif</span>
              </label>
              <div class="input-info">
                Un bassin inactif ne peut pas recevoir de poissons
              </div>
            </div>

            <div class="form-group">
              <div class="status-indicator">
                <label>Statut actuel</label>
                <div class="status-badge" :class="getStatusClass">
                  {{ form.estActivePiscine ? 'Actif' : 'Inactif' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Résumé -->
        <div class="form-section summary">
          <h2>📋 Résumé du bassin</h2>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">Nom</span>
              <span class="summary-value">{{ form.nomPiscine || 'Non défini' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Capacité</span>
              <span class="summary-value">{{ form.capaciteMaxPiscine || 0 }} poissons</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Volume</span>
              <span class="summary-value">{{ form.volumePiscine ? form.volumePiscine + ' m³' : 'Non défini' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Température</span>
              <span class="summary-value">{{ form.temperaturePiscine ? form.temperaturePiscine + '°C' : 'Non définie' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">pH</span>
              <span class="summary-value">{{ form.phPiscine || 'Non défini' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Statut</span>
              <span class="summary-value" :class="getStatusClass">
                {{ form.estActivePiscine ? 'Actif' : 'Inactif' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Actions du formulaire -->
        <div class="form-actions">
          <router-link to="/bassins" class="btn-cancel">
            Annuler
          </router-link>
          <button 
            type="submit" 
            class="btn-submit"
            :disabled="isSubmitting || !isFormValid"
          >
            <span v-if="isSubmitting" class="spinner"></span>
            {{ isSubmitting ? 'Création en cours...' : 'Créer le bassin' }}
          </button>
        </div>
      </form>

      <!-- Aperçu -->
      <div class="preview-container">
        <div class="preview-card">
          <h3>Aperçu du bassin</h3>
          <div class="preview-content">
            <div class="preview-header">
              <div class="preview-name">{{ form.nomPiscine || 'Nouveau bassin' }}</div>
              <div class="preview-status">
                <span class="status-badge" :class="getStatusClass">
                  {{ form.estActivePiscine ? 'Actif' : 'Inactif' }}
                </span>
              </div>
            </div>
            
            <div class="preview-details">
              <div class="detail-item">
                <span class="detail-label">Capacité maximale</span>
                <span class="detail-value">{{ form.capaciteMaxPiscine || 0 }} poissons</span>
              </div>
              
              <div class="detail-item">
                <span class="detail-label">Volume</span>
                <span class="detail-value">{{ form.volumePiscine ? form.volumePiscine + ' m³' : 'Non défini' }}</span>
              </div>
              
              <div v-if="form.temperaturePiscine" class="detail-item">
                <span class="detail-label">Température</span>
                <span class="detail-value">{{ form.temperaturePiscine }}°C</span>
              </div>
              
              <div v-if="form.phPiscine" class="detail-item">
                <span class="detail-label">pH</span>
                <span class="detail-value">{{ form.phPiscine }}</span>
              </div>
              
              <div class="detail-item">
                <span class="detail-label">Occupation</span>
                <div class="occupation-display">
                  <div class="occupation-bar">
                    <div class="occupation-fill" :style="{ width: '0%' }"></div>
                  </div>
                  <span class="occupation-text">0 / {{ form.capaciteMaxPiscine || 0 }} (0%)</span>
                </div>
              </div>
            </div>

            <div v-if="form.descriptionPiscine" class="preview-description">
              <h4>Description</h4>
              <p>{{ form.descriptionPiscine }}</p>
            </div>

            <div class="preview-dates">
              <div class="date-item">
                <span class="date-label">Date de création</span>
                <span class="date-value">{{ getCurrentDate() }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Conseils -->
        <div class="tips-card">
          <h3>💡 Conseils pour créer un bassin</h3>
          <ul class="tips-list">
            <li><strong>Nommage :</strong> Utilisez des noms clairs et descriptifs (ex: "Bassin Nord", "Nurserie", "Bassin Quarantaine")</li>
            <li><strong>Capacité :</strong> Calculez la capacité en fonction de l'espace disponible et de l'espèce de poisson</li>
            <li><strong>Volume :</strong> Important pour calculer les traitements et la filtration</li>
            <li><strong>Température :</strong> Varie selon les espèces (20-30°C pour les tilapias, 10-20°C pour les truites)</li>
            <li><strong>pH :</strong> La plupart des poissons d'élevage préfèrent un pH entre 6.5 et 8.5</li>
            <li><strong>Statut :</strong> Un bassin "inactif" peut être utilisé pour la maintenance ou la quarantaine</li>
          </ul>
        </div>

        <!-- Référence des types de bassins -->
        <div class="reference-card">
          <h3>🏊 Types de bassins recommandés</h3>
          <div class="reference-grid">
            <div class="reference-item">
              <h4>Bassin Principal</h4>
              <p>Capacité : 50-100 poissons</p>
              <p>Utilisation : Production principale</p>
            </div>
            <div class="reference-item">
              <h4>Nurserie</h4>
              <p>Capacité : 20-30 poissons</p>
              <p>Utilisation : Alevins et jeunes poissons</p>
            </div>
            <div class="reference-item">
              <h4>Quarantaine</h4>
              <p>Capacité : 10-15 poissons</p>
              <p>Utilisation : Nouveaux arrivants, traitement</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import bassinService from '../services/bassinService'

export default {
  name: 'NouveauBassin',
  setup() {
    const router = useRouter()
    
    // État du formulaire
    const form = ref({
      nomPiscine: '',
      capaciteMaxPiscine: 20,
      descriptionPiscine: '',
      volumePiscine: null,
      temperaturePiscine: null,
      phPiscine: null,
      estActivePiscine: true
    })

    // État de l'application
    const errors = ref({})
    const isSubmitting = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')

    // Computed properties
    const getStatusClass = computed(() => {
      return form.value.estActivePiscine ? 'status-active' : 'status-inactive'
    })

    const isFormValid = computed(() => {
      return form.value.nomPiscine &&
             form.value.capaciteMaxPiscine > 0
    })

    // Méthodes
    const getCurrentDate = () => {
      return new Date().toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const validateForm = () => {
      errors.value = {}

      // Nom
      if (!form.value.nomPiscine.trim()) {
        errors.value.nomPiscine = 'Le nom du bassin est requis'
      } else if (form.value.nomPiscine.length > 100) {
        errors.value.nomPiscine = 'Le nom ne doit pas dépasser 100 caractères'
      }

      // Capacité
      if (!form.value.capaciteMaxPiscine || form.value.capaciteMaxPiscine <= 0) {
        errors.value.capaciteMaxPiscine = 'La capacité doit être supérieure à 0'
      } else if (form.value.capaciteMaxPiscine > 1000) {
        errors.value.capaciteMaxPiscine = 'La capacité ne peut pas dépasser 1000 poissons'
      }

      // Description
      if (form.value.descriptionPiscine && form.value.descriptionPiscine.length > 1000) {
        errors.value.descriptionPiscine = 'La description ne doit pas dépasser 1000 caractères'
      }

      // Volume
      if (form.value.volumePiscine && form.value.volumePiscine < 0) {
        errors.value.volumePiscine = 'Le volume ne peut pas être négatif'
      }

      // Température
      if (form.value.temperaturePiscine && (form.value.temperaturePiscine < 0 || form.value.temperaturePiscine > 40)) {
        errors.value.temperaturePiscine = 'La température doit être entre 0°C et 40°C'
      }

      // pH
      if (form.value.phPiscine && (form.value.phPiscine < 0 || form.value.phPiscine > 14)) {
        errors.value.phPiscine = 'Le pH doit être entre 0 et 14'
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
        // Préparer les données pour l'API
        const bassinData = {
          nomPiscine: form.value.nomPiscine,
          capaciteMaxPiscine: parseInt(form.value.capaciteMaxPiscine),
          descriptionPiscine: form.value.descriptionPiscine || null,
          volumePiscine: form.value.volumePiscine ? parseFloat(form.value.volumePiscine) : null,
          temperaturePiscine: form.value.temperaturePiscine ? parseFloat(form.value.temperaturePiscine) : null,
          phPiscine: form.value.phPiscine ? parseFloat(form.value.phPiscine) : null,
          estActivePiscine: form.value.estActivePiscine
        }

        // Appeler l'API
        await bassinService.create(bassinData)
        
        successMessage.value = 'Bassin créé avec succès !'
        
        // Rediriger après un délai
        setTimeout(() => {
          router.push('/bassins')
        }, 2000)

      } catch (error) {
        console.error('Erreur création bassin:', error)
        errorMessage.value = error.response?.data?.message || error.message || 'Erreur lors de la création du bassin'
      } finally {
        isSubmitting.value = false
      }
    }

    const resetForm = () => {
      if (!confirm('Réinitialiser le formulaire ? Toutes les données seront perdues.')) return
      
      form.value = {
        nomPiscine: '',
        capaciteMaxPiscine: 20,
        descriptionPiscine: '',
        volumePiscine: null,
        temperaturePiscine: null,
        phPiscine: null,
        estActivePiscine: true
      }
      
      errors.value = {}
      errorMessage.value = ''
      successMessage.value = ''
    }

    return {
      // État
      form,
      errors,
      isSubmitting,
      errorMessage,
      successMessage,
      
      // Computed
      getStatusClass,
      isFormValid,
      
      // Méthodes
      getCurrentDate,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.nouveau-bassin {
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

.bassin-form {
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

.form-group.full-width {
  grid-column: 1 / -1;
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
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
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

/* Checkboxes */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  padding: 10px;
  background: #f7fafc;
  border-radius: 8px;
  transition: background 0.2s;
}

.checkbox-label:hover {
  background: #edf2f7;
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

/* Indicateur de statut */
.status-indicator {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-top: 8px;
}

.status-active {
  background: #c6f6d5;
  color: #22543d;
}

.status-inactive {
  background: #fed7d7;
  color: #742a2a;
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
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #3182ce;
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

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.preview-name {
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

.preview-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f7fafc;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #718096;
  font-size: 14px;
}

.detail-value {
  font-weight: 600;
  color: #2d3748;
}

.occupation-display {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.occupation-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.occupation-fill {
  height: 100%;
  background: #48bb78;
  width: 0%;
  transition: width 0.3s;
}

.occupation-text {
  font-size: 12px;
  color: #718096;
  min-width: 100px;
  text-align: right;
}

.preview-description {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
  margin-top: 10px;
}

.preview-description h4 {
  margin: 0 0 10px 0;
  color: #4a5568;
  font-size: 16px;
}

.preview-description p {
  margin: 0;
  color: #718096;
  line-height: 1.5;
}

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

.tips-list li strong {
  color: #2d3748;
}

.tips-list li:last-child {
  margin-bottom: 0;
}

/* Référence */
.reference-card {
  background: #ebf8ff;
  padding: 25px;
  border-radius: 12px;
  border-left: 4px solid #4299e1;
}

.reference-card h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
}

.reference-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.reference-item {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.reference-item h4 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 16px;
}

.reference-item p {
  margin: 5px 0;
  color: #718096;
  font-size: 14px;
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
  
  .reference-grid {
    grid-template-columns: 1fr;
  }
}
</style>