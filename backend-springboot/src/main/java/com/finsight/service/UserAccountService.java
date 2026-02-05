package com.finsight.service;

import com.finsight.dto.AssignAccountDTO;
import com.finsight.entity.User;
import com.finsight.entity.UserAccount;
import com.finsight.entity.UserRole;
import com.finsight.repository.AccountRepository;
import com.finsight.repository.UserAccountRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Account Service
 * 
 * @author Mukund Kute
 */
@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Assign account to user (only for SCRUM_MASTER role)
     */
    @Transactional
    public UserAccount assignAccountToUser(AssignAccountDTO assignDTO, String requestedBy) {
        System.out.println("  [UserAccountService] assignAccountToUser() called");
        System.out.println("  [UserAccountService] Assigning account " + assignDTO.getAccountId() + " to user " + assignDTO.getNtid());

        // Check requester permissions (only ADMIN can assign)
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found: " + requestedBy));

        if (requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can assign accounts to users");
        }

        // Verify user exists
        User user = userRepository.findByNtid(assignDTO.getNtid())
            .orElseThrow(() -> new RuntimeException("User not found: " + assignDTO.getNtid()));

        // Only SCRUM_MASTER can have account assignments
        if (user.getRole() != UserRole.SCRUM_MASTER) {
            throw new RuntimeException("Only SCRUM_MASTER role users can have account assignments");
        }

        // Verify account exists
        accountRepository.findById(assignDTO.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account not found: " + assignDTO.getAccountId()));

        // Check if assignment already exists
        if (userAccountRepository.existsByNtidAndAccountIdAndActiveTrue(assignDTO.getNtid(), assignDTO.getAccountId())) {
            throw new RuntimeException("Account is already assigned to this user");
        }

        // Create new assignment
        UserAccount userAccount = new UserAccount(assignDTO.getNtid(), assignDTO.getAccountId());
        return userAccountRepository.save(userAccount);
    }

    /**
     * Get all accounts handled by a user
     */
    public List<UserAccount> getAccountsByUser(String ntid) {
        return userAccountRepository.findByNtidAndActiveTrue(ntid);
    }

    /**
     * Get account IDs handled by user
     */
    public List<Long> getAccountIdsByUser(String ntid) {
        return userAccountRepository.findAccountIdsByNtid(ntid);
    }

    /**
     * Remove account assignment (soft delete)
     */
    @Transactional
    public void removeAccountAssignment(String ntid, Long accountId, String requestedBy) {
        System.out.println("  [UserAccountService] removeAccountAssignment() called");

        // Check requester permissions (only ADMIN can remove)
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found: " + requestedBy));

        if (requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can remove account assignments");
        }

        // Verify assignment exists
        UserAccount userAccount = userAccountRepository.findByNtidAndActiveTrue(ntid)
            .stream()
            .filter(ua -> ua.getAccountId().equals(accountId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Account assignment not found"));

        // Soft delete
        userAccount.setActive(false);
        userAccountRepository.save(userAccount);
    }

    /**
     * Get all users handling an account
     */
    public List<UserAccount> getUsersByAccount(Long accountId) {
        return userAccountRepository.findByAccountIdAndActiveTrue(accountId);
    }
}
