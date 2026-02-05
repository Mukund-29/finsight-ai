package com.finsight.repository;

import com.finsight.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User Account Repository
 * 
 * @author Mukund Kute
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    
    /**
     * Find all accounts handled by a user
     */
    List<UserAccount> findByNtidAndActiveTrue(String ntid);
    
    /**
     * Find all users handling an account
     */
    List<UserAccount> findByAccountIdAndActiveTrue(Long accountId);
    
    /**
     * Check if user handles specific account
     */
    boolean existsByNtidAndAccountIdAndActiveTrue(String ntid, Long accountId);
    
    /**
     * Get account IDs handled by user
     */
    @Query("SELECT ua.accountId FROM UserAccount ua WHERE ua.ntid = :ntid AND ua.active = true")
    List<Long> findAccountIdsByNtid(@Param("ntid") String ntid);
    
    /**
     * Delete (soft delete) account assignment
     */
    @Query("UPDATE UserAccount ua SET ua.active = false WHERE ua.ntid = :ntid AND ua.accountId = :accountId")
    void deactivateByNtidAndAccountId(@Param("ntid") String ntid, @Param("accountId") Long accountId);
}
