package com.ecommerce.ecom_backend.entity;

import com.ecommerce.ecom_backend.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Setter
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(length = 100)
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Shipment() {
    }

    public Shipment(Long id, Order order, String trackingNumber, String carrier, ShipmentStatus status, LocalDateTime estimatedDelivery, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.status = status;
        this.estimatedDelivery = estimatedDelivery;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
