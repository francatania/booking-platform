import { Injectable } from '@angular/core';
import { BookingService } from './booking.service';
import { NotificationService } from './notification.service';
import { BookingCreate, BookingResponse, BookingStats, RescheduleRequest } from '../models/booking.model';
import { CompanyServiceService } from './company.service.service';
import { CompanyServiceResponse, PageResponse } from '../models/company.model';

@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  constructor(
    private bookingService: BookingService,
    private notificationService: NotificationService,
    private companyServiceService: CompanyServiceService
  ) {}

  createBooking(dto: BookingCreate, onSuccess?: () => void, onError?: (msg: string) => void) {
    this.bookingService.createBooking(dto).subscribe({
      next: () => {
        this.notificationService.success('Your booking was confirmed!');
        onSuccess?.();
      },
      error: (err) => {
        const msg = err.status === 409
          ? 'That time slot is already taken. Please choose another time.'
          : err.status === 403
          ? 'You are not allowed to make bookings.'
          : err.status === 404
          ? 'The selected service is no longer available.'
          : 'Something went wrong. Please try again.';
        onError?.(msg);
      }
    });
  }

  cancelBooking(id: number, onSuccess?: () => void) {
    this.bookingService.cancelBooking(id).subscribe({
      next: () => {
        this.notificationService.success('Booking cancelled.');
        onSuccess?.();
      },
      error: (err) => {
        this.notificationService.error(err.error?.error || err.error?.detail || 'Something went wrong.');
        
      }
    });
  }

  rescheduleBooking(id: number, dto: RescheduleRequest, onSuccess?: () => void, onError?: () => void) {
    this.bookingService.rescheduleBooking(id, dto).subscribe({
      next: () => {
        this.notificationService.success('Booking rescheduled.');
        onSuccess?.();
      },
      error: (err) => {
        this.notificationService.error(
          err.status === 409
            ? 'That time slot is already taken. Please choose another time.'
            : err.error?.detail || 'Could not reschedule booking.'
        );
        onError?.();
      }
    });
  }

  getMyBookings(onSuccess: (booking: BookingResponse[]) => void){
    this.bookingService.getMyBookings().subscribe({
      next: onSuccess,
      error: (err)=> this.notificationService.error(err.error?.detail)
    })
  }

  getServices(page: number, onSuccess: (response: PageResponse<CompanyServiceResponse>) => void) {
    this.companyServiceService.getServices(page).subscribe({
      next: (response) => {
        if (response.content.length === 0) {
          this.notificationService.warning('No services available at the moment.');
        }
        onSuccess(response);
      },
      error: () => this.notificationService.error('Could not load services. Please try again.')
    });
  }

  getCompanyBookings(status: string, onSuccess: (bookings: BookingResponse[]) => void) {
    this.bookingService.getCompanyBookings(status).subscribe({
      next: onSuccess,
      error: () => this.notificationService.error('Could not load bookings.')
    });
  }

  confirmBooking(id: number, onSuccess?: () => void) {
    this.bookingService.confirmBooking(id).subscribe({
      next: () => { this.notificationService.success('Booking confirmed.'); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.detail || 'Could not confirm booking.')
    });
  }

  completeBooking(id: number, onSuccess?: () => void) {
    this.bookingService.completeBooking(id).subscribe({
      next: () => { this.notificationService.success('Booking marked as completed.'); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.detail || 'Could not complete booking.')
    });
  }

  getStats(fromDate: string, toDate: string, onSuccess: (stats: BookingStats) => void) {
    this.bookingService.getStats(fromDate, toDate).subscribe({
      next: onSuccess,
      error: () => this.notificationService.error('Could not load stats.')
    });
  }
}
