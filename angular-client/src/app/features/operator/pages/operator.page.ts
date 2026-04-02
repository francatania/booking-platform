import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppStateService } from '../../../core/services/app-state.service';
import { BookingDetailResponse } from '../../../core/models/booking.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-operator-page',
  imports: [CommonModule, TranslateModule],
  templateUrl: './operator.page.html'
})
export class OperatorPage implements OnInit {
  activeTab: 'pending' | 'confirmed' = 'pending';
  pendingBookings: BookingDetailResponse[] = [];
  confirmedBookings: BookingDetailResponse[] = [];

  constructor(private appState: AppStateService) {}

  ngOnInit() {
    this.loadPending();
    this.loadConfirmed();
  }

  loadPending() {
    this.appState.getCompanyBookings('PENDING', (bookings) => this.pendingBookings = bookings);
  }

  loadConfirmed() {
    this.appState.getCompanyBookings('CONFIRMED', (bookings) => this.confirmedBookings = bookings);
  }

  onConfirm(id: number) {
    this.appState.confirmBooking(id, () => { this.loadPending(); this.loadConfirmed(); });
  }

  onCancel(id: number) {
    this.appState.cancelBooking(id, () => { this.loadPending(); this.loadConfirmed(); });
  }

  onComplete(id: number) {
    this.appState.completeBooking(id, () => this.loadConfirmed());
  }
}
