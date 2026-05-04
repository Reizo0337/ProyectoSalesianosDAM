import { defineStore } from 'pinia';
import api from '@/api/axios';

interface Presupuesto {
    cantidad: number;
    gasto: number;
    nombrePresupuesto: string;
    idPresupuesto: number;
    nombreDepartamento: string;
    Codigo: string;
    type: string;
}

export const usePresupuestoStore = defineStore('presupuesto', {
    state: () => ({
        presupuestos: [] as Presupuesto[],
    }),
    actions: {
        async getAllPresupuestos() {
            try {
                const response = await api.get('/presupuestos/all');
                console.log('All Presupuestos Response:', response.data);
                if (response.data.status === 'success') {
                    this.presupuestos = response.data.presupuestos;
                }
            } catch (err) {
                console.error('Error al obtener todos los presupuestos:', err);
            }
        },
        async getPresupuestosByDept(nombreDepartamento: string) {
            try {
                const response = await api.post('/presupuestos', { nombreDepartamento });
                console.log('Presupuestos by Dept Response:', response.data);
                if (response.data.status === 'success') {
                    this.presupuestos = response.data.presupuestos;
                }
            } catch (err) {
                console.error(`Error al obtener presupuestos del departamento ${nombreDepartamento}:`, err);
            }
        },
    },
});