package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);

    @Query("""
            select x from Customer x
            where :keyword is null
               or lower(x.code) like lower(concat('%', :keyword, '%'))
               or lower(x.name) like lower(concat('%', :keyword, '%'))
               or x.phone like concat('%', :keyword, '%')
            """)
    Page<Customer> search(@Param("keyword") String keyword, Pageable pageable);
}
