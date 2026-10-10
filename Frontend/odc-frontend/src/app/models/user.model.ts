export type Role = 'CUSTOMER' | 'WASHER' | 'ADMIN' | 'GOD';

export interface User {
  id: number;
  email: string;
  fullName: string;
  phone: string;
  role: Role;
  active: boolean;
  address?: string;
  city?: string;
  avatarUrl?: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone: string;
  role: Role;
  address?: string;
  city?: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  phone: string;
  address?: string;
  city?: string;
}
