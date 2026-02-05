package com.finsight.repository;

import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * 
 * @author Mukund Kute
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByNtid(String ntid);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByNtid(String ntid);
    
    boolean existsByEmail(String email);
    
    // Find all active users
    List<User> findByActiveTrueOrderByNtidAsc();
    
    // Find active users by role
    List<User> findByRoleAndActiveTrueOrderByNtidAsc(UserRole role);
}
