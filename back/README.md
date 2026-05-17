# ZarGestion Backend ☕ - Java Jakarta API REST

Bienvenido al submódulo del **Servidor de ZarGestion**. Este componente gestiona la lógica de negocio, las reglas financieras de presupuestos, el procesamiento binario de facturas y la persistencia de datos en MySQL mediante una arquitectura desacoplada y de muy bajo consumo de recursos (sin frameworks pesados como Spring Boot, utilizando Servlets puros y JDBC nativo).

---

## 🏛️ Estructura del Código Fuente

El código se organiza en una arquitectura limpia multicapa (**Controller-Service-Repository**), garantizando una separación clara de responsabilidades:

```
back/src/main/java/com/salesianos/
├── servlets/
│   └── ApiServlet.java             # Router y controlador frontal HTTP (Front Controller)
├── controllers/
│   ├── OrderController.java        # Controlador de órdenes de compra, comentarios y facturas
│   ├── PresupuestoController.java  # Controlador de planes de inversión y presupuestos ordinarios
│   └── UserController.java         # Controlador de autenticación y roles de usuario
├── services/
│   ├── OrderService.java           # Servicio de negocio para compras y validación de saldo
│   ├── BudgetService.java          # Servicio de negocio para presupuestos y autogeneración de códigos
│   └── UserService.java            # Servicio de autenticación y lógica de perfiles
├── repositories/
│   ├── OrderRepository.java        # Acceso a datos JDBC para órdenes y facturas (BLOBs)
│   ├── BudgetRepository.java       # Acceso a datos JDBC para presupuestos (unicidad y cálculos)
│   ├── NotificationRepository.java # Acceso a datos JDBC para alertas y notificaciones en vivo
│   └── UserRepository.java         # Acceso a datos JDBC para credenciales y asignaciones
├── models/
│   ├── Order.java                  # Modelo de entidad Orden de Compra
│   ├── Budget.java                 # Modelo de entidad Presupuesto / Plan
│   └── User.java                   # Modelo de entidad Usuario y Rol
└── utils/
    ├── DatabaseManager.java        # Gestión del Pool de Conexiones JDBC (Thread-safe)
    └── JsonUtil.java               # Utilidad de serialización/deserialización con Jackson
```

---

## 🌐 Flujo de Enrutamiento Dinámico

El backend utiliza un único Servlet central (**`ApiServlet.java`**) como controlador frontal para recibir todas las peticiones HTTP concurrentes. El flujo de enrutamiento funciona de la siguiente manera:

1. **Interceptación**: `ApiServlet` intercepta las rutas asociadas (`/api/*`).
2. **Filtro CORS**: El `CorsFilter` intercepta previamente la petición para añadir cabeceras que habilitan credenciales (`Access-Control-Allow-Credentials: true`) y admiten peticiones de puertos cruzados.
3. **Delegación**: `ApiServlet` analiza la ruta y delega el flujo al controlador especializado:
   * Peticiones a `/ordenes` o `/facturas` -> `OrderController`
   * Peticiones a `/presupuestos` -> `PresupuestoController`
   * Peticiones a `/auth` -> `UserController`
4. **Respuesta**: El controlador procesa la petición y devuelve un string JSON que el Servlet escribe en el cliente. Si es una petición de archivo binario (como un PDF), el controlador escribe directamente al Stream del cliente y retorna `null`, interrumpiendo limpiamente la generación de JSON para enviar el fichero intacto.

---

## 🏗️ Implementaciones Técnicas Destacadas

### 1. Sistema de Control de Presupuestos y Unicidad Estricta
El servicio de negocio en [BudgetService.java](file:///z:/ProyectoDAM/back/src/main/java/com/salesianos/services/BudgetService.java) y [BudgetRepository.java](file:///z:/ProyectoDAM/back/src/main/java/com/salesianos/repositories/BudgetRepository.java) implementa reglas estrictas:
*   **Autogeneración**: En la creación o actualización de un presupuesto, el código y el nombre se generan automáticamente en base al departamento, año e indicador de tipo de presupuesto.
*   **Consulta de Unicidad Avanzada**: Mediante el query `existsByTypeAndDept(type, idDepartamento, anio, excludeId)`, el backend valida que no existan duplicados antes de insertar o editar:
    ```sql
    SELECT COUNT(*) FROM presupuesto 
    WHERE tipo = ? AND idDepartamento = ? AND anio = ? AND idPresupuesto != ?
    ```
*   **Restricción Financiera**: Si la validación falla, se aborta la inserción y se envía un código de error detallado HTTP 400.

### 2. Entrega y Almacenamiento Seguro de Facturas en PDF (BLOB)
*   **Almacenamiento**: Las facturas se guardan directamente como binarios en base de datos bajo la columna `blobFactura` (de tipo `BLOB`) mediante un flujo seguro de entrada binaria (`InputStream`).
*   **Streaming en Vivo (`/facturas/view`)**: En lugar de guardar archivos en el sistema de ficheros del servidor (que arriesgaría la seguridad y consistencia), el endpoint del controlador lee los bytes directamente de MySQL y los inyecta en el flujo de salida HTTP:
    ```java
    response.setContentType("application/pdf");
    response.setContentLength(pdfBytes.length);
    if ("download".equalsIgnoreCase(action)) {
        response.setHeader("Content-Disposition", "attachment; filename=\"factura_" + id + ".pdf\"");
    } else {
        response.setHeader("Content-Disposition", "inline; filename=\"factura_" + id + ".pdf\"");
    }
    response.getOutputStream().write(pdfBytes);
    ```
*   Esto habilita tanto la visualización interactiva sin recarga en el navegador como la descarga forzada del archivo PDF original.

### 3. Gestión Eficiente de Conexiones JDBC
En lugar de crear y destruir conexiones en cada petición (lo cual degradaría severamente el rendimiento y colapsaría el servidor bajo carga concurrente), [DatabaseManager.java](file:///z:/ProyectoDAM/back/src/main/java/com/salesianos/utils/DatabaseManager.java) implementa un **Pool de Conexiones** dinámico con un proxy que recicla las conexiones válidas al cerrarlas, manteniendo siempre activas hasta 10 conexiones en memoria listas para su uso inmediato.

---

## 🗄️ Modelo Relacional de Datos

La base de datos consta de las siguientes tablas centrales optimizadas con claves foráneas, índices de búsqueda y cascada:
*   **`departamento`**: Almacena los códigos de departamento (ej. `INF`, `DIR`) y nombres.
*   **`usuario`**: Registro de personal con contraseñas encriptadas y asignación de rol/departamento.
*   **`presupuesto`**: Gestión del dinero asignado, dinero gastado y disponible por año y tipo.
*   **`ordencompra`**: Órdenes creadas, importes totales, estados y referencias cruzadas.
*   **`productos`** y **`proveedores`**: Catálogo de productos e inventario disponible para compras rápidas.
*   **`orden_productos`**: Tabla intermedia que asocia las líneas de productos a cada orden de compra.
*   **`facturas`**: Almacenamiento binario (`BLOB`) de los PDFs adjuntos por factura.
*   **`notificaciones`**: Alertas del sistema sobre cambios de estado y aprobaciones de compras.

---
*Backend optimizado para un tiempo de respuesta inferior a 15ms.*
