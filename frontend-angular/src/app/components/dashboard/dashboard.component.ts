import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestService, Request } from '../../services/request.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  user: any = null;
  openRequests: Request[] = [];
  assignedRequests: Request[] = [];
  myRequests: Request[] = [];
  
  // Displayed requests (limited to 6)
  displayedOpenRequests: Request[] = [];
  displayedAssignedRequests: Request[] = [];
  displayedMyRequests: Request[] = [];
  
  isLoading = false;
  errorMessage = '';
  
  // Chart data
  statusChartData: { status: string; count: number; percentage: number }[] = [];
  priorityChartData: { priority: string; count: number; percentage: number }[] = [];
  
  readonly MAX_DISPLAY = 6;

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

    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading = true;
    this.errorMessage = '';

    // Load all requests for chart data
    this.requestService.getRequests(this.user.ntid).subscribe({
      next: (requests) => {
        this.prepareChartData(requests);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading requests:', error);
        this.errorMessage = 'Failed to load requests';
        this.isLoading = false;
      }
    });

    // Load "All Open Tickets" (OPEN status) - visible to all
    this.requestService.getRequests(this.user.ntid, { status: 'OPEN' }).subscribe({
      next: (requests) => {
        this.openRequests = requests;
        this.displayedOpenRequests = requests.slice(0, this.MAX_DISPLAY);
      },
      error: (error) => {
        console.error('Error loading open requests:', error);
      }
    });

    // Load "ASSIGNED Tickets" (visible to all users)
    // For SCRUM_MASTER/ADMIN: show all assigned tickets
    // For others: show only tickets assigned to them
    this.requestService.getRequests(this.user.ntid, { status: 'ASSIGNED' }).subscribe({
      next: (requests) => {
        if (this.user.role === 'SCRUM_MASTER' || this.user.role === 'ADMIN') {
          // SCRUM_MASTER and ADMIN see all assigned tickets
          this.assignedRequests = requests;
        } else {
          // Other users see only tickets assigned to them
          this.assignedRequests = requests.filter(r => {
            if (!r.assignedTo) return false;
            return r.assignedTo.trim().toLowerCase() === this.user.ntid.trim().toLowerCase();
          });
        }
        this.displayedAssignedRequests = this.assignedRequests.slice(0, this.MAX_DISPLAY);
      },
      error: (error) => {
        console.error('Error loading assigned requests:', error);
      }
    });

    // Load "My Tickets" (created by current user)
    this.requestService.getRequests(this.user.ntid).subscribe({
      next: (requests) => {
        // Filter to only show tickets created by this user (case-insensitive)
        this.myRequests = requests.filter(r => {
          if (!r.createdBy) return false;
          return r.createdBy.trim().toLowerCase() === this.user.ntid.trim().toLowerCase();
        });
        this.displayedMyRequests = this.myRequests.slice(0, this.MAX_DISPLAY);
        console.log('My Tickets (created by user):', this.myRequests.length, 'tickets');
      },
      error: (error) => {
        console.error('Error loading raised tickets:', error);
      }
    });
  }


  prepareChartData(requests: Request[]) {
    // Prepare status distribution
    const statusCounts: { [key: string]: number } = {};
    const priorityCounts: { [key: string]: number } = {};
    
    requests.forEach(request => {
      statusCounts[request.status] = (statusCounts[request.status] || 0) + 1;
      priorityCounts[request.priority] = (priorityCounts[request.priority] || 0) + 1;
    });

    const total = requests.length;
    
    this.statusChartData = Object.entries(statusCounts)
      .map(([status, count]) => ({
        status,
        count,
        percentage: total > 0 ? (count / total) * 100 : 0
      }))
      .sort((a, b) => b.count - a.count);

    this.priorityChartData = Object.entries(priorityCounts)
      .map(([priority, count]) => ({
        priority,
        count,
        percentage: total > 0 ? (count / total) * 100 : 0
      }))
      .sort((a, b) => {
        const priorityOrder: { [key: string]: number } = {
          'URGENT': 4,
          'HIGH': 3,
          'MEDIUM': 2,
          'LOW': 1
        };
        return (priorityOrder[b.priority] || 0) - (priorityOrder[a.priority] || 0);
      });
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

  viewRequest(requestId: number) {
    this.router.navigate(['/dashboard/request', requestId]);
  }

  createRequest() {
    this.router.navigate(['/dashboard/create']);
  }

  refresh() {
    this.loadDashboardData();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateToUserManagement() {
    this.router.navigate(['/dashboard/users']);
  }

  navigateToStatistics() {
    this.router.navigate(['/dashboard/statistics']);
  }

  navigateToAllTickets() {
    this.router.navigate(['/dashboard/tickets/all']);
  }

  viewAllTickets(type: string) {
    this.router.navigate(['/dashboard/tickets', type]);
  }

  hasMoreTickets(type: string): boolean {
    switch (type) {
      case 'open':
        return this.openRequests.length > this.MAX_DISPLAY;
      case 'assigned':
        return this.assignedRequests.length > this.MAX_DISPLAY;
      case 'my':
        return this.myRequests.length > this.MAX_DISPLAY;
      default:
        return false;
    }
  }
}
