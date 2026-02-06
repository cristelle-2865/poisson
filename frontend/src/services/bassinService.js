// services/bassinService.js
import api from './api';

const bassinService = {
    async getAll() {
        try {
            const response = await api.get('/piscines');
            return response.data;
        } catch (error) {
            console.error('Erreur récupération bassins:', error);
            throw error;
        }
    },

    async getById(id) {
        try {
            const response = await api.get(`/piscines/${id}`);
            return response.data;
        } catch (error) {
            console.error('Erreur récupération bassin:', error);
            throw error;
        }
    },

    async getStatistics(id) {
        try {
            const response = await api.get(`/piscines/${id}/statistiques`);
            return response.data;
        } catch (error) {
            console.error('Erreur statistiques bassin:', error);
            throw error;
        }
    },

    async getPoissons(id) {
        try {
            const response = await api.get(`/piscines/${id}/poissons`);
            return response.data;
        } catch (error) {
            console.error('Erreur poissons bassin:', error);
            throw error;
        }
    },

    async getHistorique(id) {
        try {
            const response = await api.get(`/piscines/${id}/historique`);
            return response.data;
        } catch (error) {
            console.error('Erreur historique bassin:', error);
            throw error;
        }
    },

    async create(bassin) {
        try {
            const response = await api.post('/piscines', bassin);
            return response.data;
        } catch (error) {
            console.error('Erreur création bassin:', error);
            throw error;
        }
    },

    async update(id, bassin) {
        try {
            const response = await api.put(`/piscines/${id}`, bassin);
            return response.data;
        } catch (error) {
            console.error('Erreur mise à jour bassin:', error);
            throw error;
        }
    },

    async delete(id) {
        try {
            await api.delete(`/piscines/${id}`);
        } catch (error) {
            console.error('Erreur suppression bassin:', error);
            throw error;
        }
    },

    async toggleStatus(id) {
        try {
            const response = await api.patch(`/piscines/${id}/toggle`);
            return response.data;
        } catch (error) {
            console.error('Erreur changement statut bassin:', error);
            throw error;
        }
    },

    async affecterPoisson(idBassin, idPoisson) {
        try {
            const response = await api.post(`/piscines/${idBassin}/affecter/${idPoisson}`);
            return response.data;
        } catch (error) {
            console.error('Erreur affectation poisson:', error);
            throw error;
        }
    },

    async retirerPoisson(idPoisson, raison) {
        try {
            const response = await api.post(`/piscines/retirer/${idPoisson}`, null, {
                params: { raison }
            });
            return response.data;
        } catch (error) {
            console.error('Erreur retrait poisson:', error);
            throw error;
        }
    },

    async viderBassin(idBassin, raison) {
        try {
            const response = await api.post(`/piscines/${idBassin}/vider`, null, {
                params: { raison }
            });
            return response.data;
        } catch (error) {
            console.error('Erreur vidage bassin:', error);
            throw error;
        }
    },

    async getBassinsDisponibles() {
        try {
            const response = await api.get('/piscines/disponibles');
            return response.data;
        } catch (error) {
            console.error('Erreur bassins disponibles:', error);
            throw error;
        }
    },

    async getHistoriquePoisson(idPoisson) {
        try {
            const response = await api.get(`/piscines/poisson/${idPoisson}/historique`);
            return response.data;
        } catch (error) {
            console.error('Erreur historique poisson:', error);
            throw error;
        }
    }
};

export default bassinService;


