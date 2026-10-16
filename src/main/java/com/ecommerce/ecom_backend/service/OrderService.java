package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.*;
import com.ecommerce.ecom_backend.entity.Order;
import com.ecommerce.ecom_backend.entity.OrderItem;
import com.ecommerce.ecom_backend.entity.Product;
import com.ecommerce.ecom_backend.entity.Shipment;
import com.ecommerce.ecom_backend.entity.User;
import com.ecommerce.ecom_backend.enums.OrderStatus;
import com.ecommerce.ecom_backend.enums.ShipmentStatus;
import com.ecommerce.ecom_backend.repository.OrderItemRepository;
import com.ecommerce.ecom_backend.repository.OrderRepository;
import com.ecommerce.ecom_backend.repository.ProductRepository;
import com.ecommerce.ecom_backend.repository.ShipmentRepository;
import com.ecommerce.ecom_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        ShipmentRepository shipmentRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<Long, Integer> productQuantities = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getProductId,
                        OrderItemRequest::getQuantity,
                        Integer::sum
                ));

        List<Long> productIds = productQuantities.keySet().stream().toList();

        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        if (products.size() != productQuantities.size()) {
            throw new RuntimeException("One or more products not found");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        Order savedOrder = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = products.get(entry.getKey());
            Integer quantity = entry.getValue();

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItemRepository.save(orderItem);

            savedOrder.getItems().add(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        Shipment shipment = new Shipment();
        shipment.setOrder(savedOrder);
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setCarrier("Standard Courier");
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
        shipmentRepository.save(shipment);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderResponse(order);
    }

    @Transactional
    public MessageResponse cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        shipmentRepository.findByOrderId(order.getId()).ifPresent(shipment -> {
            shipment.setStatus(ShipmentStatus.EXCEPTION);
            shipmentRepository.save(shipment);
        });

        return new MessageResponse("Order cancelled successfully");
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getTracking(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        return mapToShipmentResponse(shipment);
    }

    @Transactional
    public MessageResponse confirmDelivery(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Only pending or shipped orders can be confirmed as delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        return new MessageResponse("Delivery confirmed successfully");
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrder().getId(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getStatus(),
                shipment.getEstimatedDelivery(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
