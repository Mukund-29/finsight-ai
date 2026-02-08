import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestService, Request } from '../../services/request.service';

export interface UserTicketStats {
  ntid: string;
  email: string;
  role: string;
  totalTickets: number;
  resolvedTickets: number;
  pendingTickets: number;
  onHoldTickets: number;
  unresolvedTickets: number;
  crossedEtaTickets: number;
}

export interface AccountStats {
  accountId: number;
  accountName: string;
  openTickets: number;
  totalTickets: number;
  resolvedTickets: number;
  pendingTickets: number;
  onHoldTickets: number;
  crossedEtaTickets: number;
}

@Component({
  selector: 'app-user-statistics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-statistics.component.html',
  styleUrl: './user-statistics.component.scss'
})
export class UserStatisticsComponent implements OnInit {
  user: any = null;
  accountStats: AccountStats[] = [];
  userStats: UserTicketStats[] = [];
  selectedAccountId: number | null = null;
  selectedAccountName: string = '';
  viewMode: 'accounts' | 'users' | 'tickets' = 'accounts';
  isLoading = false;
  errorMessage = '';
  
  // Tickets view properties
  filteredTickets: Request[] = [];
  selectedUserNtid: string = '';
  selectedUserEmail: string = '';
  filterType: 'total' | 'resolved' | 'pending' | 'onHold' | 'crossedEta' | null = null;
  filterTitle: string = '';

  constructor(
    private authService: AuthService,
    private requestService: RequestService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.getUser();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    // All users can view stats - no permission check
    this.loadAccountStatistics();
  }

  loadAccountStatistics() {
    this.isLoading = true;
    this.errorMessage = '';
    this.viewMode = 'accounts';
    this.selectedAccountId = null;
    this.selectedAccountName = '';

    this.requestService.getAccountStatistics(this.user.ntid).subscribe({
      next: (stats) => {
        console.log('Account statistics received:', stats);
        // Log first account to verify all fields are present
        if (stats && stats.length > 0) {
          console.log('First account stats:', stats[0]);
          console.log('Total tickets:', stats[0].totalTickets);
          console.log('Resolved:', stats[0].resolvedTickets);
          console.log('Pending (In Progress):', stats[0].pendingTickets);
          console.log('On Hold:', stats[0].onHoldTickets);
          console.log('Crossed ETA:', stats[0].crossedEtaTickets);
        }
        this.accountStats = stats;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading account statistics:', error);
        this.errorMessage = error.error?.error || 'Failed to load account statistics';
        this.isLoading = false;
      }
    });
  }

  viewAccountUsers(account: AccountStats) {
    if (!account || !account.accountId) {
      console.error('Invalid account data:', account);
      this.errorMessage = 'Invalid account selected';
      return;
    }

    this.selectedAccountId = account.accountId;
    this.selectedAccountName = account.accountName;
    this.viewMode = 'users';
    this.isLoading = true;
    this.errorMessage = '';

    console.log('Loading user statistics for account:', account.accountId, account.accountName);

    this.requestService.getUserStatisticsByAccount(account.accountId, this.user.ntid).subscribe({
      next: (stats) => {
        console.log('User statistics received:', stats);
        this.userStats = stats;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading user statistics by account:', error);
        console.error('Error details:', {
          status: error.status,
          statusText: error.statusText,
          error: error.error,
          message: error.message
        });
        this.errorMessage = error.error?.error || error.message || 'Failed to load user statistics';
        this.isLoading = false;
      }
    });
  }

  getRoleLabel(role: string): string {
    const roleMap: { [key: string]: string } = {
      'USER': 'User',
      'DEVELOPER': 'Developer',
      'MANAGER': 'Manager',
      'SCRUM_MASTER': 'Scrum Master',
      'ADMIN': 'Admin'
    };
    return roleMap[role] || role;
  }

