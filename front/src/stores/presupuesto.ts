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
        availableYears: [] as number[],
    }),
    actions: {
        async getAllPresupuestos(anio?: number) {
            try {
                // He modificado el backend para que acepte anio en el body
                const response = await api.post('/presupuestos/all', { anio });
                if (response.data.status === 'success') {
                    this.presupuestos = response.data.presupuestos;
                }
            } catch (err) {
                console.error('Error al obtener todos los presupuestos:', err);
            }
        },
        async getPresupuestosByDept(nombreDepartamento: string, anio?: number) {
            try {
                const response = await api.post('/presupuestos', { nombreDepartamento, anio });
                if (response.data.status === 'success') {
                    this.presupuestos = response.data.presupuestos;
                }
            } catch (err) {
                console.error(`Error al obtener presupuestos del departamento ${nombreDepartamento}:`, err);
            }
        },
        async getYears() {
            try {
                const response = await api.get('/presupuestos/years');
                if (response.data.status === 'success') {
                    this.availableYears = response.data.years;
                    return response.data.years;
                }
            } catch (err) {
                console.error('Error al obtener años:', err);
            }
            return [];
        },
        async cloneBudgets(fromYear: number, toYear: number) {
            try {
                const response = await api.post('/presupuestos/clone', { fromYear, toYear });
                return response.data.status === 'success';
            } catch (err) {
                console.error('Error al clonar presupuestos:', err);
                return false;
            }
        }
    },
});