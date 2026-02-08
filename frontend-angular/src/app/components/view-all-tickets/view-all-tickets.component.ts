import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestService, Request } from '../../services/request.service';

@Component({
  selector: 'app-view-all-tickets',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './view-all-tickets.component.html',
  styleUrl: './view-all-tickets.component.scss'
})
export class ViewAllTicketsComponent implements OnInit {
  user: any = null;
  tickets: Request[] = [];
  sectionType: string = ''; // 'open', 'assigned', 'my'
  sectionTitle: string = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private requestService: RequestService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.user = this.authService.getUser();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    // Get section type from route
    this.route.params.subscribe(params => {
      this.sectionType = params['type'] || 'open';
      this.loadTickets();
    });
  }

  loadTickets() {
    this.isLoading = true;
    this.errorMessage = '';

    switch (this.sectionType) {
      case 'open':
        this.sectionTitle = 'All Open Tickets';
        this.requestService.getRequests(this.user.ntid, { status: 'OPEN' }).subscribe({
          next: (requests) => {
            this.tickets = requests;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading open tickets:', error);
            this.errorMessage = 'Failed to load open tickets';
            this.isLoading = false;
          }
        });
        break;

      case 'assigned':
        this.sectionTitle = 'All Assigned Tickets';
        this.requestService.getRequests(this.user.ntid, { status: 'ASSIGNED' }).subscribe({
          next: (requests) => {
            if (this.user.role === 'SCRUM_MASTER' || this.user.role === 'ADMIN') {
              this.tickets = requests;
            } else {
              this.tickets = requests.filter(r => {
                if (!r.assignedTo) return false;
                return r.assignedTo.trim().toLowerCase() === this.user.ntid.trim().toLowerCase();
              });
            }
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading assigned tickets:', error);
            this.errorMessage = 'Failed to load assigned tickets';
            this.isLoading = false;
          }
        });
        break;

      case 'my':
        this.sectionTitle = 'All My Tickets (Raised by Me)';
        this.requestService.getRequests(this.user.ntid).subscribe({
          next: (requests) => {
            this.tickets = requests.filter(r => {
              if (!r.createdBy) return false;
              return r.createdBy.trim().toLowerCase() === this.user.ntid.trim().toLowerCase();
            });
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading my tickets:', error);
            this.errorMessage = 'Failed to load my tickets';
            this.isLoading = false;
          }
        });
        break;

      case 'all':
        this.sectionTitle = 'All Tickets';
        // Get all tickets - no status filter
        this.requestService.getRequests(this.user.ntid).subscribe({
          next: (requests) => {
            this.tickets = requests;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading all tickets:', error);
            this.errorMessage = 'Failed to load tickets';
            this.isLoading = false;
          }
        });
        break;

      default:
        this.errorMessage = 'Invalid section type';
        this.isLoading = false;
    }
  }

  viewRequest(requestId: number) {
    this.router.navigate(['/dashboard/request', requestId]);
  }

  goBack() {
    this.router.navigate(['/dashboard']);
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
