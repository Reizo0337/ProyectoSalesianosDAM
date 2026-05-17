# ZarGestion Frontend 🪙 - Vue 3 Single Page Application (SPA)

Bienvenido al submódulo del **Frontend de ZarGestion**. Esta SPA moderna, interactiva y reactiva está construida sobre **Vue 3 (Composition API)**, **TypeScript**, **Pinia** (gestión de estado global) y compilada mediante la velocidad instantánea de **Vite**.

---

## 🏛️ Estructura del Código del Proyecto

La carpeta principal `/front/src` organiza el código bajo principios de modularidad y escalabilidad:

```
front/src/
├── assets/
│   └── main.css                    # Estilos CSS globales, variables CSS de ZarGestion y temas
├── components/
│   ├── orders/
│   │   ├── OrderInvoices.vue       # Listado de facturas, subida multipart y visor interactivo
│   │   ├── OrderModal.vue          # Modal interactivo paso a paso para la creación de órdenes
│   │   ├── OrderTable.vue          # Tabla optimizada de órdenes con filtros de estados
│   │   └── PdfPreview.vue          # Visor integrado y reactivo de PDF mediante canvas (PDF.js)
│   └── common/
│       └── Navbar.vue              # Barra de navegación superior con perfiles y cambio de rol
├── stores/
│   ├── auth.ts                     # Estado global del usuario logueado, rol y departamento
│   ├── presupuesto.ts              # Gestión reactiva de presupuestos, creación y validaciones
│   └── orders.ts                   # Gestión reactiva de compras, proveedores, productos y facturas
├── views/
│   ├── Login.vue                   # Vista de acceso segura con animaciones fluidas
│   ├── Ordenes.vue                 # Panel de control de órdenes de compra departamentales
│   ├── Presupuestos.vue            # Gestión de presupuestos con previsualizador interactivo
│   └── OrderDetail.vue             # Panel de detalle pormenorizado y auditoría de la orden
├── router/
│   └── index.ts                    # Enrutador de Vue Router con protección de rutas por roles (guards)
├── App.vue                         # Componente raíz del proyecto
└── main.ts                         # Punto de entrada de la aplicación e inicialización de plugins
```

---

## 💎 Características e Implementaciones Premium

### 1. Generador de Códigos y Tarjeta Reactiva en Vivo (`Presupuestos.vue`)
Para evitar que los usuarios introduzcan códigos de presupuesto incorrectos y garantizar las directrices corporativas, se ha eliminado por completo la introducción manual de códigos y nombres de presupuestos:
*   **Tarjeta de Previsualización Dinámica**: A medida que el usuario cambia el *Departamento*, *Año* o *Tipo de Presupuesto* en el formulario de creación, una tarjeta interactiva renderiza en tiempo real el formato final del código (`PRES-[DEP]-[AÑO]` o `PLAN-[DEP]-[AÑO]`) y del nombre.
*   **Protección en Edición**: Durante la edición de un presupuesto, el formulario bloquea de forma inteligente los campos críticos (tipo y departamento) para mantener la consistencia referencial de la base de datos, mostrando la información oficial en la tarjeta interactiva.
*   **Gestión de Respuestas y Alertas**: Integrado con `vue-toastification`, el frontend procesa y muestra de forma clara y vistosa los errores controlados devueltos por el backend (por ejemplo, si ya existe un presupuesto para ese departamento en el año fiscal en curso).

### 2. Visor Inteligente de PDFs en Canvas (`PdfPreview.vue`)
Para prescindir de visores externos del navegador que rompen la estética y la experiencia SPA, se ha desarrollado un visor a medida usando **`PDF.js`**:
*   **Renderizado de Precisión**: Lee la respuesta binaria del backend y dibuja las páginas del PDF directamente en un elemento `<canvas>` HTML5.
*   **Barra de Herramientas Interactiva**:
    *   *Paginación*: Botones de avance y retroceso con detección dinámica de páginas (`Página X de Y`).
    *   *Zoom Reactivo*: Controles para aumentar o disminuir el zoom con cálculo porcentual del tamaño de visualización.
    *   *Indicador de Carga*: Un spinner animado de gran fluidez visual informa al usuario mientras se recuperan y procesan los bytes del servidor.

### 3. Scroll Inteligente y Seguridad CORS (`OrderInvoices.vue`)
*   **Bloqueo de Scroll de Fondo**: Al abrir el modal del previsualizador de facturas PDF, un `watch` sobre `previewUrl` inyecta automáticamente `overflow: hidden` en el `body` de la ventana. Esto congela la página de abajo de forma que el scroll de la rueda del ratón se realiza única y exclusivamente de forma interna dentro del PDF, evitando desplazamientos de fondo molestos. Al cerrar el visor, el scroll se reactiva al instante.
*   **Descargas Seguras mediante Credenciales CORS**: Las peticiones de descarga de facturas incorporan la directiva `{ credentials: 'include' }` en el `fetch` de JavaScript. Esto asegura que la cookie de sesión del usuario sea compartida entre el puerto del front (`5173`) y el del back (`8080`), evitando errores de autenticación (`401 Unauthorized`) y descargas corruptas.

---

## 🔌 Configuración e Instalación del Frontend

### **Requisitos Previos**
Asegúrate de tener instalado [Node.js](https://nodejs.org/) (versión 18 o superior).

### **Instalación de Dependencias**
```sh
npm install
```

### **Comandos de Ejecución Disponibles**
*   **Desarrollo local (con recarga rápida HMR)**:
    ```sh
    npm run dev
    ```
*   **Compilación y minificación para Producción**:
    ```sh
    npm run build
    ```
*   **Auditoría y formateo de código (Linter)**:
    ```sh
    npm run lint
    ```

---
*Frontend premium diseñado para maximizar la usabilidad corporativa.*
