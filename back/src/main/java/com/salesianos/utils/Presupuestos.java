package com.salesianos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Presupuestos {

    private static final Logger LOGGER = Logger.getLogger(Presupuestos.class.getName());

    public List<Map<String, String>> getAllPresupuestos(int year) {
        String sql = "SELECT p.idPresupuesto, p.Codigo, p.Nombre AS nombrePresupuesto, p.idDepartamento, d.Nombre AS nombreDepartamento, p.Cantidad, p.Gasto, p.Type, p.Anio " +
                     "FROM presupuesto p LEFT JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE p.Anio = ?";
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, String> p = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = metaData.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        p.put(col.toLowerCase(), val != null ? val.toString() : "");
                    }
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all presupuestos for year " + year, e);
        }
        return list;
    }

    public List<Integer> getYearsWithPresupuestos() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT Anio FROM presupuesto ORDER BY Anio DESC";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("Anio"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting years with budgets", e);
        }
        // If empty, return at least current year
        if (years.isEmpty()) years.add(java.time.LocalDate.now().getYear());
        return years;
    }

    public List<Map<String, String>> getPresupuestosByDept(String dep, int year) {
        if (dep == null) {
            throw new IllegalArgumentException("department cannot be null");
        }

        String sql = "SELECT p.idPresupuesto, p.Codigo, p.Nombre AS nombrePresupuesto, p.idDepartamento, d.Nombre AS nombreDepartamento, p.Cantidad, p.Gasto, p.Type, p.Anio " +
                     "FROM presupuesto p " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE d.Nombre = ? AND p.Anio = ?";
        List<Map<String, String>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection("webapp");
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dep);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, String> p = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = metaData.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        p.put(col.toLowerCase(), val != null ? val.toString() : "");
                    }
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting presupuestos by dept: " + dep + " for year " + year, e);
        }
        return list;
    }
    public boolean cloneBudgets(int fromYear, int toYear) {
        String sql = "INSERT INTO presupuesto (Codigo, Nombre, Cantidad, Gasto, idDepartamento, Type, Anio) " +
                     "SELECT CONCAT(Codigo, '-', ?), Nombre, Cantidad, 0.00, idDepartamento, Type, ? " +
                     "FROM presupuesto WHERE Anio = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, toYear);
            stmt.setInt(2, toYear);
            stmt.setInt(3, fromYear);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cloning budgets from " + fromYear + " to " + toYear, e);
        }
        return false;
    }
}
