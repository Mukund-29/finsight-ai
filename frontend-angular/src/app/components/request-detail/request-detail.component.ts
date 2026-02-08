import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestService, Request, UpdateStatus, Comment, CreateComment } from '../../services/request.service';

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
  isAssigning = false;
  isUpdatingStatus = false;
  isDeleting = false;
  isUpdatingEta = false;
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

  // ETA Update
  showEtaUpdateForm = false;
  etaUpdateData = {
    etaDate: '',
    etaTime: '',
    newEta: '',
    changeReason: '',
    commentText: ''
  };

  // Comments
  comments: Comment[] = [];
  isLoadingComments = false;
  showAddCommentForm = false;
  newComment = {
    commentText: ''
  };
  isAddingComment = false;
  @ViewChild('commentForm') commentFormRef?: ElementRef;

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
        this.loadComments(); // Load comments after request is loaded
      },
      error: (error) => {
        console.error('Error loading request:', error);
        this.errorMessage = error.error?.error || 'Failed to load request';
        this.isLoading = false;
      }
    });
  }

  loadComments() {
    this.isLoadingComments = true;
    this.requestService.getComments(this.requestId, this.user.ntid).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoadingComments = false;
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.isLoadingComments = false;
        // Don't show error for comments, just log it
      }
    });
  }

  canAddComment(): boolean {
    if (!this.request) return false;
    const role = this.user?.role;
    
    // ADMIN can always comment
    if (role === 'ADMIN') {
      return true;
    }
    
    // SCRUM_MASTER can comment
    if (role === 'SCRUM_MASTER') {
      return true;
    }
    
    // Assigned user can comment
    if (this.request.assignedTo && this.request.assignedTo === this.user.ntid) {
      return true;
    }
    
    // Creator can comment
    if (this.request.createdBy === this.user.ntid) {
      return true;
    }
    
    return false;
  }

  showAddComment() {
    this.showAddCommentForm = true;
    this.newComment.commentText = '';
    
    // Scroll to comment form after a brief delay to ensure DOM is updated
    setTimeout(() => {
      if (this.commentFormRef) {
        this.commentFormRef.nativeElement.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        });
        // Focus on textarea after scrolling
        setTimeout(() => {
          const textarea = this.commentFormRef?.nativeElement.querySelector('textarea');
          if (textarea) {
            textarea.focus();
          }
        }, 300);
      }
    }, 100);
  }

  cancelAddComment() {
    this.showAddCommentForm = false;
    this.newComment.commentText = '';
  }

  addComment() {
    // Prevent multiple clicks
    if (this.isAddingComment || this.isLoading || this.isAssigning || this.isUpdatingStatus || this.isDeleting || this.isUpdatingEta) {
      return;
    }

    if (!this.newComment.commentText || this.newComment.commentText.trim() === '') {
      this.errorMessage = 'Please enter a comment';
      return;
    }

    this.isAddingComment = true;
    this.errorMessage = '';

    const createComment: CreateComment = {
      commentText: this.newComment.commentText.trim(),
      isEtaChange: false
    };

    this.requestService.addComment(this.requestId, createComment, this.user.ntid).subscribe({
      next: (comment) => {
        this.isAddingComment = false;
        this.showAddCommentForm = false;
        this.newComment.commentText = '';
        this.loadComments(); // Reload comments
      },
      error: (error) => {
        this.isAddingComment = false;
        this.errorMessage = error.error?.error || 'Failed to add comment';
        console.error('Add comment error:', error);
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

  canUpdateEta(): boolean {
    if (!this.request) return false;
    const role = this.user?.role;
    
    // ADMIN can always update ETA
    if (role === 'ADMIN') {
      return true;
    }
    
    // SCRUM_MASTER can update ETA
    if (role === 'SCRUM_MASTER') {
      return true;
    }
    
    // Assigned user can update ETA
    if (this.request.assignedTo && this.request.assignedTo === this.user.ntid) {
      return true;
    }
    
    return false;
  }

  showEtaUpdate() {
    // Prevent multiple clicks
    if (this.isLoading || this.isAssigning || this.isUpdatingStatus || this.isDeleting || this.isUpdatingEta) {
      return;
    }
    this.errorMessage = '';
    
    // Initialize with current ETA if exists
    if (this.request?.eta) {
      const etaDate = new Date(this.request.eta);
      this.etaUpdateData.etaDate = etaDate.toISOString().split('T')[0];
      this.etaUpdateData.etaTime = etaDate.toTimeString().split(' ')[0].substring(0, 5);
    } else {
      this.etaUpdateData.etaDate = '';
      this.etaUpdateData.etaTime = '';
    }
    this.etaUpdateData.newEta = '';
    this.etaUpdateData.changeReason = '';
    this.etaUpdateData.commentText = '';
    this.showEtaUpdateForm = true;
  }

  setNewEta() {
    // Combine date and time into ISO format
    if (this.etaUpdateData.etaDate && this.etaUpdateData.etaTime) {
      const dateTime = new Date(`${this.etaUpdateData.etaDate}T${this.etaUpdateData.etaTime}`);
      this.etaUpdateData.newEta = dateTime.toISOString();
    } else {
      this.errorMessage = 'Please select both date and time';
    }
  }

  updateEta() {
    // Prevent multiple clicks
    if (this.isUpdatingEta || this.isLoading || this.isAssigning || this.isUpdatingStatus || this.isDeleting) {
      return;
    }

    // Validate required fields
    if (!this.etaUpdateData.etaDate || !this.etaUpdateData.etaTime) {
      this.errorMessage = 'Please select both date and time for the new ETA';
      return;
    }

    if (!this.etaUpdateData.changeReason || this.etaUpdateData.changeReason.trim() === '') {
      this.errorMessage = 'Reason for ETA change is required';
      return;
    }

    // Set the new ETA
    this.setNewEta();
    if (!this.etaUpdateData.newEta) {
      return;
    }

    this.isUpdatingEta = true;
    this.isLoading = true;
    this.errorMessage = '';

    const updateEtaDTO = {
      newEta: this.etaUpdateData.newEta,
      changeReason: this.etaUpdateData.changeReason.trim(),
      commentText: this.etaUpdateData.commentText?.trim() || ''
    };

    this.requestService.updateEta(this.requestId, updateEtaDTO, this.user.ntid).subscribe({
      next: (response) => {
        this.isUpdatingEta = false;
        this.isLoading = false;
        this.showEtaUpdateForm = false;
        this.etaUpdateData = { etaDate: '', etaTime: '', newEta: '', changeReason: '', commentText: '' };
        this.loadRequest(); // Reload to get updated data
        this.loadComments(); // Reload comments to show ETA change comment
      },
      error: (error) => {
        this.isUpdatingEta = false;
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to update ETA';
        console.error('Update ETA error:', error);
      }
    });
  }

  cancelEtaUpdate() {
    this.showEtaUpdateForm = false;
    this.etaUpdateData = { etaDate: '', etaTime: '', newEta: '', changeReason: '', commentText: '' };
    this.errorMessage = '';
  }

  showAssignForm() {
    // Prevent multiple clicks
    if (this.isLoading || this.isAssigning || this.isUpdatingStatus || this.isDeleting) {
      return;
    }
    this.errorMessage = '';
    this.assignmentData = { assignedTo: '', etaDate: '', etaTime: '', eta: '' };
    this.showEtaPicker = false;
    this.loadAssignableUsers();
    this.showAssignModal = true;
  }

  loadAssignableUsers() {
    this.isLoadingUsers = true;
    
    // Get accountId and createdBy from the request to filter users
    const accountId = this.request?.accountId;
    const createdByNtid = this.request?.createdBy;
    
    this.requestService.getAssignableUsers(
      this.user.ntid,
      undefined, // role filter (optional)
      accountId, // filter by ticket's accountId
      createdByNtid // include the user who created the ticket
    ).subscribe({
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
    // Prevent multiple clicks
    if (this.isAssigning || this.isLoading || this.isUpdatingStatus || this.isDeleting) {
      return;
    }

    if (!this.assignmentData.assignedTo) {
      this.errorMessage = 'Please select a user to assign';
      return;
    }

    if (!this.assignmentData.eta) {
      this.errorMessage = 'Please set an ETA';
      return;
    }

    this.isAssigning = true;
    this.isLoading = true;
    this.errorMessage = '';

    const assignData = {
      assignedTo: this.assignmentData.assignedTo,
      eta: this.assignmentData.eta
    };

    this.requestService.assignRequest(this.requestId, assignData, this.user.ntid).subscribe({
      next: (response) => {
        this.isAssigning = false;
        this.isLoading = false;
        this.showAssignModal = false;
        this.assignmentData = { assignedTo: '', etaDate: '', etaTime: '', eta: '' };
        this.loadRequest(); // Reload to get updated data
      },
      error: (error) => {
        this.isAssigning = false;
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to assign request';
        console.error('Assign request error:', error);
      }
    });
  }

  deleteRequest() {
    // Prevent multiple clicks
    if (this.isDeleting || this.isLoading || this.isAssigning || this.isUpdatingStatus) {
      return;
    }

    if (!confirm('Are you sure you want to delete this request? This action cannot be undone.')) {
      return;
    }

    this.isDeleting = true;
    this.isLoading = true;
    this.errorMessage = '';

    this.requestService.deleteRequest(this.requestId, this.user.ntid).subscribe({
      next: (response) => {
        this.isDeleting = false;
        this.isLoading = false;
        alert('Request deleted successfully. You can now create a new request if needed.');
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isDeleting = false;
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
    // Prevent multiple clicks
    if (this.isUpdatingStatus || this.isLoading || this.isAssigning || this.isDeleting) {
      return;
    }

    if (!this.statusUpdate.status) {
      this.errorMessage = 'Please select a status';
      return;
    }

    this.isUpdatingStatus = true;
    this.isLoading = true;
    this.errorMessage = '';

    const updateData: UpdateStatus = {
      status: this.statusUpdate.status,
      comment: this.statusUpdate.comment || undefined
    };

    this.requestService.updateStatus(this.requestId, updateData, this.user.ntid).subscribe({
      next: (response) => {
        this.isUpdatingStatus = false;
        this.isLoading = false;
        this.showStatusUpdate = false;
        this.statusUpdate = { status: '', comment: '' };
        this.loadRequest(); // Reload to get updated data
      },
      error: (error) => {
        this.isUpdatingStatus = false;
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
    // Prevent navigation if any request is processing
    if (this.isLoading || this.isAssigning || this.isUpdatingStatus || this.isDeleting) {
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
