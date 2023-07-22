package com.aptech.coursemanagementserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.models.Roles;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    @Query(value = """
            SELECT r.* FROM roles r
            WHERE r.name <> 'ADMIN'
                      """, nativeQuery = true)
    List<Roles> findAllRoleExceptRoleADMIN();

    @Query(value = """
            SELECT r.* FROM roles r
            WHERE r.name IN ('MANAGER', 'EMPLOYEE')
                      """, nativeQuery = true)
    List<Roles> findManagerAndEmployeeRole();
}
