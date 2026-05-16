import { defineStore } from 'pinia';
import api from '@/api/axios';

interface User {
  nombre: string;
  correo: string;
  rol: string;
  idDepartamento: string;
  codigoDepartamento: string;
  nombreDepartamento: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
    loading: false,
    error: null as string | null,
  }),
  actions: {
    async login(usuario: string, password: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.post('/login', {
          usuario,
          password,
        });

        if (response.data.status === 'success') {
          this.isAuthenticated = true;
          this.user = { 
            nombre: response.data.nombre,
            correo: response.data.correo,
            rol: response.data.rol,
            idDepartamento: response.data.idDepartamento,
            codigoDepartamento: response.data.codigoDepartamento,
            nombreDepartamento: response.data.nombreDepartamento
          };
          return true;
        } else {
          this.error = response.data.message || 'Error de login';
          return false;
        }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Error de conexión con el servidor';
        return false;
      } finally {
        this.loading = false;
      }
    },
    async register(nombre: string, apellidos: string, email: string, password: string, telefono: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.post('/register', {
          nombre, apellidos, email, password, telefono
        });
        return { 
          success: true, 
          message: response.data.message || 'Registro solicitado con éxito' 
        };
      } catch (err: any) {
        return { 
          success: false, 
          message: err.response?.data?.message || 'Error al solicitar el registro' 
        };
      } finally {
        this.loading = false;
      }
    },
    async checkAuth() {
      try {
        const response = await api.get('/me');
        if (response.data.status === 'success') {
          this.isAuthenticated = true;
          this.user = {
            nombre: response.data.nombre,
            correo: response.data.correo,
            rol: response.data.rol,
            idDepartamento: response.data.idDepartamento,
            codigoDepartamento: response.data.codigoDepartamento,
            nombreDepartamento: response.data.nombreDepartamento
          };
        } else {
          this.isAuthenticated = false;
          this.user = null;
        }
      } catch (err) {
        this.isAuthenticated = false;
        this.user = null;
      }
    },
    async logout() {
      try {
        await api.get('/logout');
      } catch (err) {
        console.error('Logout failed', err);
      } finally {
        this.user = null;
        this.isAuthenticated = false;
      }
    },
  },
});
