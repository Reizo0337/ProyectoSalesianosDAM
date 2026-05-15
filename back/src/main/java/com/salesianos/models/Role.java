package com.salesianos.models;

public enum Role {
    ADMIN("Admin"),
    ADMINISTRADOR("Administrador"),
    JEFE_EQUIPO("Jefe de Equipo"),
    CONTABLE("Contable"),
    EMPLEADO("Empleado"),
    USER("Usuario");

    private final String dbName;

    Role(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.dbName.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return USER; // Default safe fallback
    }

    // Permission Groups
    public boolean canManageInvoices() {
        return this == ADMIN || this == ADMINISTRADOR || this == CONTABLE;
    }

    public boolean canComment() {
        return this == ADMIN || this == ADMINISTRADOR || this == CONTABLE || this == JEFE_EQUIPO;
    }

    public boolean canChangeStatus() {
        return this == ADMIN || this == ADMINISTRADOR || this == JEFE_EQUIPO;
    }

    public boolean canManageSuppliers() {
        return this == ADMIN || this == ADMINISTRADOR || this == JEFE_EQUIPO;
    }
    
    public boolean hasGlobalAccess() {
        return this == ADMIN || this == ADMINISTRADOR || this == CONTABLE;
    }
}
