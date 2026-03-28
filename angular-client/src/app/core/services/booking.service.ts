import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingCreate, BookingDetailResponse, BookingResponse, BookingStats, RescheduleRequest } from '../models/booking.model';


@Injectable({
  providedIn: 'root',
})
export class BookingService {
  BASE_URL = environment.apiUrl
  constructor(private http: HttpClient){}

  createBooking(dto: BookingCreate): Observable<BookingResponse>{
    return this.http.post<BookingResponse>(`${this.BASE_URL}/bookings`, dto);
  }

  getMyBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.BASE_URL}/bookings/my`);
  }

  cancelBooking(id: number): Observable<BookingResponse> {
    return this.http.patch<BookingResponse>(`${this.BASE_URL}/bookings/${id}/cancel`, {});
  }

  rescheduleBooking(id: number, dto: RescheduleRequest): Observable<BookingResponse> {
    return this.http.patch<BookingResponse>(`${this.BASE_URL}/bookings/${id}/reschedule`, dto);
  }

  getStats(fromDate: string, toDate: string): Observable<BookingStats> {
    return this.http.get<BookingStats>(`${this.BASE_URL}/bookings/stats`, {
      params: { from_date: fromDate, to_date: toDate }
    });
  }

  getCompanyBookings(status?: string): Observable<BookingDetailResponse[]> {
    const params: Record<string, string> = {};
    if (status) params['status'] = status;
    return this.http.get<BookingDetailResponse[]>(`${this.BASE_URL}/bookings/company`, { params });
  }

  confirmBooking(id: number): Observable<BookingResponse> {
    return this.http.patch<BookingResponse>(`${this.BASE_URL}/bookings/${id}/confirm`, {});
  }

  completeBooking(id: number): Observable<BookingResponse> {
    return this.http.patch<BookingResponse>(`${this.BASE_URL}/bookings/${id}/complete`, {});
  }


}
