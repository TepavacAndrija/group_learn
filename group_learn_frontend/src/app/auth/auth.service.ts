import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, catchError } from 'rxjs/operators';
import { of, Observable, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });

  private userNameSubject = new BehaviorSubject<string | null>(null);
  public userName$ = this.userNameSubject.asObservable();

  constructor(private http: HttpClient) {
    this.userName$ = this.loadUserName();
  }
  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  login(loginData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, loginData, {
      headers: this.headers,
    });
  }

  setToken(token: string) {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  setPlayerId(playerId: string) {
    localStorage.setItem('playerId', playerId);
  }

  getPlayerId(): string {
    return localStorage.getItem('playerId') || '';
  }

  loadUserName(): Observable<string> {
    const playerId = this.getPlayerId();
    if (playerId) {
      return this.http
        .get<any>(`http://localhost:8080/api/users/${playerId}`)
        .pipe(
          map((response) => response?.username ?? 'Unknown'),
          catchError((err) => {
            console.error('Failed to load user name:', err);
            return of('Unknown');
          })
        );
    } else {
      return of('Unknown');
    }
  }

  isLoggedIn() {
    return !!localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
  }
}
