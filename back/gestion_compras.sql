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

 Date: 11/05/2026 17:53:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comentarios_orden
-- ----------------------------
DROP TABLE IF EXISTS `comentarios_orden`;
CREATE TABLE `comentarios_orden`  (
  `idComentario` int NOT NULL AUTO_INCREMENT,
  `idOrden` int NOT NULL,
  `idUsuario` int NOT NULL,
  `comentario` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idComentario`) USING BTREE,
  INDEX `fk_comentario_orden`(`idOrden` ASC) USING BTREE,
  INDEX `fk_comentario_usuario`(`idUsuario` ASC) USING BTREE,
  CONSTRAINT `fk_comentario_orden` FOREIGN KEY (`idOrden`) REFERENCES `ordencompra` (`idOrden`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comentario_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`IdUsuario`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notificaciones
-- ----------------------------
DROP TABLE IF EXISTS `notificaciones`;
CREATE TABLE `notificaciones`  (
  `idNotificacion` int NOT NULL AUTO_INCREMENT,
  `idUsuarioDestino` int NOT NULL,
  `mensaje` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `leida` tinyint(1) NULL DEFAULT 0,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `idOrden` int NULL DEFAULT NULL,
  PRIMARY KEY (`idNotificacion`) USING BTREE,
  INDEX `fk_notif_usuario`(`idUsuarioDestino` ASC) USING BTREE,
  CONSTRAINT `fk_notif_usuario` FOREIGN KEY (`idUsuarioDestino`) REFERENCES `usuario` (`IdUsuario`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
  `descripcion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `fechaCreacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `Estado` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Pendiente',
  PRIMARY KEY (`idOrden`) USING BTREE,
  UNIQUE INDEX `numero_orden`(`numero_orden` ASC) USING BTREE,
  INDEX `fk_orden_proveedor`(`idProveedor` ASC) USING BTREE,
  INDEX `fk_orden_presupuesto`(`idPresupuesto` ASC) USING BTREE,
  CONSTRAINT `fk_orden_presupuesto` FOREIGN KEY (`idPresupuesto`) REFERENCES `presupuesto` (`idPresupuesto`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orden_proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedores` (`idProveedor`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
  `Type` enum('presupuesto','planInversion') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`idPresupuesto`) USING BTREE,
  UNIQUE INDEX `Codigo`(`Codigo` ASC) USING BTREE,
  INDEX `fk_presupuesto_departamento`(`idDepartamento` ASC) USING BTREE,
  CONSTRAINT `fk_presupuesto_departamento` FOREIGN KEY (`idDepartamento`) REFERENCES `departamento` (`idDepartamento`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for productos
-- ----------------------------
DROP TABLE IF EXISTS `productos`;
CREATE TABLE `productos`  (
  `idProducto` int NOT NULL AUTO_INCREMENT,
  `Nombre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `Descripcion` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'Sin descripción',
  PRIMARY KEY (`idProducto` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
-- View structure for vista_ordenes_detalle
-- ----------------------------
DROP VIEW IF EXISTS `vista_ordenes_detalle`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `vista_ordenes_detalle` AS select `o`.`idOrden` AS `idOrden`,`o`.`fechaCreacion` AS `fechaCreacion`,`o`.`Cantidad` AS `Cantidad`,`o`.`Estado` AS `Estado`,`p`.`Nombre` AS `presupuesto`,`d`.`Nombre` AS `departamento` from ((`ordencompra` `o` join `presupuesto` `p` on((`o`.`idPresupuesto` = `p`.`idPresupuesto`))) join `departamento` `d` on((`p`.`idDepartamento` = `d`.`idDepartamento`)));

-- ----------------------------
-- View structure for vista_ordenes_detalle_fixed
-- ----------------------------
DROP VIEW IF EXISTS `vista_ordenes_detalle_fixed`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `vista_ordenes_detalle_fixed` AS select `o`.`idOrden` AS `idOrden`,`o`.`fechaCreacion` AS `fechaCreacion`,`o`.`Cantidad` AS `Cantidad`,`o`.`Estado` AS `Estado`,`p`.`Nombre` AS `presupuesto`,`d`.`Nombre` AS `departamento` from ((`ordencompra` `o` join `presupuesto` `p` on((`o`.`idPresupuesto` = `p`.`idPresupuesto`))) join `departamento` `d` on((`p`.`idDepartamento` = `d`.`idDepartamento`)));

-- ----------------------------
-- View structure for vista_resumen_presupuestos
-- ----------------------------
DROP VIEW IF EXISTS `vista_resumen_presupuestos`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `vista_resumen_presupuestos` AS select `d`.`idDepartamento` AS `idDepartamento`,`d`.`Nombre` AS `departamento`,sum(`p`.`Cantidad`) AS `presupuesto_total`,sum(`p`.`Gasto`) AS `total_gastado`,(sum(`p`.`Cantidad`) - sum(`p`.`Gasto`)) AS `presupuesto_restante`,sum(`o`.`Cantidad`) AS `total_inventariable` from ((`departamento` `d` left join `presupuesto` `p` on((`d`.`idDepartamento` = `p`.`idDepartamento`))) left join `ordencompra` `o` on((`p`.`idPresupuesto` = `o`.`idPresupuesto`))) group by `d`.`idDepartamento`,`d`.`Nombre`;

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
-- Procedure structure for obtener_presupuestos_por_departamento
-- ----------------------------
DROP PROCEDURE IF EXISTS `obtener_presupuestos_por_departamento`;
delimiter ;;
CREATE PROCEDURE `obtener_presupuestos_por_departamento`(IN dep_nombre VARCHAR(100))
BEGIN
    SELECT * from presupuesto where idDepartamento = dep_nombre;
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
-- Triggers structure for table ordencompra
-- ----------------------------
DROP TRIGGER IF EXISTS `no_borrar_orden_con_factura`;
delimiter ;;
CREATE TRIGGER `no_borrar_orden_con_factura` BEFORE DELETE ON `ordencompra` FOR EACH ROW BEGIN
    DECLARE existe INT;

    SELECT COUNT(*) INTO existe
    FROM facturas
    WHERE idOrdenCompra = OLD.idOrden;

    IF existe > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede eliminar la orden porque tiene factura asociada';
    END IF;
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
