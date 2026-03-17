export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  companyId?: number;
}

export interface AuthResponse {
  token: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPER_ADMIN';
  companyId: number | null;
}
