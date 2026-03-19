import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompanyServiceResponse, PageResponse } from '../models/company.model';

@Injectable({
  providedIn: 'root',
})
export class CompanyServiceService {
  BASE_URL = environment.apiUrl

  constructor(private http: HttpClient){}

  getServices(page = 0, size = 10): Observable<PageResponse<CompanyServiceResponse>>{
    return this.http.get<PageResponse<CompanyServiceResponse>>(`${this.BASE_URL}/services`, {params: {page, size}})
  }
}
