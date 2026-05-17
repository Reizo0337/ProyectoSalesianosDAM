package com.salesianos.services;

import com.salesianos.models.Budget;
import com.salesianos.models.Product;
import com.salesianos.models.Supplier;
import com.salesianos.repositories.BudgetRepository;
import com.salesianos.repositories.ProductRepository;
import com.salesianos.repositories.SupplierRepository;
import java.util.List;

public class BudgetService {
    private BudgetRepository repository = new BudgetRepository();

    public List<Budget> getBudgetsByYear(int year) {
        return repository.findAllByYear(year);
    }

    public List<Budget> getBudgetsByDept(String depName, int year) {
        return repository.findAllByDept(depName, year);
    }

    public List<Integer> getYears() {
        return repository.getYearsWithBudgets();
    }

    public boolean cloneBudgets(int fromYear, int toYear) {
        return repository.cloneBudgets(fromYear, toYear);
    }

    public String createBudget(Budget budget, int anio) {
        if (repository.existsByTypeAndDept(budget.getType(), budget.getIdDepartamento(), anio, null)) {
            return "Ya existe un " + (budget.getType().equals("planInversion") ? "Plan de Inversión" : "Presupuesto Genérico") 
                   + " asignado a este departamento para el año " + anio;
        }

        String[] deptInfo = repository.getDeptInfo(budget.getIdDepartamento());
        if (deptInfo == null) {
            return "El departamento seleccionado no existe";
        }
        String deptCode = deptInfo[0];
        String deptName = deptInfo[1];

        String generatedCode = "";
        String generatedName = "";
        if ("planInversion".equalsIgnoreCase(budget.getType())) {
            generatedCode = "PLAN-" + deptCode + "-" + anio;
            generatedName = "Plan de Inversión " + deptName + " " + anio;
        } else {
            generatedCode = "PRES-" + deptCode + "-" + anio;
            generatedName = "Presupuesto " + deptName + " " + anio;
        }

        budget.setCodigo(generatedCode);
        budget.setNombrePresupuesto(generatedName);

        boolean ok = repository.create(budget, anio);
        return ok ? "success" : "Error de base de datos al crear el presupuesto";
    }

    public String updateBudget(Budget budget, int anio) {
        if (repository.existsByTypeAndDept(budget.getType(), budget.getIdDepartamento(), anio, budget.getIdPresupuesto())) {
            return "Ya existe otro " + (budget.getType().equals("planInversion") ? "Plan de Inversión" : "Presupuesto Genérico") 
                   + " asignado a este departamento para el año " + anio;
        }

        String[] deptInfo = repository.getDeptInfo(budget.getIdDepartamento());
        if (deptInfo == null) {
            return "El departamento seleccionado no existe";
        }
        String deptCode = deptInfo[0];
        String deptName = deptInfo[1];

        String generatedCode = "";
        String generatedName = "";
        if ("planInversion".equalsIgnoreCase(budget.getType())) {
            generatedCode = "PLAN-" + deptCode + "-" + anio;
            generatedName = "Plan de Inversión " + deptName + " " + anio;
        } else {
            generatedCode = "PRES-" + deptCode + "-" + anio;
            generatedName = "Presupuesto " + deptName + " " + anio;
        }

        budget.setCodigo(generatedCode);
        budget.setNombrePresupuesto(generatedName);

        boolean ok = repository.update(budget);
        return ok ? "success" : "Error de base de datos al actualizar el presupuesto";
    }
}

// Podríamos ponerlos en archivos separados, pero para agilizar los creamos aquí si el usuario no tiene inconveniente.
// Sin embargo, por estándares de Java, lo ideal es un archivo por clase pública.
// Crearé los archivos separados para que el video sea perfecto.
