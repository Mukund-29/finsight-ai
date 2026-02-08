import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, Account } from '../../services/auth.service';
import { RequestService } from '../../services/request.service';
import { CanComponentDeactivate } from '../../guards/can-deactivate.guard';

@Component({
  selector: 'app-create-request',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './create-request.component.html',
  styleUrl: './create-request.component.scss'
})
export class CreateRequestComponent implements OnInit, CanComponentDeactivate {
  user: any = null;
  accounts: Account[] = [];
  
  requestData = {
    title: '',
    description: '',
    requestType: 'ADHOC',
    priority: 'MEDIUM',
    accountId: null as number | null
  };

  requestTypes = [
    { value: 'TOOL_ENHANCEMENT', label: 'Tool Enhancement' },
    { value: 'ADHOC', label: 'Ad-hoc' },
    { value: 'BUG_FIX', label: 'Bug Fix' },
    { value: 'FEATURE_REQUEST', label: 'Feature Request' },
    { value: 'OTHER', label: 'Other' }
  ];

  priorities = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' }
  ];

  isLoading = false;
  isLoadingAccounts = false;
  errorMessage = '';
  successMessage = '';

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

    // Set default account from user
    if (this.user.accountId) {
      this.requestData.accountId = this.user.accountId;
    }

    this.loadAccounts();
  }

  loadAccounts() {
    this.isLoadingAccounts = true;
    this.authService.getActiveAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.isLoadingAccounts = false;
      },
      error: (error) => {
        console.error('Failed to load accounts:', error);
        this.isLoadingAccounts = false;
      }
    });
  }

  onSubmit() {
    // Prevent multiple clicks
    if (this.isLoading) {
      return;
    }

    if (!this.requestData.title.trim()) {
      this.errorMessage = 'Title is required';
      return;
    }

    if (!this.requestData.accountId) {
      this.errorMessage = 'Account is required';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      title: this.requestData.title,
      description: this.requestData.description || undefined,
      requestType: this.requestData.requestType,
      priority: this.requestData.priority,
      accountId: this.requestData.accountId
    };

    this.requestService.createRequest(payload, this.user.ntid).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Request created successfully!';
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Failed to create request. Please try again.';
        console.error('Create request error:', error);
      }
    });
  }

  cancel() {
    // Prevent navigation if request is processing
    if (this.isLoading) {
      return;
    }
    this.router.navigate(['/dashboard']);
  }

  canDeactivate(): boolean {
    // Allow navigation if form is not dirty or request is being processed
    if (this.isLoading) {
      return false; // Prevent navigation during submission
    }
    
    // Check if form has unsaved changes
    const hasChanges = this.requestData.title.trim() || 
                      this.requestData.description.trim() || 
                      this.requestData.accountId !== null;
    
    if (hasChanges && !this.successMessage) {
      return confirm('You have unsaved changes. Are you sure you want to leave?');
    }
    
    return true;
  }
}
