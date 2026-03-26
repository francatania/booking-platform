import { Injectable } from '@angular/core';
import { BookingService } from './booking.service';
import { NotificationService } from './notification.service';
import { BookingCreate, BookingResponse, RescheduleRequest } from '../models/booking.model';

@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  constructor(
    private bookingService: BookingService,
    private notificationService: NotificationService
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
}
