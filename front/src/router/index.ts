import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { hideLayout: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { hideLayout: true },
    },
    {
      path: '/presupuestos',
      name: 'presupuestos',
      component: () => import('../views/Presupuestos.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ordenes',
      name: 'ordenes',
      component: () => import('../views/Ordenes.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ordenes/:id',
      name: 'ordenDetalle',
      component: () => import('../views/OrderDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/historico',
      name: 'historico',
      component: () => import('../views/Historico.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/config',
      name: 'config',
      component: () => import('../views/Config.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ayuda',
      name: 'ayuda',
      component: () => import('../views/Ayuda.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/proveedores',
      name: 'proveedores',
      component: () => import('../views/Proveedores.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/proveedores/:id',
      name: 'proveedorDetalle',
      component: () => import('../views/ProveedorDetail.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  
  // If we haven't checked authentication yet, do it now
  if (!authStore.isAuthenticated && authStore.user === null) {
     await authStore.checkAuth()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' }
  }

  if ((to.name === 'login' || to.name === 'register') && authStore.isAuthenticated) {
    return { name: 'home' }
  }
})

export default router
