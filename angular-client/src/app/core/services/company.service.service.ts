import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompanyResponse, CompanyServiceResponse, PageResponse } from '../models/company.model';

export interface ServiceFilters {
  companyId?: number;
  name?: string;
  minPrice?: number;
  maxPrice?: number;
}

@Injectable({
  providedIn: 'root',
})
export class CompanyServiceService {
  BASE_URL = environment.apiUrl

  constructor(private http: HttpClient){}

  getServices(page = 0, size = 10, filters: ServiceFilters = {}): Observable<PageResponse<CompanyServiceResponse>>{
    let params = new HttpParams().set('page', page).set('size', size);
    if (filters.companyId != null) params = params.set('companyId', filters.companyId);
    if (filters.name?.trim()) params = params.set('name', filters.name.trim());
    if (filters.minPrice != null) params = params.set('minPrice', filters.minPrice);
    if (filters.maxPrice != null) params = params.set('maxPrice', filters.maxPrice);
    return this.http.get<PageResponse<CompanyServiceResponse>>(`${this.BASE_URL}/services`, { params });
  }

  getCompanies(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(`${this.BASE_URL}/companies`);
  }
}
