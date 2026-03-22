import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingCreate, BookingResponse } from '../models/booking.model';


@Injectable({
  providedIn: 'root',
})
export class BookingService {
  BASE_URL = environment.apiUrl
  constructor(private http: HttpClient){}

  createBooking(dto: BookingCreate): Observable<BookingResponse>{
    return this.http.post<BookingResponse>(`${this.BASE_URL}/bookings`, dto);
  }
}
