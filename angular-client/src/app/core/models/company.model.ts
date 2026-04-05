export interface CreateCompanyRequest {
  name: string;
  description?: string;
  address: string;
  phone: string;
}

export interface CompanyResponse {
  companyId: number;
  companyName: string;
}

export interface CompanyDetailResponse {
  id: number;
  name: string;
  description: string;
  address: string;
  phone: string;
  createdAt: string;
}

export interface CreateCompanyServiceRequest {
  name: string;
  description?: string;
  durationMinutes: number;
  price: number;
}

export interface UpdateCompanyServiceRequest {
  name?: string;
  description?: string;
  durationMinutes?: number;
  price?: number;
  isActive?: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}

export interface CompanyServiceResponse {
  id: number;
  companyId: number;
  companyName: string;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
