<template>
  <div class="dashboard">
    <div class="header">
      <h1>Dashboard Pisciculture</h1>
      <div class="date-info">
        <span class="date">{{ currentDate }}</span>
        <span class="time">{{ currentTime }}</span>
      </div>
    </div>

    <!-- Statistiques en temps réel -->
    <div class="stats-container">
      <StatistiquesCard 
        title="Poissons en vie" 
        :value="stats.poissonsEnVie || 0" 
        icon="🐟"
        color="primary"
      />
      <StatistiquesCard 
        title="Poissons affamés" 
        :value="stats.poissonsAffames || 0" 
        icon="🍽️"
        color="warning"
      />
      <StatistiquesCard 
        title="Prêts à vendre" 
        :value="stats.poissonsPretAVendre || 0" 
        icon="💰"
        color="success"
      />
      <StatistiquesCard 
        title="Poids total (kg)" 
        :value="formatPoids(stats.poidsTotal) || 0" 
        icon="⚖️"
        color="info"
        :suffix="' kg'"
      />
      <StatistiquesCard 
        title="Bénéfice potentiel" 
        :value="formatPrice(stats.beneficePotentiel) || 0" 
        icon="💸"
        color="purple"
        :suffix="' MGA'"
        :format="true"
      />
      <StatistiquesCard 
        title="Taux croissance moyen" 
        :value="stats.tauxCroissanceMoyen || 0" 
        icon="📈"
        color="orange"
        :suffix="'%'"
      />
    </div>

    <!-- Alertes urgentes -->
    <div class="alertes-container" v-if="alertes.length > 0">
      <h2>Alertes</h2>
      <div class="alertes-list">
        <div v-for="alerte in alertes" :key="alerte.id" class="alerte" :class="alerte.type">
          <span class="alerte-icon">{{ alerte.icon }}</span>
          <div class="alerte-content">
            <h3>{{ alerte.titre }}</h3>
            <p>{{ alerte.message }}</p>
          </div>
          <button v-if="alerte.action" @click="handleAlerteAction(alerte)" class="btn-action">
            {{ alerte.actionText }}
          </button>
        </div>
      </div>
    </div>

    <!-- Graphique d'évolution -->
    <div class="chart-container">
      <div class="chart-card">
        <h2>Évolution des poids</h2>
        <div class="chart-wrapper">
          <canvas ref="evolutionChartRef"></canvas>
        </div>
        <div class="chart-controls">
          <select v-model="chartPeriod" @change="updateChart">
            <option value="7">7 derniers jours</option>
            <option value="30">30 derniers jours</option>
            <option value="90">3 derniers mois</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="actions-rapides">
      <h2>Actions rapides</h2>
      <div class="actions-grid">
        <router-link to="/nourrissage" class="action-card primary">
          <span class="action-icon">🍽️</span>
          <h3>Nourrir poissons</h3>
          <p>Distribuer de la nourriture</p>
        </router-link>
        
        <router-link to="/poissons/nouveau" class="action-card success">
          <span class="action-icon">🐟</span>
          <h3>Ajouter poisson</h3>
          <p>Nouvel arrivage</p>
        </router-link>
        
        <router-link to="/aliments" class="action-card warning">
          <span class="action-icon">🛒</span>
          <h3>Gérer aliments</h3>
          <p>Stocks et commandes</p>
        </router-link>
        
        <router-link to="/benefices" class="action-card info">
          <span class="action-icon">📊</span>
          <h3>Voir bénéfices</h3>
          <p>Analyse financière</p>
        </router-link>
      </div>
    </div>

    <!-- Poissons à surveiller -->
    <div class="surveillance-container">
      <h2>Poissons à surveiller</h2>
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Race</th>
              <th>Poids actuel</th>
              <th>Poids max</th>
              <th>Progression</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="poisson in poissonsSurveillance" :key="poisson.idPoisson">
              <td>{{ poisson.nomPoisson }}</td>
              <td>{{ poisson.racePoisson?.nomRacePoisson || 'Non défini' }}</td>
              <td>{{ formatPoids(poisson.poidsActuelPoisson) }} g</td>
              <td>{{ formatPoids(poisson.poidsMaximalPoisson) }} g</td>
              <td>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: getProgression(poisson) + '%' }"></div>
                  <span class="progress-text">{{ getProgression(poisson) }}%</span>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(poisson)">
                  {{ getStatusText(poisson) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <router-link :to="'/poissons/' + poisson.idPoisson" class="btn-view">
                    👁️ Voir
                  </router-link>
                  <button v-if="poisson.estPretAVendre" @click="vendrePoisson(poisson.idPoisson)" class="btn-sell">
                    💰 Vendre
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import StatistiquesCard from '../components/StatistiquesCard.vue'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import alimentService from '../services/alimentService' 

Chart.register(...registerables)

export default {
  name: 'Dashboard',
  components: {
    StatistiquesCard
  },
  setup() {
    const router = useRouter()
    const stats = ref({})
    const poissonsSurveillance = ref([])
    const alertes = ref([])
    const currentDate = ref('')
    const currentTime = ref('')
    const chartPeriod = ref('7')
    const evolutionChart = ref(null)
    const evolutionChartRef = ref(null)

    // Mettre à jour la date et l'heure en temps réel
    const updateDateTime = () => {
      const now = new Date()
      currentDate.value = now.toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
      currentTime.value = now.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // Charger les données du dashboard
    const loadDashboardData = async () => {
      try {
        console.log('Chargement des données du dashboard...')
        
        // Charger les statistiques
        const statsData = await poissonService.getStatistiques()
        console.log('Statistiques chargées:', statsData)
        stats.value = statsData

        // Calculer le bénéfice potentiel
        if (statsData.poissonsPretAVendre && statsData.poissonsPretAVendre > 0) {
          const poissonsReady = await poissonService.getPoissonsPretAVendre()
          const beneficePotentiel = poissonsReady.reduce((sum, poisson) => {
            const prixAchat = poisson.prixAchatPoisson || 0
            const prixVente = poisson.prixVentePoisson || 0
            return sum + (prixVente - prixAchat)
          }, 0)
          stats.value.beneficePotentiel = beneficePotentiel
        }

        // Charger les poissons à surveiller
        const poissonsData = await poissonService.getPoissonsEnVie()
        console.log('Poissons chargés:', poissonsData)
        
        // Trier par progression (du plus bas au plus haut pour surveillance)
        poissonsSurveillance.value = poissonsData
          .filter(p => p.estEnViePoisson && !p.estVenduPoisson)
          .sort((a, b) => {
            const progA = getProgression(a)
            const progB = getProgression(b)
            return progA - progB
          })
          .slice(0, 10)

        // Générer les alertes
        generateAlertes(statsData)

        // Mettre à jour le graphique
        await nextTick()
        updateChart()
      } catch (error) {
        console.error('Erreur chargement dashboard:', error)
        alert('Impossible de charger les données. Vérifiez que le serveur est démarré.')
      }
    }

    // Formater le prix
    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }

    // Générer les alertes dynamiques
    const generateAlertes = (statsData) => {
      alertes.value = []

      // Alerte si stock bas
      if (statsData.stockAliment && statsData.stockAliment < 20) {
        alertes.value.push({
          id: 1,
          type: 'warning',
          icon: '⚠️',
          titre: 'Stock aliment bas',
          message: `Il reste seulement ${statsData.stockAliment}kg de nourriture`,
          action: 'reapprovisionner',
          actionText: 'Réapprovisionner'
        })
      }

      // Alerte si poissons prêts à vendre
      if (statsData.poissonsPretAVendre > 0) {
        alertes.value.push({
          id: 2,
          type: 'success',
          icon: '💰',
          titre: 'Poissons prêts à vendre',
          message: `${statsData.poissonsPretAVendre} poissons atteignent 95% de leur poids maximum`,
          action: 'vendre',
          actionText: 'Voir'
        })
      }

      // Alerte si nombreux poissons affamés
      if (statsData.poissonsAffames > 10) {
        alertes.value.push({
          id: 3,
          type: 'info',
          icon: '🍽️',
          titre: 'Nourrissage nécessaire',
          message: `${statsData.poissonsAffames} poissons sont affamés`,
          action: 'nourrir',
          actionText: 'Nourrir'
        })
      }
    }

    // Mettre à jour le graphique d'évolution
    const updateChart = async () => {
      try {
        // Détruire le graphique existant
        if (evolutionChart.value) {
          evolutionChart.value.destroy()
        }

        // Vérifier que la référence existe
        if (!evolutionChartRef.value) {
          console.error('Canvas reference not found')
          return
        }

        const ctx = evolutionChartRef.value.getContext('2d')
        if (!ctx) return

        // Récupérer les données historiques
        const historique = await getHistoriqueData()
        
        if (!historique || !historique.labels || historique.labels.length === 0) {
          console.warn('Aucune donnée pour le graphique')
          return
        }

        // Créer le graphique
        evolutionChart.value = new Chart(ctx, {
          type: 'line',
          data: {
            labels: historique.labels,
            datasets: [
              {
                label: 'Poids moyen (g)',
                data: historique.poids,
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointHoverRadius: 6
              },
              {
                label: 'Gain journalier (g)',
                data: historique.gains,
                borderColor: '#48bb78',
                backgroundColor: 'rgba(72, 187, 120, 0.1)',
                borderWidth: 2,
                fill: false,
                tension: 0.4,
                pointRadius: 4,
                pointHoverRadius: 6
              }
            ]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'top',
                labels: {
                  usePointStyle: true,
                  padding: 20
                }
              },
              tooltip: {
                mode: 'index',
                intersect: false,
                backgroundColor: 'rgba(0, 0, 0, 0.7)',
                padding: 10,
                cornerRadius: 8
              }
            },
            scales: {
              y: {
                beginAtZero: true,
                title: {
                  display: true,
                  text: 'Grammes',
                  color: '#4a5568',
                  font: {
                    weight: 'bold'
                  }
                },
                grid: {
                  color: 'rgba(226, 232, 240, 0.5)'
                }
              },
              x: {
                title: {
                  display: true,
                  text: 'Date',
                  color: '#4a5568',
                  font: {
                    weight: 'bold'
                  }
                },
                grid: {
                  display: false
                }
              }
            },
            interaction: {
              intersect: false,
              mode: 'nearest'
            }
          }
        })
        
        console.log('Graphique créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique:', error)
      }
    }

    // Simuler des données d'historique (à remplacer par l'API réelle)
    const getHistoriqueData = async () => {
      try {
        const jours = parseInt(chartPeriod.value)
        const labels = []
        const poids = []
        const gains = []

        // Pour l'exemple, générer des données aléatoires
        // Dans un cas réel, appeler l'API pour obtenir les données historiques
        for (let i = jours - 1; i >= 0; i--) {
          const date = new Date()
          date.setDate(date.getDate() - i)
          labels.push(date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' }))
          
          // Simuler des données de croissance
          const basePoids = 500 + Math.random() * 200
          poids.push(Math.round(basePoids + i * 15))
          gains.push(Math.round(10 + Math.random() * 20))
        }

        return { labels, poids, gains }
      } catch (error) {
        console.error('Erreur récupération données historiques:', error)
        return { labels: [], poids: [], gains: [] }
      }
    }

    // Formater le poids
    const formatPoids = (poids) => {
      if (!poids && poids !== 0) return '0'
      return parseFloat(poids).toFixed(2)
    }

    // Calculer la progression
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      const progression = (poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100
      return Math.min(Math.round(progression), 100)
    }

    // Déterminer la classe de statut
    const getStatusClass = (poisson) => {
      const progression = getProgression(poisson)
      if (progression >= 95) return 'status-ready'
      if (progression >= 70) return 'status-good'
      if (poisson.estRassasiePoisson) return 'status-fed'
      return 'status-hungry'
    }

    // Déterminer le texte de statut
    const getStatusText = (poisson) => {
      const progression = getProgression(poisson)
      if (progression >= 95) return 'Prêt à vendre'
      if (progression >= 70) return 'Croissance bonne'
      if (poisson.estRassasiePoisson) return 'Rassasié'
      return 'Affamé'
    }

    // Vendre un poisson
    const vendrePoisson = async (id) => {
      if (confirm('Vendre ce poisson ?')) {
        try {
          await poissonService.vendrePoisson(id)
          await loadDashboardData()
          alert('Poisson vendu avec succès !')
        } catch (error) {
          console.error('Erreur vente poisson:', error)
          alert('Erreur lors de la vente')
        }
      }
    }

    // Gérer les actions d'alerte
    const handleAlerteAction = (alerte) => {
      switch (alerte.action) {
        case 'reapprovisionner':
          router.push('/aliments')
          break
        case 'vendre':
          router.push('/poissons?filter=ready')
          break
        case 'nourrir':
          router.push('/nourrissage')
          break
      }
    }

    // Initialisation
    onMounted(() => {
      updateDateTime()
      loadDashboardData()
      
      // Mettre à jour l'heure toutes les minutes
      const timeInterval = setInterval(updateDateTime, 60000)
      
      // Recharger les données toutes les 5 minutes
      const dataInterval = setInterval(loadDashboardData, 300000)

      // Nettoyer les intervalles
      onUnmounted(() => {
        clearInterval(timeInterval)
        clearInterval(dataInterval)
        if (evolutionChart.value) {
          evolutionChart.value.destroy()
        }
      })
    })

    return {
      stats,
      poissonsSurveillance,
      alertes,
      currentDate,
      currentTime,
      chartPeriod,
      evolutionChartRef,
      formatPoids,
      formatPrice,
      getProgression,
      getStatusClass,
      getStatusText,
      vendrePoisson,
      handleAlerteAction,
      updateChart
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/dashboard.css';

/* Styles supplémentaires pour le canvas */
.chart-wrapper {
  height: 300px;
  position: relative;
}

.chart-wrapper canvas {
  width: 100% !important;
  height: 100% !important;
}

/* Styles pour les statuts */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-ready {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.status-good {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: white;
}

.status-fed {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
}

.status-hungry {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  color: white;
}

/* Barre de progression */
.progress-bar {
  position: relative;
  height: 20px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.progress-text {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.8rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}
</style>

