package com.salesianos.services;

import com.salesianos.models.Order;
import com.salesianos.models.Budget;
import com.salesianos.repositories.*;
import java.util.List;
import java.util.Map;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final BudgetRepository budgetRepository = new BudgetRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final UserRepository userRepository = new UserRepository();

    public List<Order> getOrdersByYear(int year) {
        return orderRepository.findAllByYear(year);
    }

    public List<Integer> getYears() {
        return orderRepository.findYears();
    }

    public Map<String, Object> getOrderDetail(long id, String dept, String role) {
        return orderRepository.findByIdDetailed(id, dept, role);
    }

    public long createOrder(Order o, List<Map<String, String>> products) {
        // 1. Check budget
        Budget b = budgetRepository.findById(o.getIdPresupuesto());
        if (b == null) return -1;
        if (b.getDisponible() < o.getCantidad()) return -2; // Insufficient

        // 2. Save order
        long id = orderRepository.save(o);
        if (id > 0 && products != null) {
            for (Map<String, String> p : products) {
                orderRepository.addProduct(id, Long.parseLong(p.get("id")), Double.parseDouble(p.get("precio")));
            }
        }
        return id;
    }

    public boolean updateOrder(Order o) {
        Order old = orderRepository.findById(o.getIdOrden());
        if (old == null || "Cerrada".equalsIgnoreCase(old.getEstado())) return false;

        boolean ok = orderRepository.update(o);
        if (ok && "Aprobada".equalsIgnoreCase(old.getEstado())) {
            double diff = o.getCantidad() - old.getCantidad();
            if (diff != 0) budgetRepository.updateGasto(o.getIdPresupuesto(), diff);
        }
        return ok;
    }

    public boolean updateStatus(long id, String newStatus) {
        Order o = orderRepository.findById(id);
        if (o == null || "Cerrada".equalsIgnoreCase(o.getEstado())) return false;

        boolean ok = orderRepository.updateStatus(id, newStatus);
        if (ok) {
            if ("Cerrada".equalsIgnoreCase(newStatus)) {
                budgetRepository.updateGasto(o.getIdPresupuesto(), o.getCantidad());
            }
            notifyStatusChange(id, o.getEstado(), newStatus);
        }
        return ok;
    }

    public boolean deleteOrder(long id, String dept, String role) {
        Order o = orderRepository.findById(id);
        if (o == null || "Cerrada".equalsIgnoreCase(o.getEstado())) return false;
        
        // Permission check should be here or in controller. Let's do it in controller for better role handling.
        return orderRepository.delete(id);
    }

    public String getNextSequence(String dept, String year) {
        return orderRepository.getNextSequence(dept, year);
    }

    public boolean addInvoice(long orderId, byte[] fileData) {
        Order o = orderRepository.findById(orderId);
        if (o == null) return false;

        boolean ok = orderRepository.addInvoice(orderId, fileData);
        if (ok && !"Cerrada".equalsIgnoreCase(o.getEstado())) {
            orderRepository.updateStatus(orderId, "Cerrada");
            budgetRepository.updateGasto(o.getIdPresupuesto(), o.getCantidad());
        }
        return ok;
    }

    private void notifyStatusChange(long orderId, String oldS, String newS) {
        String msg = "La orden " + orderId + " cambió: " + oldS + " -> " + newS;
        List<Integer> heads = userRepository.findHeadsByOrder(orderId);
        for (Integer headId : heads) {
            notificationRepository.create(headId, msg, orderId);
        }
    }
}
