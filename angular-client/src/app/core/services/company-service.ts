import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { CompanyDetailResponse } from '../models/company.model';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class CompanyService {
  BASE_URL = environment.apiUrl

  constructor(private http: HttpClient){}

  getCompanyName(companyId: number): Observable<CompanyDetailResponse>{
    return this.http.get<CompanyDetailResponse>(`${this.BASE_URL}/companies/${companyId}`);
  }
}
