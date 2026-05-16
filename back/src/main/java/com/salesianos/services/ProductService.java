package com.salesianos.services;

import com.salesianos.models.Product;
import com.salesianos.repositories.ProductRepository;
import java.util.List;

public class ProductService {
    private ProductRepository repository = new ProductRepository();

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public long createProduct(Product p) {
        return repository.save(p);
    }

    public boolean updateProduct(Product p) {
        return repository.update(p);
    }

    public boolean deleteProduct(long id) {
        return repository.delete(id);
    }
}
