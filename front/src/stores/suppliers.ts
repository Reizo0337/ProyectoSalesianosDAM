import { defineStore } from 'pinia';
import api from '@/api/axios';

export interface Supplier {
  idproveedor: number;
  nombre: string;
  telefono: string;
  direccion: string;
}

export interface Product {
  idproducto: number;
  nombre: string;
  descripcion: string;
}

export const useSupplierStore = defineStore('supplier', {
  state: () => ({
    suppliers: [] as Supplier[],
    currentSupplierProducts: [] as Product[],
    loading: false,
  }),
  actions: {
    async fetchSuppliers() {
      this.loading = true;
      try {
        const response = await api.get('/proveedores');
        if (response.data.status === 'success') {
          this.suppliers = response.data.suppliers || [];
        }
      } catch (err) {
        console.error('Error fetching suppliers:', err);
      } finally {
        this.loading = false;
      }
    },

    async fetchSupplierProducts(id: number) {
      try {
        const response = await api.get(`/proveedores?id=${id}&detail=products`);
        if (response.data.status === 'success') {
          this.currentSupplierProducts = response.data.productos || [];
        }
      } catch (err) {
        console.error('Error fetching supplier products:', err);
      }
    },

    async createSupplier(data: any) {
      try {
        const response = await api.post('/proveedores', data);
        if (response.data.status === 'success') {
          await this.fetchSuppliers();
        }
        return response.data;
      } catch (err) {
        console.error('Error creating supplier:', err);
        throw err;
      }
    },

    async updateSupplier(id: number, data: any) {
      try {
        const response = await api.post(`/proveedores?action=update&id=${id}`, data);
        if (response.data.status === 'success') {
          await this.fetchSuppliers();
        }
        return response.data;
      } catch (err) {
        console.error('Error updating supplier:', err);
        throw err;
      }
    },

    async deleteSupplier(id: number) {
      try {
        const response = await api.post(`/proveedores?action=delete&id=${id}`);
        if (response.data.status === 'success') {
          await this.fetchSuppliers();
        }
        return response.data;
      } catch (err) {
        console.error('Error deleting supplier:', err);
        throw err;
      }
    },

    async assignProduct(supplierId: number, productId: number) {
      try {
        const response = await api.post(`/proveedores?action=assignProduct&id=${supplierId}&productId=${productId}`);
        if (response.data.status === 'success') {
          await this.fetchSupplierProducts(supplierId);
        }
        return response.data;
      } catch (err) {
        console.error('Error assigning product:', err);
        throw err;
      }
    },

    async removeProduct(supplierId: number, productId: number) {
      try {
        const response = await api.post(`/proveedores?action=removeProduct&id=${supplierId}&productId=${productId}`);
        if (response.data.status === 'success') {
          await this.fetchSupplierProducts(supplierId);
        }
        return response.data;
      } catch (err) {
        console.error('Error removing product:', err);
        throw err;
      }
    }
  }
});
