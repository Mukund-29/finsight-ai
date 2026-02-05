package com.finsight.repository;

import com.finsight.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Account Repository
 * 
 * @author Mukund Kute
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByActiveTrueOrderByAccountNameAsc();
    
    List<Account> findAllByOrderByAccountNameAsc();
    
    Optional<Account> findByAccountName(String accountName);
    
    boolean existsByAccountName(String accountName);
}
