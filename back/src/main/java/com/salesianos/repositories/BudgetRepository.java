package com.salesianos.repositories;

import com.salesianos.models.Budget;
import com.salesianos.utils.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BudgetRepository {
    private static final Logger LOGGER = Logger.getLogger(BudgetRepository.class.getName());

    public List<Budget> findAllByYear(int year) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT p.*, d.Nombre as nombredepartamento " +
                     "FROM presupuesto p " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE p.Anio = ? " +
                     "ORDER BY p.idPresupuesto ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(mapResultSetToBudget(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all budgets by year", e);
        }
        return budgets;
    }

    public Budget findById(long id) {
        String sql = "SELECT p.*, d.Nombre as nombredepartamento " +
                     "FROM presupuesto p " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE p.idPresupuesto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToBudget(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding budget " + id, e);
        }
        return null;
    }

    public List<Integer> getYearsWithBudgets() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT Anio FROM presupuesto ORDER BY Anio DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("Anio"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching budget years", e);
        }
        if (years.isEmpty()) years.add(java.time.LocalDate.now().getYear());
        return years;
    }

    public List<Budget> findAllByDept(String depName, int year) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT p.*, d.Nombre as nombredepartamento " +
                     "FROM presupuesto p " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE d.Nombre = ? AND p.Anio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, depName);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(mapResultSetToBudget(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding budgets by dept: " + depName, e);
        }
        return budgets;
    }

    public boolean cloneBudgets(int fromYear, int toYear) {
        String sql = "INSERT INTO presupuesto (Codigo, Nombre, Cantidad, Gasto, idDepartamento, Type, Anio) " +
                     "SELECT CONCAT(Codigo, '-', ?), Nombre, Cantidad, 0.00, idDepartamento, Type, ? " +
                     "FROM presupuesto WHERE Anio = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, toYear);
            stmt.setInt(2, toYear);
            stmt.setInt(3, fromYear);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cloning budgets", e);
            return false;
        }
    }

    public boolean updateGasto(long budgetId, double diff) {
        String sql = "UPDATE presupuesto SET Gasto = Gasto + ? WHERE idPresupuesto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, diff);
            stmt.setLong(2, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating budget gasto", e);
            return false;
        }
    }

    public String[] getDeptInfo(long idDepartamento) {
        String sql = "SELECT Codigo, Nombre FROM departamento WHERE idDepartamento = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idDepartamento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("Codigo"), rs.getString("Nombre")};
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching department info for ID " + idDepartamento, e);
        }
        return null;
    }

    public boolean existsByTypeAndDept(String type, long idDepartamento, int anio, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM presupuesto WHERE Type = ? AND idDepartamento = ? AND Anio = ?";
        if (excludeId != null) {
            sql += " AND idPresupuesto != ?";
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setLong(2, idDepartamento);
            stmt.setInt(3, anio);
            if (excludeId != null) {
                stmt.setLong(4, excludeId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking budget existence", e);
        }
        return false;
    }

    public boolean create(Budget budget, int anio) {
        String sql = "INSERT INTO presupuesto (Codigo, Nombre, Cantidad, Gasto, idDepartamento, Type, Anio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, budget.getCodigo());
            stmt.setString(2, budget.getNombrePresupuesto());
            stmt.setDouble(3, budget.getCantidad());
            stmt.setDouble(4, budget.getGasto());
            stmt.setLong(5, budget.getIdDepartamento());
            stmt.setString(6, budget.getType());
            stmt.setInt(7, anio);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating budget", e);
            return false;
        }
    }

    public boolean update(Budget budget) {
        String sql = "UPDATE presupuesto SET Codigo = ?, Nombre = ?, Cantidad = ?, Gasto = ?, idDepartamento = ?, Type = ? WHERE idPresupuesto = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, budget.getCodigo());
            stmt.setString(2, budget.getNombrePresupuesto());
            stmt.setDouble(3, budget.getCantidad());
            stmt.setDouble(4, budget.getGasto());
            stmt.setLong(5, budget.getIdDepartamento());
            stmt.setString(6, budget.getType());
            stmt.setLong(7, budget.getIdPresupuesto());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating budget", e);
            return false;
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setIdPresupuesto(rs.getLong("idPresupuesto"));
        b.setCodigo(rs.getString("Codigo"));
        b.setNombrePresupuesto(rs.getString("Nombre"));
        b.setCantidad(rs.getDouble("Cantidad"));
        b.setGasto(rs.getDouble("Gasto"));
        b.setType(rs.getString("Type"));
        b.setIdDepartamento(rs.getLong("idDepartamento"));
        b.setNombreDepartamento(rs.getString("nombredepartamento"));
        return b;
    }
}
