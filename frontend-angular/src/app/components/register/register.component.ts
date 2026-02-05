import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, Account } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  registerData = {
    ntid: '',
    email: '',
    account: '',
    password: '',
    confirmPassword: ''
  };
  
  accounts: Account[] = [];
  errorMessage = '';
  successMessage = '';
  isLoading = false;
  isLoadingAccounts = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.isLoadingAccounts = true;
    this.errorMessage = ''; // Clear previous errors
    
    // First try the test endpoint to verify API is working
    this.authService.testAccounts().subscribe({
      next: (testResponse) => {
        console.log('Accounts API test successful:', testResponse);
        
        // Now load the actual accounts
        this.authService.getActiveAccounts().subscribe({
          next: (accounts) => {
            console.log('Loaded accounts:', accounts);
            this.accounts = accounts;
            this.isLoadingAccounts = false;
            if (accounts.length === 0) {
              this.errorMessage = 'No accounts available. Please contact administrator.';
            }
          },
          error: (error) => {
            console.error('Failed to load accounts:', error);
            this.isLoadingAccounts = false;
            
            // Try alternative endpoint
            this.authService.getAllAccounts().subscribe({
              next: (accounts) => {
                console.log('Loaded accounts from alternative endpoint:', accounts);
                this.accounts = accounts;
                this.isLoadingAccounts = false;
                if (accounts.length === 0) {
                  this.errorMessage = 'No accounts available. Please contact administrator.';
                }
              },
              error: (altError) => {
                console.error('Alternative endpoint also failed:', altError);
                this.isLoadingAccounts = false;
                this.errorMessage = `Failed to load accounts: ${error.error?.error || error.message || 'Unknown error'}. Please refresh the page or contact administrator.`;
              }
            });
          }
        });
      },
      error: (testError) => {
        console.error('Accounts API test failed:', testError);
        // Still try to load accounts even if test fails
        this.authService.getActiveAccounts().subscribe({
          next: (accounts) => {
            this.accounts = accounts;
            this.isLoadingAccounts = false;
          },
          error: (error) => {
            console.error('Failed to load accounts:', error);
            this.isLoadingAccounts = false;
            this.errorMessage = `Failed to load accounts: ${error.error?.error || error.message || 'Unknown error'}. Please check if backend is running on http://localhost:8081`;
          }
        });
      }
    });
  }

  onSubmit() {
    if (!this.registerData.ntid || !this.registerData.email || !this.registerData.password || !this.registerData.account) {
      this.errorMessage = 'NTID, Email, Password, and Account are required';
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registerData.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    // Validate password match
    if (this.registerData.password !== this.registerData.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    // Validate password length
    if (this.registerData.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Prepare request payload (account is now mandatory)
    const payload = {
      ntid: this.registerData.ntid,
      email: this.registerData.email,
      password: this.registerData.password,
      account: this.registerData.account
    };

    this.authService.register(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Registration successful! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}
