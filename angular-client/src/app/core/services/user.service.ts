import { Injectable } from '@angular/core';

export interface UserPayload {
  sub: string;
  userId: number;
  username: string;
  role: 'USER' | 'ADMIN' | 'SUPER_ADMIN';
  companyId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  getUser(): UserPayload | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload)) as UserPayload;
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    return this.getUser()?.role ?? null;
  }

  logout(): void {
    localStorage.removeItem('token');
  }
}
