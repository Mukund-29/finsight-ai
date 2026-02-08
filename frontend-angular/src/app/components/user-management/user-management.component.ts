import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, Account } from '../../services/auth.service';
import { UserService } from '../../services/user.service';

export interface User {
  ntid: string;
  email: string;
  role: string;
  account: string;
  accountId: number;
  active: boolean;
  createdAt?: string;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  user: any = null;
  users: User[] = [];
  accounts: Account[] = [];
  
  isLoading = false;
  isLoadingUsers = false;
  isLoadingAccounts = false;
  errorMessage = '';
  successMessage = '';
  
  selectedUser: User | null = null;
  showEditModal = false;
  isUpdating = false;
  isDeleting = false;
  deletingUserId: string | null = null;
  isTogglingActive = false;
  togglingUserId: string | null = null;
  
  editForm = {
    ntid: '',
    email: '',
    account: '',
    accountId: null as number | null,
    role: '',
    active: true
  };
  
  roles = [
    { value: 'USER', label: 'User' },
    { value: 'DEVELOPER', label: 'Developer' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'SCRUM_MASTER', label: 'Scrum Master' },
    { value: 'ADMIN', label: 'Admin' }
  ];

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.getUser();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    // Check if user has permission (ADMIN or SCRUM_MASTER)
    if (this.user.role !== 'ADMIN' && this.user.role !== 'SCRUM_MASTER') {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadUsers();
    this.loadAccounts();
  }

  loadUsers() {
    this.isLoadingUsers = true;
    this.errorMessage = '';

    this.userService.getAllUsers(this.user.ntid).subscribe({
      next: (users) => {
        // Ensure active field is properly set (handle null/undefined or 0/1 from database)
        this.users = users.map(user => ({
          ...user,
          active: user.active !== null && user.active !== undefined 
            ? (typeof user.active === 'boolean' ? user.active : Boolean(user.active))
            : true // Default to true if not provided
        }));
        this.isLoadingUsers = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.errorMessage = error.error?.error || 'Failed to load users';
        this.isLoadingUsers = false;
      }
    });
  }

  loadAccounts() {
    this.isLoadingAccounts = true;
    this.authService.getActiveAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.isLoadingAccounts = false;
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
        this.isLoadingAccounts = false;
      }
    });
  }

  openEditModal(user: User) {
    this.selectedUser = user;
    this.editForm = {
      ntid: user.ntid,
      email: user.email,
      account: user.account || '',
      accountId: user.accountId || null,
      role: user.role,
      active: user.active // Keep for form but don't show in UI
    };
    this.showEditModal = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeEditModal() {
    this.showEditModal = false;
    this.selectedUser = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onAccountChange() {
    // When account name is selected, find the accountId
    if (this.editForm.account) {
      const account = this.accounts.find(a => a.accountName === this.editForm.account);
      if (account) {
        this.editForm.accountId = account.accountId;
      }
    }
  }

  updateUser() {
    // Prevent multiple clicks
    if (this.isUpdating || this.isLoading || this.isDeleting) {
      return;
    }

    if (!this.selectedUser) return;

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editForm.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    // Validate account is selected
    if (!this.editForm.account && !this.editForm.accountId) {
      this.errorMessage = 'Please select an account';
      return;
    }

    this.isUpdating = true;
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      ntid: this.editForm.ntid,
      email: this.editForm.email,
      account: this.editForm.account,
      accountId: this.editForm.accountId,
      role: this.editForm.role,
      active: this.editForm.active
    };

    this.userService.updateUser(this.selectedUser.ntid, updateData, this.user.ntid).subscribe({
      next: (response) => {
        this.isUpdating = false;
        this.isLoading = false;
        this.successMessage = 'User updated successfully!';
        this.loadUsers(); // Reload users list
        setTimeout(() => {
          this.closeEditModal();
        }, 1500);
      },
      error: (error) => {
        this.isUpdating = false;
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to update user';
        console.error('Update user error:', error);
      }
    });
  }

  deleteUser(user: User) {
    // Prevent multiple clicks
    if (this.isDeleting || this.isLoading || this.isUpdating) {
      return;
    }

    // Only ADMIN can delete users
    if (this.user.role !== 'ADMIN') {
      this.errorMessage = 'Only ADMIN can delete users';
      return;
    }

    // Prevent deleting yourself
    if (user.ntid.toLowerCase() === this.user.ntid.toLowerCase()) {
      this.errorMessage = 'You cannot delete your own account';
      return;
    }

    // Confirmation dialog
    if (!confirm(`Are you sure you want to delete user "${user.ntid}" (${user.email})? This action cannot be undone.`)) {
      return;
    }

    this.isDeleting = true;
    this.deletingUserId = user.ntid;
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.userService.deleteUser(user.ntid, this.user.ntid).subscribe({
      next: (response) => {
        this.isDeleting = false;
        this.deletingUserId = null;
        this.isLoading = false;
        this.successMessage = `User "${user.ntid}" deleted successfully!`;
        this.loadUsers(); // Reload users list
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.isDeleting = false;
        this.deletingUserId = null;
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to delete user';
        console.error('Delete user error:', error);
      }
    });
  }

  canDeleteUser(user: User): boolean {
    // Only ADMIN can delete users
    if (this.user?.role !== 'ADMIN') {
      return false;
    }
    // Cannot delete yourself
    if (user.ntid.toLowerCase() === this.user.ntid.toLowerCase()) {
      return false;
    }
    return true;
  }

  toggleUserActive(user: User, event: Event) {
    // Prevent multiple clicks
    if (this.isTogglingActive || this.isLoading || this.isUpdating || this.isDeleting) {
      event.preventDefault();
      return;
    }

    // Cannot deactivate yourself
    if (user.ntid.toLowerCase() === this.user.ntid.toLowerCase()) {
      event.preventDefault();
      this.errorMessage = 'You cannot deactivate your own account';
      return;
    }

    // Get the new state from the checkbox
    const checkbox = event.target as HTMLInputElement;
    const newActiveState = checkbox.checked;
    
    // If the state hasn't actually changed, do nothing
    if (user.active === newActiveState) {
      return;
    }

    // No confirmation needed for toggle switch - immediate action
    this.isTogglingActive = true;
    this.togglingUserId = user.ntid;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      active: newActiveState
    };

    this.userService.updateUser(user.ntid, updateData, this.user.ntid).subscribe({
      next: (response) => {
        this.isTogglingActive = false;
        this.togglingUserId = null;
        // Update local user object immediately
        user.active = newActiveState;
        this.successMessage = `User ${newActiveState ? 'activated' : 'deactivated'} successfully!`;
        setTimeout(() => {
          this.successMessage = '';
        }, 2000);
      },
      error: (error) => {
        this.isTogglingActive = false;
        this.togglingUserId = null;
        // Revert the checkbox state on error
        checkbox.checked = user.active;
        this.errorMessage = error.error?.error || 'Failed to update user status';
        console.error('Toggle user active error:', error);
      }
    });
  }

  getRoleLabel(role: string): string {
    const roleObj = this.roles.find(r => r.value === role);
    return roleObj ? roleObj.label : role;
  }

  goBack() {
    if (this.isLoading || this.isUpdating || this.isDeleting || this.isTogglingActive) {
      return;
    }
    this.router.navigate(['/dashboard']);
  }
}
