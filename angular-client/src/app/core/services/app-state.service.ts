import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BookingService } from './booking.service';
import { NotificationService } from './notification.service';
import { BookingCreate, BookingDetailResponse, BookingResponse, BookingStats, RescheduleRequest } from '../models/booking.model';
import { CompanyServiceService, ServiceFilters } from './company.service.service';
import { CompanyResponse, CompanyServiceResponse, CreateCompanyServiceRequest, PageResponse, UpdateCompanyServiceRequest } from '../models/company.model';

@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  constructor(
    private bookingService: BookingService,
    private notificationService: NotificationService,
    private companyServiceService: CompanyServiceService,
    private translate: TranslateService
  ) {}

  createBooking(dto: BookingCreate, onSuccess?: () => void, onError?: (msg: string) => void) {
    this.bookingService.createBooking(dto).subscribe({
      next: () => {
        this.notificationService.success(this.translate.instant('NOTIFY.BOOKING_CREATED'));
        onSuccess?.();
      },
      error: (err) => {
        const msg = err.status === 409
          ? this.translate.instant('NOTIFY.ERROR_SLOT_TAKEN')
          : err.status === 403
          ? this.translate.instant('NOTIFY.ERROR_FORBIDDEN')
          : err.status === 404
          ? this.translate.instant('NOTIFY.ERROR_SERVICE_NOT_FOUND')
          : this.translate.instant('NOTIFY.ERROR_GENERIC');
        onError?.(msg);
      }
    });
  }

  cancelBooking(id: number, onSuccess?: () => void) {
    this.bookingService.cancelBooking(id).subscribe({
      next: () => {
        this.notificationService.success(this.translate.instant('NOTIFY.BOOKING_CANCELLED'));
        onSuccess?.();
      },
      error: (err) => {
        this.notificationService.error(err.error?.error || err.error?.detail || this.translate.instant('NOTIFY.ERROR_GENERIC'));
      }
    });
  }

  rescheduleBooking(id: number, dto: RescheduleRequest, onSuccess?: () => void, onError?: () => void) {
    this.bookingService.rescheduleBooking(id, dto).subscribe({
      next: () => {
        this.notificationService.success(this.translate.instant('NOTIFY.BOOKING_RESCHEDULED'));
        onSuccess?.();
      },
      error: (err) => {
        this.notificationService.error(
          err.status === 409
            ? this.translate.instant('NOTIFY.ERROR_SLOT_TAKEN')
            : err.error?.detail || this.translate.instant('NOTIFY.ERROR_RESCHEDULE')
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

  getServices(page: number, filters: ServiceFilters, onSuccess: (response: PageResponse<CompanyServiceResponse>) => void) {
    this.companyServiceService.getServices(page, 10, filters).subscribe({
      next: (response) => {
        if (response.content.length === 0) {
          this.notificationService.warning(this.translate.instant('NOTIFY.NO_SERVICES'));
        }
        onSuccess(response);
      },
      error: () => this.notificationService.error(this.translate.instant('NOTIFY.ERROR_LOAD_SERVICES'))
    });
  }

  getCompanies(onSuccess: (companies: CompanyResponse[]) => void) {
    this.companyServiceService.getCompanies().subscribe({
      next: onSuccess,
      error: () => this.notificationService.error(this.translate.instant('NOTIFY.ERROR_LOAD_SERVICES'))
    });
  }

  getCompanyBookings(status: string, onSuccess: (bookings: BookingDetailResponse[]) => void, fromDate?: string, toDate?: string, fullName?: string) {
    this.bookingService.getCompanyBookings(status, fromDate, toDate, fullName).subscribe({
      next: onSuccess,
      error: () => this.notificationService.error(this.translate.instant('NOTIFY.ERROR_LOAD_BOOKINGS'))
    });
  }

  confirmBooking(id: number, onSuccess?: () => void) {
    this.bookingService.confirmBooking(id).subscribe({
      next: () => { this.notificationService.success(this.translate.instant('NOTIFY.BOOKING_CONFIRMED')); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.detail || this.translate.instant('NOTIFY.ERROR_CONFIRM'))
    });
  }

  completeBooking(id: number, onSuccess?: () => void) {
    this.bookingService.completeBooking(id).subscribe({
      next: () => { this.notificationService.success(this.translate.instant('NOTIFY.BOOKING_COMPLETED')); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.detail || this.translate.instant('NOTIFY.ERROR_COMPLETE'))
    });
  }

  getStats(fromDate: string, toDate: string, onSuccess: (stats: BookingStats) => void) {
    this.bookingService.getStats(fromDate, toDate).subscribe({
      next: onSuccess,
      error: () => this.notificationService.error(this.translate.instant('NOTIFY.ERROR_LOAD_STATS'))
    });
  }

  getServicesByCompany(companyId: number, onSuccess: (services: CompanyServiceResponse[]) => void) {
    this.companyServiceService.getServicesByCompany(companyId).subscribe({
      next: onSuccess,
      error: () => this.notificationService.error(this.translate.instant('NOTIFY.ERROR_LOAD_SERVICES'))
    });
  }

  createService(companyId: number, dto: CreateCompanyServiceRequest, onSuccess?: () => void) {
    this.companyServiceService.createService(companyId, dto).subscribe({
      next: () => { this.notificationService.success(this.translate.instant('NOTIFY.SERVICE_CREATED')); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.error || this.translate.instant('NOTIFY.ERROR_GENERIC'))
    });
  }

  editService(id: number, dto: UpdateCompanyServiceRequest, onSuccess?: () => void) {
    this.companyServiceService.editService(id, dto).subscribe({
      next: () => { this.notificationService.success(this.translate.instant('NOTIFY.SERVICE_UPDATED')); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.error || this.translate.instant('NOTIFY.ERROR_GENERIC'))
    });
  }

  toggleServiceActive(id: number, isActive: boolean, onSuccess?: () => void) {
    const call = isActive
      ? this.companyServiceService.deactivateService(id)
      : this.companyServiceService.activateService(id);
    call.subscribe({
      next: () => { this.notificationService.success(this.translate.instant(isActive ? 'NOTIFY.SERVICE_DEACTIVATED' : 'NOTIFY.SERVICE_ACTIVATED')); onSuccess?.(); },
      error: (err) => this.notificationService.error(err.error?.error || this.translate.instant('NOTIFY.ERROR_GENERIC'))
    });
  }
}
