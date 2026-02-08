import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  ntid: string;
  email: string;
  role: string;
  account: string;
  accountId: number;
  active: boolean;
  createdAt?: string;
}

export interface UpdateUserData {
  ntid?: string;
  email?: string;
  account?: string;
  accountId?: number | null;
  role?: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getAllUsers(userNtid: string): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl, {
      headers: new HttpHeaders({ 'X-User-NTID': userNtid })
    });
  }

  getUserByNtid(ntid: string, userNtid: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${ntid}`, {
      headers: new HttpHeaders({ 'X-User-NTID': userNtid })
    });
  }

  updateUser(ntid: string, updateData: UpdateUserData, userNtid: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${ntid}`, updateData, {
      headers: new HttpHeaders({ 
        'X-User-NTID': userNtid,
        'Content-Type': 'application/json'
      })
    });
  }

  deleteUser(ntid: string, userNtid: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${ntid}`, {
      headers: new HttpHeaders({ 'X-User-NTID': userNtid })
    });
  }
}
