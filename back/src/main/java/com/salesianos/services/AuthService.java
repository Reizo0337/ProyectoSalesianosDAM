package com.salesianos.services;

import com.salesianos.models.User;
import com.salesianos.repositories.UserRepository;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    public User login(String email, String password) {
        return userRepository.authenticate(email, password);
    }

    public String register(String nombre, String apellidos, String email, String password, String telefono) {
        return userRepository.register(nombre, apellidos, email, password, telefono);
    }
}
