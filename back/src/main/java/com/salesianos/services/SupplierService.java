package com.salesianos.services;

import com.salesianos.models.Supplier;
import com.salesianos.repositories.SupplierRepository;
import java.util.List;

public class SupplierService {
    private SupplierRepository repository = new SupplierRepository();

    public List<Supplier> getAllSuppliers() {
        return repository.findAll();
    }

    public long createSupplier(Supplier s) {
        return repository.save(s);
    }

    public boolean updateSupplier(Supplier s) {
        return repository.update(s);
    }

    public boolean deleteSupplier(long id) {
        return repository.delete(id);
    }

    public List<Product> getProducts(long idSupplier) {
        return repository.getSupplierProducts(idSupplier);
    }

    public boolean assignProduct(long idSupplier, long idProduct) {
        return repository.assignProduct(idSupplier, idProduct);
    }

    public boolean removeProduct(long idSupplier, long idProduct) {
        return repository.removeProduct(idSupplier, idProduct);
    }
}
