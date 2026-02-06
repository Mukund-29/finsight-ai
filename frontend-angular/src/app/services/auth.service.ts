import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  ntid: string;
  password: string;
}

export interface RegisterRequest {
  ntid: string;
  email: string;
  account?: string;
  password: string;
}

export interface Account {
  accountId: number;
  accountName: string;
}

export interface AuthResponse {
  ntid: string;
  email: string;
  role: string;
  token: string;
  message: string;
  accountId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  getCurrentUser(ntid: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`, {
      headers: new HttpHeaders({ 'X-User-NTID': ntid })
    });
  }

  getActiveAccounts(): Observable<Account[]> {
    return this.http.get<any>(`${environment.apiUrl}/accounts/active`, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    }).pipe(
      // Handle both array response and object with accounts property
      map((response: any) => {
        if (Array.isArray(response)) {
          return response;
        } else if (response.accounts && Array.isArray(response.accounts)) {
          return response.accounts;
        } else {
          return [];
        }
      })
    );
  }

  // Test endpoint to check if accounts API is working
  testAccounts(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/accounts/test`, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Alternative endpoint to get all accounts
  getAllAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${environment.apiUrl}/accounts/all`, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Store user data in localStorage
  setUser(user: AuthResponse): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    localStorage.setItem('token', user.token);
  }

  // Get user data from localStorage
  getUser(): AuthResponse | null {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  // Logout
  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
  }
}
