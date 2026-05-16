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
}

// Podríamos ponerlos en archivos separados, pero para agilizar los creamos aquí si el usuario no tiene inconveniente.
// Sin embargo, por estándares de Java, lo ideal es un archivo por clase pública.
// Crearé los archivos separados para que el video sea perfecto.
