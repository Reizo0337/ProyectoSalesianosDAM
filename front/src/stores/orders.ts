import { defineStore } from 'pinia';
import api from '@/api/axios';

interface Order {
    idorden: number;
    numero_orden: string;
    numero_plan: string;
    cantidad: number;
    descripcion: string;
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
        async getAllOrders(year?: number) {
            this.loading = true;
            try {
                const url = year ? `/ordenes/all?year=${year}` : '/ordenes/all';
                const response = await api.get(url);
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

        async getOrdersByDept(nombreDepartamento: string, year?: number) {
            this.loading = true;
            try {
                const response = await api.post('/ordenes', { nombreDepartamento, year });
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

        async fetchYears() {
            try {
                const response = await api.get('/ordenes/years');
                return response.data.years || [];
            } catch (err) {
                console.error('Error fetching years:', err);
                return [];
            }
        },

        async updateOrderStatus(id: number | string, estado: string) {
            try {
                const response = await api.post('/ordenes/update-status', { id, estado });
                return response.data;
            } catch (err) {
                console.error('Error updating status:', err);
                throw err;
            }
        },

        async updateDescription(id: number | string, descripcion: string) {
            try {
                const response = await api.post('/ordenes/update-observations', { id, descripcion });
                return response.data;
            } catch (err) {
                console.error('Error updating description:', err);
                throw err;
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

        async updateOrder(id: number | string, orderData: any) {
            this.loading = true;
            try {
                const response = await api.post('/ordenes/update', { id, ...orderData });
                return response.data;
            } catch (err) {
                console.error('Error updating order:', err);
                throw err;
            } finally {
                this.loading = false;
            }
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
                if (response.data.status === 'success') {
                    await this.fetchProducts();
                }
                return response.data;
            } catch (err) {
                console.error('Error creating product:', err);
                throw err;
            }
        },

        async updateProduct(id: number | string, productData: any) {
            try {
                const response = await api.post(`/productos?action=update&id=${id}`, productData);
                if (response.data.status === 'success') {
                    await this.fetchProducts();
                }
                return response.data;
            } catch (err) {
                console.error('Error updating product:', err);
                throw err;
            }
        },

        async deleteProduct(id: number | string) {
            try {
                const response = await api.post(`/productos?action=delete&id=${id}`);
                if (response.data.status === 'success') {
                    await this.fetchProducts();
                }
                return response.data;
            } catch (err) {
                console.error('Error deleting product:', err);
                throw err;
            }
        },

        async deleteSupplier(id: number | string) {
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
        
        async deleteOrder(id: number | string) {
            try {
                const response = await api.post('/ordenes/delete', { id });
                return response.data;
            } catch (err) {
                console.error('Error deleting order:', err);
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
        },

        async fetchComments(orderId: string | number) {
            try {
                const response = await api.get(`/comentarios?idOrden=${orderId}`);
                return response.data.comentarios || [];
            } catch (err) {
                console.error('Error fetching comments:', err);
                return [];
            }
        },

        async addComment(orderId: string | number, comentario: string) {
            try {
                const response = await api.post('/comentarios', { idOrden: orderId.toString(), comentario });
                return response.data;
            } catch (err) {
                console.error('Error adding comment:', err);
                throw err;
            }
        },

        async fetchNotifications() {
            try {
                const response = await api.get('/notificaciones');
                return response.data.notificaciones || [];
            } catch (err) {
                console.error('Error fetching notifications:', err);
                return [];
            }
        },

        async markNotificationAsRead(idNotificacion: string | number) {
            try {
                const response = await api.post('/notificaciones/read', { idNotificacion: idNotificacion.toString() });
                return response.data;
            } catch (err) {
                console.error('Error marking notification as read:', err);
                throw err;
            }
        }
    },
});
