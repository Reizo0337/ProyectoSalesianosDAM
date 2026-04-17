/*
 Navicat Premium Dump SQL

 Source Server         : reizo
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : gestion_compras

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 15/04/2026 17:46:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for departamento
-- ----------------------------
DROP TABLE IF EXISTS `departamento`;
CREATE TABLE `departamento`  (
  `idDepartamento` int NOT NULL AUTO_INCREMENT,
  `Codigo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`idDepartamento`) USING BTREE,
  UNIQUE INDEX `Codigo`(`Codigo` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of departamento
-- ----------------------------
INSERT INTO `departamento` VALUES (1, 'INFO', 'Informatica');
INSERT INTO `departamento` VALUES (2, 'MECA', 'Mecanica');
INSERT INTO `departamento` VALUES (3, 'ELTA', 'Electricidad');
INSERT INTO `departamento` VALUES (4, 'AUTO', 'Automocion');
INSERT INTO `departamento` VALUES (5, 'FPGB', 'Grado Basico');
INSERT INTO `departamento` VALUES (6, 'TELE', 'Telecomunicaciones');
INSERT INTO `departamento` VALUES (7, 'ROBO', 'Robotica');
INSERT INTO `departamento` VALUES (8, 'PRI', 'Primaria');
INSERT INTO `departamento` VALUES (9, 'INF', 'Infantil');
INSERT INTO `departamento` VALUES (10, 'ESO', 'Secundaria');
INSERT INTO `departamento` VALUES (11, 'BAC', 'Bachillerato');
INSERT INTO `departamento` VALUES (12, 'SAT', 'SAT');
INSERT INTO `departamento` VALUES (13, 'MANT', 'Mantenimiento');
INSERT INTO `departamento` VALUES (14, 'PDB', 'Premio Don Bosco');
INSERT INTO `departamento` VALUES (15, 'FOR', 'Formacion');
INSERT INTO `departamento` VALUES (16, 'ADM', 'Administracion');
INSERT INTO `departamento` VALUES (17, 'MARK', 'Marketing y Publicidad');
INSERT INTO `departamento` VALUES (18, 'LOGI', 'Logística y Almacén');

-- ----------------------------
-- Table structure for departamentoproveedor
-- ----------------------------
DROP TABLE IF EXISTS `departamentoproveedor`;
CREATE TABLE `departamentoproveedor`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `proveedorID` int NOT NULL,
  `departamentoID` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `proveedorID`(`proveedorID` ASC) USING BTREE,
  INDEX `departamento`(`departamentoID` ASC) USING BTREE,
  CONSTRAINT `departamento` FOREIGN KEY (`departamentoID`) REFERENCES `departamento` (`idDepartamento`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `proveedorID` FOREIGN KEY (`proveedorID`) REFERENCES `proveedores` (`idProveedor`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of departamentoproveedor
-- ----------------------------
INSERT INTO `departamentoproveedor` VALUES (1, 1, 1);
INSERT INTO `departamentoproveedor` VALUES (2, 2, 16);

-- ----------------------------
-- Table structure for facturas
-- ----------------------------
DROP TABLE IF EXISTS `facturas`;
CREATE TABLE `facturas`  (
  `idFactura` int NOT NULL AUTO_INCREMENT,
  `blobFactura` longblob NOT NULL,
  `fechaCreacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `idOrdenCompra` int NOT NULL,
  PRIMARY KEY (`idFactura`) USING BTREE,
  INDEX `fk_factura_orden`(`idOrdenCompra` ASC) USING BTREE,
  CONSTRAINT `fk_factura_orden` FOREIGN KEY (`idOrdenCompra`) REFERENCES `ordencompra` (`idOrden`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facturas
-- ----------------------------
INSERT INTO `facturas` VALUES (1, 0x4661637475726131, '2026-03-17 16:22:35', 1);
INSERT INTO `facturas` VALUES (2, 0x4661637475726132, '2026-03-17 16:22:35', 2);
INSERT INTO `facturas` VALUES (3, 0x4661637475726133, '2026-03-17 16:22:35', 3);
INSERT INTO `facturas` VALUES (4, 0x466163747572615F4E75657661, '2026-04-13 17:48:21', 4);

-- ----------------------------
-- Table structure for ordencompra
-- ----------------------------
DROP TABLE IF EXISTS `ordencompra`;
CREATE TABLE `ordencompra`  (
  `idOrden` int NOT NULL AUTO_INCREMENT,
  `idProveedor` int NULL DEFAULT NULL,
  `idPresupuesto` int NOT NULL,
  `numero_orden` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `numero_plan` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `Cantidad` decimal(12, 2) NOT NULL,
  `Inversion` tinyint(1) NULL DEFAULT 0,
  `Tipo` enum('Invariable','Fungible') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Fungible',
  `Observaciones` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `fechaCreacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `Estado` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Pendiente',
  PRIMARY KEY (`idOrden`) USING BTREE,
  UNIQUE INDEX `numero_orden`(`numero_orden` ASC) USING BTREE,
  INDEX `fk_orden_proveedor`(`idProveedor` ASC) USING BTREE,
  INDEX `fk_orden_presupuesto`(`idPresupuesto` ASC) USING BTREE,
  CONSTRAINT `fk_orden_presupuesto` FOREIGN KEY (`idPresupuesto`) REFERENCES `presupuesto` (`idPresupuesto`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orden_proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedores` (`idProveedor`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ordencompra
-- ----------------------------
INSERT INTO `ordencompra` VALUES (1, 1, 1, 'INFO/001/26/0', '1234567', 500.00, 0, 'Fungible', 'Compra portatiles', '2026-03-17 16:22:35', 'Pendiente');
INSERT INTO `ordencompra` VALUES (2, 2, 2, 'ADM/001/26/0', '7654321', 150.00, 0, 'Fungible', 'Material oficina', '2026-03-17 16:22:35', 'Pendiente');
INSERT INTO `ordencompra` VALUES (3, 1, 1, 'INFO/002/26/0', '1234567', 1200.00, 1, 'Invariable', 'Monitores nuevos', '2026-03-17 16:22:35', 'Pendiente');
INSERT INTO `ordencompra` VALUES (4, 3, 4, 'AUTO/001/26/1', '9998887', 450.00, 0, 'Fungible', 'Pedido urgente de lubricantes', '2026-04-13 17:48:15', 'Pendiente');

-- ----------------------------
-- Table structure for ordencompraproductos
-- ----------------------------
DROP TABLE IF EXISTS `ordencompraproductos`;
CREATE TABLE `ordencompraproductos`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `idOrdenCompra` int NOT NULL,
  `idProducto` int NOT NULL,
  `PrecioUnitario` decimal(12, 2) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `OrdenCompra`(`idOrdenCompra` ASC) USING BTREE,
  INDEX `Productos`(`idProducto` ASC) USING BTREE,
  CONSTRAINT `OrdenCompra` FOREIGN KEY (`idOrdenCompra`) REFERENCES `ordencompra` (`idOrden`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Productos` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ordencompraproductos
-- ----------------------------
INSERT INTO `ordencompraproductos` VALUES (1, 1, 1, 0.00);
INSERT INTO `ordencompraproductos` VALUES (2, 1, 4, 0.00);
INSERT INTO `ordencompraproductos` VALUES (3, 2, 5, 0.00);
INSERT INTO `ordencompraproductos` VALUES (4, 2, 2, 0.00);
INSERT INTO `ordencompraproductos` VALUES (5, 3, 3, 0.00);

-- ----------------------------
-- Table structure for presupuesto
-- ----------------------------
DROP TABLE IF EXISTS `presupuesto`;
CREATE TABLE `presupuesto`  (
  `idPresupuesto` int NOT NULL AUTO_INCREMENT,
  `Codigo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Nombre` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Cantidad` decimal(12, 2) NOT NULL,
  `Gasto` decimal(12, 2) NULL DEFAULT 0.00,
  `idDepartamento` int NOT NULL,
  PRIMARY KEY (`idPresupuesto`) USING BTREE,
  UNIQUE INDEX `Codigo`(`Codigo` ASC) USING BTREE,
  INDEX `fk_presupuesto_departamento`(`idDepartamento` ASC) USING BTREE,
  CONSTRAINT `fk_presupuesto_departamento` FOREIGN KEY (`idDepartamento`) REFERENCES `departamento` (`idDepartamento`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of presupuesto
-- ----------------------------
INSERT INTO `presupuesto` VALUES (1, 'PRES-IT-01', 'Presupuesto IT 2026', 10000.00, 2000.00, 1);
INSERT INTO `presupuesto` VALUES (2, 'PRES-RRHH-01', 'Presupuesto RRHH 2026', 5000.00, 1000.00, 2);
INSERT INTO `presupuesto` VALUES (3, 'PRES-FIN-01', 'Presupuesto Finanzas 2026', 8000.00, 1500.00, 3);
INSERT INTO `presupuesto` VALUES (4, 'PRES-AUTO-2026', 'Presupuesto Taller Automoción', 12000.00, 0.00, 4);

-- ----------------------------
-- Table structure for productos
-- ----------------------------
DROP TABLE IF EXISTS `productos`;
CREATE TABLE `productos`  (
  `idProducto` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Descripcion` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'Sin descripción',
  PRIMARY KEY (`idProducto` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of productos
-- ----------------------------
INSERT INTO `productos` VALUES (11, 'Aceite Motor 5W30', 'Lata 5 Litros para taller');
INSERT INTO `productos` VALUES (10, 'Cable HDMI', 'Cable 2 metros v2.0');
INSERT INTO `productos` VALUES (9, 'Mesa de Escritorio', 'Mesa de madera 140x70cm');
INSERT INTO `productos` VALUES (8, 'Tóner Impresora', 'Cartucho de tinta negra alta capacidad');
INSERT INTO `productos` VALUES (7, 'Webcam HD', 'Resolución 1080p para reuniones');
INSERT INTO `productos` VALUES (6, 'Ratón Inalámbrico', 'Ratón óptico ergonómico');
INSERT INTO `productos` VALUES (5, 'Papel A4', 'Paquete 500 hojas');
INSERT INTO `productos` VALUES (4, 'Teclado mecanico', 'Teclado RGB');
INSERT INTO `productos` VALUES (3, 'Monitor 24', 'Monitor Full HD');
INSERT INTO `productos` VALUES (2, 'Silla ergonomica', 'Silla comoda');
INSERT INTO `productos` VALUES (1, 'Portatil Dell', 'Portatil oficina');

-- ----------------------------
-- Table structure for productosproveedores
-- ----------------------------
DROP TABLE IF EXISTS `productosproveedores`;
CREATE TABLE `productosproveedores`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `idProducto` int NOT NULL,
  `idProveedor` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `Producto`(`idProducto` ASC) USING BTREE,
  INDEX `Proveedor`(`idProveedor` ASC) USING BTREE,
  CONSTRAINT `Producto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `Proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedores` (`idProveedor`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of productosproveedores
-- ----------------------------
INSERT INTO `productosproveedores` VALUES (1, 1, 1);
INSERT INTO `productosproveedores` VALUES (2, 4, 1);
INSERT INTO `productosproveedores` VALUES (3, 2, 2);
INSERT INTO `productosproveedores` VALUES (4, 5, 2);

-- ----------------------------
-- Table structure for proveedores
-- ----------------------------
DROP TABLE IF EXISTS `proveedores`;
CREATE TABLE `proveedores`  (
  `idProveedor` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Direccion` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `CP` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `Telefono` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`idProveedor`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of proveedores
-- ----------------------------
INSERT INTO `proveedores` VALUES (1, 'Proveedor Tech', 'Calle Tecnologia 1', '50001', '600111222');
INSERT INTO `proveedores` VALUES (2, 'OfiMarket', 'Avenida Oficina 23', '50002', '600333444');
INSERT INTO `proveedores` VALUES (3, 'Suministros SL', 'Calle Industria 45', '50003', '600555666');
INSERT INTO `proveedores` VALUES (4, 'PC Componentes', 'Polígono Industrial s/n', '30001', '968112233');
INSERT INTO `proveedores` VALUES (5, 'Amazon Business', 'Online', '28001', '900800700');
INSERT INTO `proveedores` VALUES (6, 'Papelería García', 'Calle Mayor 5', '50004', '976554433');
INSERT INTO `proveedores` VALUES (7, 'Recambios Paco', 'Polígono Industrial 4', '50012', '699888777');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `idRol` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Descripcion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dbUser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`idRol`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'Administrador', 'Acceso total y edición de todo', NULL);
INSERT INTO `roles` VALUES (2, 'Contable', 'Visualización total y marcado de facturas (sin edición)', NULL);
INSERT INTO `roles` VALUES (3, 'Jefe de Equipo', 'Edición de su departamento y visualización total', NULL);

-- ----------------------------
-- Table structure for usuario
-- ----------------------------
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario`  (
  `IdUsuario` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Apellidos` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Contrasena` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Correo` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Telefono` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `idRol` int NOT NULL,
  `idDepartamento` int NULL DEFAULT NULL,
  PRIMARY KEY (`IdUsuario`) USING BTREE,
  UNIQUE INDEX `Correo`(`Correo` ASC) USING BTREE,
  INDEX `fk_usuario_rol`(`idRol` ASC) USING BTREE,
  INDEX `fk_departamento`(`idDepartamento` ASC) USING BTREE,
  CONSTRAINT `fk_departamento` FOREIGN KEY (`idDepartamento`) REFERENCES `departamento` (`idDepartamento`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`idRol`) REFERENCES `roles` (`idRol`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of usuario
-- ----------------------------
INSERT INTO `usuario` VALUES (1, 'Admin', 'Principal', '1234', 'admin@empresa.com', '600000001', 1, 1);
INSERT INTO `usuario` VALUES (2, 'Ana', 'Contable', '1234', 'contable@empresa.com', '600000002', 2, NULL);
INSERT INTO `usuario` VALUES (3, 'Luis', 'IT', '1234', 'luis@empresa.com', '600000003', 3, 1);
INSERT INTO `usuario` VALUES (4, 'Maria', 'RRHH', '1234', 'maria@empresa.com', '600000004', 3, 2);
INSERT INTO `usuario` VALUES (5, 'Carlos', 'Finanzas', '1234', 'carlos@empresa.com', '600000005', 3, 3);

-- ----------------------------
-- Procedure structure for borrar_proveedores_inactivos
-- ----------------------------
DROP PROCEDURE IF EXISTS `borrar_proveedores_inactivos`;
delimiter ;;
CREATE PROCEDURE `borrar_proveedores_inactivos`()
BEGIN
    DELETE FROM proveedores 
    WHERE idProveedor NOT IN (
        SELECT DISTINCT pp.idProveedor
        FROM productosproveedores pp
        JOIN ordencompraproductos ocp ON pp.idProducto = ocp.idProducto
        JOIN ordencompra oc ON ocp.idOrdenCompra = oc.idOrden
        WHERE oc.fechaCreacion >= DATE_SUB(CURDATE(), INTERVAL 3 YEAR)
    );
    SELECT 'Proveedores inactivos eliminados correctamente' AS resultado;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for clonar_presupuestos_nuevo_anio
-- ----------------------------
DROP PROCEDURE IF EXISTS `clonar_presupuestos_nuevo_anio`;
delimiter ;;
CREATE PROCEDURE `clonar_presupuestos_nuevo_anio`(IN p_incremento DECIMAL(5,2))
BEGIN
    INSERT INTO presupuesto (Codigo, Nombre, Cantidad, Gasto, idDepartamento)
    SELECT 
        CONCAT(Codigo, '-NEXT'), 
        CONCAT(Nombre, ' Proyectado'), 
        Cantidad * (1 + p_incremento), 
        0.00, 
        idDepartamento
    FROM presupuesto;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for fn_desencriptar
-- ----------------------------
DROP FUNCTION IF EXISTS `fn_desencriptar`;
delimiter ;;
CREATE FUNCTION `fn_desencriptar`(p_blob BLOB, p_llave VARCHAR(100))
 RETURNS varchar(255) CHARSET utf8mb4
  DETERMINISTIC
BEGIN
    RETURN CAST(AES_DECRYPT(p_blob, p_llave) AS CHAR);
END
;;
delimiter ;

-- ----------------------------
-- Function structure for fn_encriptar
-- ----------------------------
DROP FUNCTION IF EXISTS `fn_encriptar`;
delimiter ;;
CREATE FUNCTION `fn_encriptar`(p_texto VARCHAR(255), p_llave VARCHAR(100))
 RETURNS blob
  DETERMINISTIC
BEGIN
    RETURN AES_ENCRYPT(p_texto, p_llave);
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertar_usuario
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertar_usuario`;
delimiter ;;
CREATE PROCEDURE `insertar_usuario`(IN p_nombre VARCHAR(100),
    IN p_apellidos VARCHAR(150),
    IN p_email VARCHAR(150),
    IN p_password VARCHAR(255),
    IN p_telefono VARCHAR(20))
BEGIN
    DECLARE existe INT;

    -- Verificar si el email ya existe (Usamos la tabla 'usuario')
    SELECT COUNT(*) INTO existe
    FROM usuario
    WHERE Correo = p_email;

    IF existe > 0 THEN
        SELECT 'error' AS status, 'El usuario ya existe con ese email' AS mensaje;
    ELSE
        -- Insertamos con rol predeterminado 3 (Jefe Departamento) por ejemplo
        INSERT INTO usuario (Nombre, Apellidos, Contrasena, Correo, Telefono, idRol)
        VALUES (p_nombre, p_apellidos, p_password, p_email, p_telefono, 3);

        SELECT 'success' AS status, 'Usuario insertado correctamente' AS mensaje;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for obtener_ordenes_por_departamento
-- ----------------------------
DROP PROCEDURE IF EXISTS `obtener_ordenes_por_departamento`;
delimiter ;;
CREATE PROCEDURE `obtener_ordenes_por_departamento`(IN dep_nombre VARCHAR(100))
BEGIN
    SELECT oc.*
    FROM ordencompra oc
    JOIN presupuesto p ON p.idPresupuesto = oc.idPresupuesto
    JOIN departamento d ON d.idDepartamento = p.idDepartamento
    WHERE d.Nombre = dep_nombre;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for obtener_proveedores_por_anio
-- ----------------------------
DROP PROCEDURE IF EXISTS `obtener_proveedores_por_anio`;
delimiter ;;
CREATE PROCEDURE `obtener_proveedores_por_anio`(IN p_idDepartamento INT, IN p_anio INT)
BEGIN
    SELECT DISTINCT prov.idProveedor, prov.Nombre, prov.Telefono
    FROM proveedores prov
    JOIN productosproveedores pp ON prov.idProveedor = pp.idProveedor
    JOIN ordencompraproductos ocp ON pp.idProducto = ocp.idProducto
    JOIN ordencompra oc ON ocp.idOrdenCompra = oc.idOrden
    JOIN presupuesto pres ON oc.idPresupuesto = pres.idPresupuesto
    WHERE pres.idDepartamento = p_idDepartamento 
      AND YEAR(oc.fechaCreacion) = p_anio;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for ordenes_sin_factura
-- ----------------------------
DROP FUNCTION IF EXISTS `ordenes_sin_factura`;
delimiter ;;
CREATE FUNCTION `ordenes_sin_factura`(p_idDepartamento INT)
 RETURNS text CHARSET utf8mb4
  DETERMINISTIC
BEGIN
    DECLARE resultado TEXT;

    SELECT GROUP_CONCAT(oc.idOrden)
    INTO resultado
    FROM ordencompra oc
    JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto
    LEFT JOIN facturas f ON oc.idOrden = f.idOrdenCompra
    WHERE p.idDepartamento = p_idDepartamento
      AND f.idFactura IS NULL;

    RETURN resultado;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for presupuesto_pendiente
-- ----------------------------
DROP FUNCTION IF EXISTS `presupuesto_pendiente`;
delimiter ;;
CREATE FUNCTION `presupuesto_pendiente`(p_idPresupuesto INT)
 RETURNS decimal(12,2)
  READS SQL DATA 
BEGIN
    DECLARE total_presupuesto DECIMAL(12,2);
    DECLARE total_gastado DECIMAL(12,2);

    SELECT Cantidad, Gasto INTO total_presupuesto, total_gastado
    FROM presupuesto
    WHERE idPresupuesto = p_idPresupuesto;

    RETURN total_presupuesto - total_gastado;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table ordencompraproductos
-- ----------------------------
DROP TRIGGER IF EXISTS `tg_actualizar_gasto_presupuesto`;
delimiter ;;
CREATE TRIGGER `tg_actualizar_gasto_presupuesto` AFTER INSERT ON `ordencompraproductos` FOR EACH ROW BEGIN
    UPDATE presupuesto p
    JOIN ordencompra oc ON p.idPresupuesto = oc.idPresupuesto
    SET p.Gasto = p.Gasto + 100
    WHERE oc.idOrden = NEW.idOrdenCompra;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;



-- ENTREGA 4 CONSULTAS SQL

-- Una consulta con subconsulta

SELECT Nombre 
FROM departamento 
WHERE idDepartamento IN (
    SELECT idDepartamento 
    FROM presupuesto 
    WHERE Cantidad > (SELECT AVG(Cantidad) FROM presupuesto)
);

-- Una consulta con función (concat, substring... )
SELECT UPPER(CONCAT(Nombre, ' ', Apellidos)) AS Nombre_Completo, Correo 
FROM usuario;

-- Una consulta con full outer join
SELECT prov.Nombre AS Proveedor, prod.Nombre AS Producto
FROM proveedores prov
LEFT JOIN productosproveedores pp ON prov.idProveedor = pp.idProveedor
LEFT JOIN productos prod ON pp.idProducto = prod.idProducto
UNION
SELECT prov.Nombre AS Proveedor, prod.Nombre AS Producto
FROM proveedores prov
RIGHT JOIN productosproveedores pp ON prov.idProveedor = pp.idProveedor
RIGHT JOIN productos prod ON pp.idProducto = prod.idProducto;

--  Un Select que muestre un pedido de este año, uno del año pasado y uno de hace 2 años, y todos ellos de diferentes departamentos.
(SELECT oc.idOrden, YEAR(oc.fechaCreacion) as Anio, d.Nombre as Departamento
 FROM ordencompra oc
 JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto
 JOIN departamento d ON p.idDepartamento = d.idDepartamento
 WHERE YEAR(oc.fechaCreacion) = 2026 LIMIT 1)
UNION ALL
(SELECT oc.idOrden, YEAR(oc.fechaCreacion) as Anio, d.Nombre as Departamento
 FROM ordencompra oc
 JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto
 JOIN departamento d ON p.idDepartamento = d.idDepartamento
 WHERE YEAR(oc.fechaCreacion) = 2025 LIMIT 1)
UNION ALL
(SELECT oc.idOrden, YEAR(oc.fechaCreacion) as Anio, d.Nombre as Departamento
 FROM ordencompra oc
 JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto
 JOIN departamento d ON p.idDepartamento = d.idDepartamento
 WHERE YEAR(oc.fechaCreacion) = 2024 LIMIT 1);
 
 -- Una consulta con having y agrupación.
 SELECT d.Nombre, SUM(p.Cantidad) AS Total_Presupuesto
FROM departamento d
JOIN presupuesto p ON d.idDepartamento = p.idDepartamento
GROUP BY d.Nombre
HAVING Total_Presupuesto > 6000;
