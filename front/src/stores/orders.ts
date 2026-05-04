import { defineStore } from 'pinia';
import api from '@/api/axios';

interface Order {
    idorden: number;
    numero_orden: string;
    numero_plan: string;
    cantidad: number;
    observaciones: string;
    fechacreacion: string;
    idpresupuesto: number;
    estado: string;
    tipo: string;
    inversion: string;
    nombredepartamento?: string;
    numfacturas?: string;
}

interface Product {
    idproducto: string;
    nombre: string;
    descripcion: string;
}

interface Supplier {
    idproveedor: string;
    nombre: string;
}

export const useOrderStore = defineStore('order', {
    state: () => ({
        orders: [] as Order[],
        products: [] as Product[],
        suppliers: [] as Supplier[],
        orderDetail: null as any,
        loading: false,
    }),
    actions: {
        async getAllOrders() {
            this.loading = true;
            try {
                const response = await api.get('/ordenes/all');
                if (response.data.status === 'success') {
                    this.orders = response.data.orders || [];
                }
            } catch (err) {
                console.error('Error fetching orders:', err);
                this.orders = [];
            } finally {
                this.loading = false;
            }
        },

        async getOrdersByDept(nombreDepartamento: string) {
            this.loading = true;
            try {
                const response = await api.post('/ordenes', { nombreDepartamento });
                if (response.data.status === 'success') {
                    this.orders = response.data.orders || [];
                }
            } catch (err) {
                console.error('Error fetching orders by dept:', err);
                this.orders = [];
            } finally {
                this.loading = false;
            }
        },

        async fetchProducts() {
            try {
                const response = await api.get('/productos');
                if (response.data.status === 'success') {
                    this.products = response.data.productos || [];
                }
            } catch (err) {
                console.error('Error fetching products:', err);
            }
        },

        async fetchSuppliers() {
            try {
                const response = await api.get('/proveedores');
                if (response.data.status === 'success') {
                    this.suppliers = response.data.suppliers || [];
                }
            } catch (err) {
                console.error('Error fetching suppliers:', err);
            }
        },

        async fetchNextSequence(dept: string, year: string) {
            try {
                const response = await api.get(`/ordenes/next-number?dept=${dept}&year=${year}`);
                if (response.data.status === 'success') {
                    return response.data.nextSequence;
                }
            } catch (err) {
                console.error('Error fetching next sequence:', err);
            }
            return '001';
        },

        async createOrder(orderData: any) {
            this.loading = true;
            try {
                const response = await api.post('/ordenes', orderData);
                return response.data;
            } catch (err) {
                console.error('Error creating order:', err);
                throw err;
            } finally {
                this.loading = false;
            }
        },

        async createProduct(productData: { nombre: string; descripcion: string; idProveedor?: string }) {
            try {
                const response = await api.post('/productos', productData);
                return response.data;
            } catch (err) {
                console.error('Error creating product:', err);
                throw err;
            }
        },

        async fetchOrderDetail(id: string | number) {
            this.loading = true;
            try {
                const response = await api.get(`/ordenes/detail?id=${id}`);
                if (response.data.status === 'success') {
                    this.orderDetail = response.data;
                    return response.data;
                }
            } catch (err) {
                console.error('Error fetching order detail:', err);
            } finally {
                this.loading = false;
            }
            return null;
        },

        async uploadInvoice(orderId: number | string, file: File) {
            const formData = new FormData();
            formData.append('id', orderId.toString());
            formData.append('file', file);

            try {
                const response = await api.post('/ordenes/upload-invoice', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                });
                return response.data;
            } catch (err) {
                console.error('Error uploading invoice:', err);
                throw err;
            }
        }
    },
});
