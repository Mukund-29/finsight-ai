import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestService, Request, UpdateStatus } from '../../services/request.service';

@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './request-detail.component.html',
  styleUrl: './request-detail.component.scss'
})
export class RequestDetailComponent implements OnInit {
  user: any = null;
  request: Request | null = null;
  requestId: number = 0;
  isLoading = false;
  errorMessage = '';
  
  // Status update
  showStatusUpdate = false;
  statusUpdate = {
    status: '',
    comment: ''
  };
  
  availableStatuses = [
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'ON_HOLD', label: 'On Hold' },
    { value: 'DELAYED', label: 'Delayed' }
  ];

  // Assignment
  showAssignModal = false;
  assignableUsers: any[] = [];
  isLoadingUsers = false;
  assignmentData = {
    assignedTo: '',
    etaDate: '',
    etaTime: '',
    eta: '' // Combined datetime
  };
  showEtaPicker = false;

  constructor(
    private authService: AuthService,
    private requestService: RequestService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.getUser();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    this.route.params.subscribe(params => {
      this.requestId = +params['id'];
      this.loadRequest();
    });
  }

  loadRequest() {
    this.isLoading = true;
    this.errorMessage = '';

    this.requestService.getRequestById(this.requestId, this.user.ntid).subscribe({
      next: (request) => {
        this.request = request;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading request:', error);
        this.errorMessage = error.error?.error || 'Failed to load request';
        this.isLoading = false;
      }
    });
  }

  canUpdateStatus(): boolean {
    if (!this.request) return false;
    
    // Cannot update status of OPEN tickets (must be assigned first)
    if (this.request.status === 'OPEN') {
      return false;
    }
    
    const role = this.user?.role;
    
    // ADMIN can always update status (except OPEN)
    if (role === 'ADMIN') {
      return true;
    }
    
    // Assigned user can update status (regardless of role)
    // Backend will validate the status transition
    if (this.request.assignedTo === this.user.ntid) {
      return true;
    }
    
    // SCRUM_MASTER can update status for tickets in their accounts (handled by backend)
    if (role === 'SCRUM_MASTER') {
      return true; // Backend will validate account access
    }
    
    return false;
  }

  canAssignRequest(): boolean {
    if (!this.request) return false;
    const role = this.user?.role;
    // Only SCRUM_MASTER or ADMIN can assign
    // Can only assign OPEN tickets
    return (role === 'SCRUM_MASTER' || role === 'ADMIN') && 
           this.request.status === 'OPEN';
  }

  canDeleteRequest(): boolean {
    if (!this.request) return false;
    const role = this.user?.role;
    
    // Only creator, SCRUM_MASTER, or ADMIN can delete tickets (regardless of status)
    return this.request.createdBy === this.user.ntid || 
           role === 'SCRUM_MASTER' || 
           role === 'ADMIN';
  }

  showAssignForm() {
    this.errorMessage = '';
    this.assignmentData = { assignedTo: '', etaDate: '', etaTime: '', eta: '' };
    this.showEtaPicker = false;
    this.loadAssignableUsers();
    this.showAssignModal = true;
  }

  loadAssignableUsers() {
    this.isLoadingUsers = true;
    this.requestService.getAssignableUsers(this.user.ntid).subscribe({
      next: (users) => {
        this.assignableUsers = users;
        this.isLoadingUsers = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.isLoadingUsers = false;
        this.errorMessage = 'Failed to load users for assignment';
      }
    });
  }

  setEta() {
    // Combine date and time into ISO format
    if (this.assignmentData.etaDate && this.assignmentData.etaTime) {
      const dateTime = new Date(`${this.assignmentData.etaDate}T${this.assignmentData.etaTime}`);
      this.assignmentData.eta = dateTime.toISOString();
      this.showEtaPicker = false;
    } else {
      this.errorMessage = 'Please select both date and time';
    }
  }

  assignRequest() {
    if (!this.assignmentData.assignedTo) {
      this.errorMessage = 'Please select a user to assign';
      return;
    }

    if (!this.assignmentData.eta) {
      this.errorMessage = 'Please set an ETA';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const assignData = {
      assignedTo: this.assignmentData.assignedTo,
      eta: this.assignmentData.eta
    };

    this.requestService.assignRequest(this.requestId, assignData, this.user.ntid).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.showAssignModal = false;
        this.assignmentData = { assignedTo: '', etaDate: '', etaTime: '', eta: '' };
        this.loadRequest(); // Reload to get updated data
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to assign request';
        console.error('Assign request error:', error);
      }
    });
  }

  deleteRequest() {
    if (!confirm('Are you sure you want to delete this request? This action cannot be undone.')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.requestService.deleteRequest(this.requestId, this.user.ntid).subscribe({
      next: (response) => {
        this.isLoading = false;
        alert('Request deleted successfully. You can now create a new request if needed.');
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to delete request';
        console.error('Delete request error:', error);
      }
    });
  }

  showStatusUpdateForm() {
    this.showStatusUpdate = true;
    if (this.request) {
      this.statusUpdate.status = this.request.status;
    }
  }

  updateStatus() {
    if (!this.statusUpdate.status) {
      this.errorMessage = 'Please select a status';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const updateData: UpdateStatus = {
      status: this.statusUpdate.status,
      comment: this.statusUpdate.comment || undefined
    };

    this.requestService.updateStatus(this.requestId, updateData, this.user.ntid).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.showStatusUpdate = false;
        this.statusUpdate = { status: '', comment: '' };
        this.loadRequest(); // Reload to get updated data
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to update status';
        console.error('Update status error:', error);
      }
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

  goBack() {
    // Prevent navigation if request is processing
    if (this.isLoading) {
      return;
    }
    this.router.navigate(['/dashboard']);
  }

  getMinDate(): string {
    // Set minimum date to today
    const today = new Date();
    return today.toISOString().split('T')[0];
  }
}
