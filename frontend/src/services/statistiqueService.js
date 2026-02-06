// services/statistiqueService.js
import api from './api';

const statistiqueService = {
    async getStatistiquesCompletes() {
        try {
            const response = await api.get('/statistiques/completes');
            return response.data;
        } catch (error) {
            console.error('Erreur statistiques complètes:', error);
            throw error;
        }
    },

    async getStatistiquesMensuelles(mois, annee) {
        try {
            const response = await api.get(`/statistiques/mensuelles/${annee}/${mois}`);
            return response.data;
        } catch (error) {
            console.error('Erreur statistiques mensuelles:', error);
            throw error;
        }
    },

    async getRapportPerformance() {
        try {
            const response = await api.get('/statistiques/performance');
            return response.data;
        } catch (error) {
            console.error('Erreur rapport performance:', error);
            throw error;
        }
    }
};

export default statistiqueService;

