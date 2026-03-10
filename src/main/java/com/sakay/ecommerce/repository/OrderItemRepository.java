package com.sakay.ecommerce.repository;

import com.sakay.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi.productId, SUM(oi.qty) FROM OrderItem oi GROUP BY oi.productId")
    List<Object[]> sumQtyByProduct();
}
