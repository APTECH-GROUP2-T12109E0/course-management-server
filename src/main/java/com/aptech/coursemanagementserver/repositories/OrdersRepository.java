package com.aptech.coursemanagementserver.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import com.aptech.coursemanagementserver.models.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @Query(value = """
            SELECT [id], [created_at], [description],
            [duration], [image], [name], [net_price],
            [payment], [price], [status], [transaction_id],
            [updated_at], [user_description], [course_id], [user_id]
            FROM orders
            WHERE status = 'COMPLETED'
            AND user_id = :userId
            ORDER BY created_at DESC
                        """, nativeQuery = true)
    Page<Orders> findByUserId(long userId, Pageable pageable);

    @Query(value = """
            SELECT [id], [created_at], [description],
            [duration], [image], [name], [net_price],
            [payment], [price], [status], [transaction_id],
            [updated_at], [user_description], [course_id], [user_id]
            FROM orders
            WHERE user_id = :userId
            ORDER BY created_at DESC
                        """, nativeQuery = true)
    Page<Orders> findInCompletedByUserId(long userId, Pageable pageable);

    Optional<Orders> findByTransactionId(String transactionId);

    @Query(value = """
            SELECT TOP 1 o.*
            FROM orders AS o
            INNER JOIN users AS u ON o.user_id = u.id
            WHERE o.status = 'COMPLETED'
            AND o.course_id = :courseId
            AND o.user_id = :userId
            """, nativeQuery = true)
    Orders findFirstCompletedOrderWithCourse(long userId, long courseId);
}
