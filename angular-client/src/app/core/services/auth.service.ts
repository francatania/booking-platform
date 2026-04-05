import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/auth.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(private http: HttpClient){}
  BASE_URL = environment.apiUrl

  login(credentials: LoginRequest): Observable<AuthResponse>{
    return this.http.post<AuthResponse>(`${this.BASE_URL}/auth/login`, credentials);
  }

  register(dto: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.BASE_URL}/auth/register`, dto);
  }
  


}
