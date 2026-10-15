package com.ecommerce.ecom_backend.config;

import com.ecommerce.ecom_backend.entity.Product;
import com.ecommerce.ecom_backend.entity.User;
import com.ecommerce.ecom_backend.enums.UserRole;
import com.ecommerce.ecom_backend.repository.ProductRepository;
import com.ecommerce.ecom_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("john@example.com").isEmpty()) {
                User john = new User();
                john.setEmail("john@example.com");
                john.setPasswordHash(passwordEncoder.encode("password123"));
                john.setFirstName("John");
                john.setLastName("Doe");
                john.setRole(UserRole.CUSTOMER);
                userRepository.save(john);
            }

            if (userRepository.findByEmail("jane@example.com").isEmpty()) {
                User jane = new User();
                jane.setEmail("jane@example.com");
                jane.setPasswordHash(passwordEncoder.encode("password123"));
                jane.setFirstName("Jane");
                jane.setLastName("Smith");
                jane.setRole(UserRole.CUSTOMER);
                userRepository.save(jane);
            }

            if (productRepository.findBySku("ELEC-001").isEmpty()) {
                Product mouse = new Product();
                mouse.setName("Wireless Mouse");
                mouse.setDescription("Ergonomic wireless mouse with 2.4 GHz connectivity.");
                mouse.setPrice(new BigDecimal("29.99"));
                mouse.setStockQuantity(100);
                mouse.setSku("ELEC-001");
                mouse.setCategory("Electronics");
                productRepository.save(mouse);
            }

            if (productRepository.findBySku("ELEC-002").isEmpty()) {
                Product keyboard = new Product();
                keyboard.setName("Mechanical Keyboard");
                keyboard.setDescription("RGB backlit mechanical keyboard with blue switches.");
                keyboard.setPrice(new BigDecimal("89.99"));
                keyboard.setStockQuantity(50);
                keyboard.setSku("ELEC-002");
                keyboard.setCategory("Electronics");
                productRepository.save(keyboard);
            }

            if (productRepository.findBySku("ELEC-003").isEmpty()) {
                Product cable = new Product();
                cable.setName("USB-C Cable");
                cable.setDescription("Braided USB-C to USB-A cable, 2 meters.");
                cable.setPrice(new BigDecimal("12.99"));
                cable.setStockQuantity(200);
                cable.setSku("ELEC-003");
                cable.setCategory("Accessories");
                productRepository.save(cable);
            }
        };
    }
}
