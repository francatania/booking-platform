import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AppStateService } from '../../../core/services/app-state.service';
import { BookingDetailResponse } from '../../../core/models/booking.model';

@Component({
  selector: 'app-operator-page',
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './operator.page.html'
})
export class OperatorPage implements OnInit {
  activeTab: 'pending' | 'confirmed' = 'pending';
  pendingBookings: BookingDetailResponse[] = [];
  confirmedBookings: BookingDetailResponse[] = [];

  fromDate: string = '';
  toDate: string = '';
  fullName: string = '';

  constructor(private appState: AppStateService) {
    const saved = localStorage.getItem('operator_tab');
    if (saved === 'pending' || saved === 'confirmed') this.activeTab = saved;
  }

  ngOnInit() {
    this.loadPending();
    this.loadConfirmed();
  }

  switchTab(tab: 'pending' | 'confirmed') {
    this.activeTab = tab;
    localStorage.setItem('operator_tab', tab);
  }

  onFilterApply() {
    if (this.activeTab === 'pending') {
      this.loadPending();
    } else {
      this.loadConfirmed();
    }
  }

  onFilterReset() {
    this.fromDate = '';
    this.toDate = '';
    this.fullName = '';
    this.loadPending();
    this.loadConfirmed();
  }

  loadPending() {
    this.appState.getCompanyBookings('PENDING', (bookings) => this.pendingBookings = bookings, this.fromDate || undefined, this.toDate || undefined, this.fullName || undefined);
  }

  loadConfirmed() {
    this.appState.getCompanyBookings('CONFIRMED', (bookings) => this.confirmedBookings = bookings, this.fromDate || undefined, this.toDate || undefined, this.fullName || undefined);
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
