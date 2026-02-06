import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Request {
  requestId: number;
  title: string;
  description?: string;
  requestType: string;
  priority: string;
  status: string;
  createdBy: string;
  assignedTo?: string;
  assignedBy?: string;
  accountId?: number;
  createdAt: string;
  updatedAt?: string;
  assignedAt?: string;
  eta?: string;
  timeInOpenQueue?: string;
  timeInDeveloperQueue?: string;
  timeUntilEta?: string;
  etaApproaching?: boolean;
  etaExceeded?: boolean;
}

export interface User {
  ntid: string;
  email: string;
  role: string;
  account?: string;
  accountId?: number;
}

export interface CreateRequest {
  title: string;
  description?: string;
  requestType: string;
  priority: string;
  accountId?: number;
}

export interface AssignRequest {
  assignedTo: string;
  eta: string;
}

export interface UpdateStatus {
  status: string;
  comment?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RequestService {
  private apiUrl = `${environment.apiUrl}/requests`;

  constructor(private http: HttpClient) {}

  private getHeaders(userNtid: string): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-NTID': userNtid
    });
  }

  createRequest(request: CreateRequest, userNtid: string): Observable<any> {
    return this.http.post(`${this.apiUrl}`, request, {
      headers: this.getHeaders(userNtid)
    });
  }

  getRequests(userNtid: string, filters?: {
    status?: string;
    priority?: string;
    requestType?: string;
    accountId?: number;
  }): Observable<Request[]> {
    let url = `${this.apiUrl}`;
    const params: string[] = [];
    
    if (filters) {
      if (filters.status) params.push(`status=${filters.status}`);
      if (filters.priority) params.push(`priority=${filters.priority}`);
      if (filters.requestType) params.push(`requestType=${filters.requestType}`);
      if (filters.accountId) params.push(`accountId=${filters.accountId}`);
    }
    
    if (params.length > 0) {
      url += '?' + params.join('&');
    }

    return this.http.get<Request[]>(url, {
      headers: this.getHeaders(userNtid)
    });
  }

  getRequestById(requestId: number, userNtid: string): Observable<Request> {
    return this.http.get<Request>(`${this.apiUrl}/${requestId}`, {
      headers: this.getHeaders(userNtid)
    });
  }

  updateRequest(requestId: number, request: Partial<CreateRequest>, userNtid: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${requestId}`, request, {
      headers: this.getHeaders(userNtid)
    });
  }

  assignRequest(requestId: number, assignData: AssignRequest, userNtid: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${requestId}/assign`, assignData, {
      headers: this.getHeaders(userNtid)
    });
  }

  updateStatus(requestId: number, statusData: UpdateStatus, userNtid: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${requestId}/status`, statusData, {
      headers: this.getHeaders(userNtid)
    });
  }

  deleteRequest(requestId: number, userNtid: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${requestId}`, {
      headers: this.getHeaders(userNtid)
    });
  }

  getDashboardStats(userNtid: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`, {
      headers: this.getHeaders(userNtid)
    });
  }

  getEtaAlerts(userNtid: string, thresholdMinutes: number = 30): Observable<Request[]> {
    return this.http.get<Request[]>(`${this.apiUrl}/eta-alerts?thresholdMinutes=${thresholdMinutes}`, {
      headers: this.getHeaders(userNtid)
    });
  }

  getAssignableUsers(userNtid: string, role?: string): Observable<User[]> {
    let url = `${environment.apiUrl}/users`;
    if (role) {
      url += `?role=${role}`;
    }
    return this.http.get<User[]>(url, {
      headers: this.getHeaders(userNtid)
    });
  }
}