  getRoleClass(role: string): string {
    return 'role-' + role.toLowerCase();
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  refresh() {
    if (this.viewMode === 'accounts') {
      this.loadAccountStatistics();
    } else if (this.viewMode === 'tickets') {
      // Refresh tickets view - find the user stat and reload tickets
      const userStat = this.userStats.find(u => u.ntid === this.selectedUserNtid);
      if (userStat && this.filterType) {
        const event = new Event('refresh');
        this.viewTickets(userStat, this.filterType, event);
      } else {
        // If user stat not found, go back to users view
        this.goBackToUsers();
      }
    } else if (this.selectedAccountId) {
      // Find the account from accountStats array
      const account = this.accountStats.find(a => a.accountId === this.selectedAccountId);
      if (account) {
        this.viewAccountUsers(account);
      } else {
        // If account not found, reload account statistics first
        this.loadAccountStatistics();
      }
    }
  }

  goBackToAccounts() {
    this.loadAccountStatistics();
  }

  viewTickets(userStat: UserTicketStats, filterType: 'total' | 'resolved' | 'pending' | 'onHold' | 'crossedEta', event: Event) {
    event.stopPropagation();
    
    this.selectedUserNtid = userStat.ntid;
    this.selectedUserEmail = userStat.email;
    this.filterType = filterType;
    this.viewMode = 'tickets';
    this.isLoading = true;
    this.errorMessage = '';

    // Set filter title
    const filterTitles: { [key: string]: string } = {
      'total': 'All Assigned Tickets',
      'resolved': 'Resolved Tickets',
      'pending': 'In Progress Tickets',
      'onHold': 'On Hold Tickets',
      'crossedEta': 'Crossed ETA Tickets'
    };
    this.filterTitle = filterTitles[filterType] || 'Tickets';

    // Fetch all tickets assigned to this user
    this.requestService.getRequests(this.user.ntid, {
      accountId: this.selectedAccountId || undefined
    }).subscribe({
      next: (allTickets) => {
        // Filter tickets assigned to the selected user
        let userTickets = allTickets.filter(ticket => {
          if (!ticket.assignedTo) return false;
          return ticket.assignedTo.trim().toLowerCase() === userStat.ntid.trim().toLowerCase();
        });

        // Apply additional filters based on filterType
        if (filterType === 'resolved') {
          userTickets = userTickets.filter(t => t.status === 'COMPLETED');
        } else if (filterType === 'pending') {
          userTickets = userTickets.filter(t => t.status === 'ASSIGNED' || t.status === 'IN_PROGRESS');
        } else if (filterType === 'onHold') {
          userTickets = userTickets.filter(t => t.status === 'ON_HOLD');
        } else if (filterType === 'crossedEta') {
          const now = new Date();
          userTickets = userTickets.filter(t => {
            if (!t.eta) return false;
            const etaDate = new Date(t.eta);
            // ETA crossed if: completed after ETA, or ETA passed and not completed
            if (t.status === 'COMPLETED' && t.updatedAt) {
              const updatedDate = new Date(t.updatedAt);
              return updatedDate > etaDate;
            } else if (t.status !== 'COMPLETED' && t.status !== 'CANCELLED') {
              return etaDate < now;
            }
            return false;
          });
        }
        // 'total' shows all tickets, no additional filtering needed

        this.filteredTickets = userTickets;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tickets:', error);
        this.errorMessage = error.error?.error || 'Failed to load tickets';
        this.isLoading = false;
      }
    });
  }

  viewRequest(requestId: number) {
    this.router.navigate(['/dashboard/request', requestId]);
  }

  goBackToUsers() {
    this.viewMode = 'users';
    this.filteredTickets = [];
    this.filterType = null;
  }

  getPriorityClass(priority: string): string {
    const classes: { [key: string]: string } = {
      'URGENT': 'priority-urgent',
      'HIGH': 'priority-high',
      'MEDIUM': 'priority-medium',
      'LOW': 'priority-low'
    };
    return classes[priority] || 'priority-medium';
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'OPEN': 'status-open',
      'ASSIGNED': 'status-assigned',
      'IN_PROGRESS': 'status-in-progress',
      'ON_HOLD': 'status-on-hold',
      'COMPLETED': 'status-completed',
      'DELAYED': 'status-delayed',
      'CANCELLED': 'status-cancelled'
    };
    return classes[status] || 'status-open';
  }
}
