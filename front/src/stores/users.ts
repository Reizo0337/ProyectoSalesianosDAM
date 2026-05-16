import { defineStore } from 'pinia';
import api from '@/api/axios';
 
export const useUserStore = defineStore('users', {
  state: () => ({
    users: [] as any[],
    loading: false,
  }),
  actions: {
    async fetchUsers() {
      this.loading = true;
      try {
        const response = await api.get('/usuarios');
        // El backend devuelve el array directamente
        this.users = Array.isArray(response.data) ? response.data : [];
      } catch (error) {
        console.error('Error fetching users:', error);
        this.users = [];
      } finally {
        this.loading = false;
      }
    },
 
    async fetchRoles() {
      try {
        const response = await api.get('/usuarios/roles');
        return response.data || [];
      } catch (error) {
        console.error('Error fetching roles:', error);
        return [];
      }
    },
 
    async fetchDepartments() {
      try {
        const response = await api.get('/usuarios/departamentos');
        return response.data || [];
      } catch (error) {
        console.error('Error fetching departments:', error);
        return [];
      }
    },
 
    async verifyUser(idUsuario: number, idRol: string, idDepartamento: string) {
      try {
        const response = await api.post(`/usuarios/verify/${idUsuario}`, {
          idRol,
          idDepartamento
        });
        return response.data;
      } catch (error) {
        console.error('Error verifying user:', error);
        throw error;
      }
    },
 
    async updateUser(idUsuario: number, data: any) {
      try {
        // El backend usa /usuarios/update/{id} con POST
        const response = await api.post(`/usuarios/update/${idUsuario}`, data);
        return response.data;
      } catch (error) {
        console.error('Error updating user:', error);
        throw error;
      }
    },
 
    async updatePassword(idUsuario: number, newPassword: string) {
      try {
        const response = await api.post(`/usuarios/password/${idUsuario}`, { newPassword });
        return response.data;
      } catch (error) {
        console.error('Error updating password:', error);
        throw error;
      }
    },
 
    async deleteUser(idUsuario: number) {
      try {
        const response = await api.delete(`/usuarios/delete/${idUsuario}`);
        // Si el backend devuelve un objeto con status o mensaje
        this.users = this.users.filter(u => parseInt(u.IdUsuario) !== idUsuario);
        return response.data;
      } catch (error) {
        console.error('Error deleting user:', error);
        throw error;
      }
    }
  }
});
