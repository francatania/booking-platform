import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AppStateService } from '../../../core/services/app-state.service';
import { BookingDetailResponse } from '../../../core/models/booking.model';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog.component/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-operator-page',
  imports: [CommonModule, FormsModule, TranslateModule, ConfirmationDialogComponent],
  templateUrl: './operator.page.html'
})
export class OperatorPage implements OnInit {
  activeTab: 'pending' | 'confirmed' = 'pending';
  pendingBookings: BookingDetailResponse[] = [];
  confirmedBookings: BookingDetailResponse[] = [];

  fromDate: string = '';
  toDate: string = '';
  fullName: string = '';

  showConfirm = false;
  confirmTitle = '';
  confirmMessage = '';
  private pendingAction: (() => void) | null = null;

  constructor(private appState: AppStateService, private translate: TranslateService) {
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
    this.openConfirm(
      this.translate.instant('OPERATOR.CONFIRM_TITLE'),
      this.translate.instant('OPERATOR.CONFIRM_TEXT'),
      () => this.appState.confirmBooking(id, () => { this.loadPending(); this.loadConfirmed(); })
    );
  }

  onCancel(id: number) {
    this.openConfirm(
      this.translate.instant('OPERATOR.CANCEL_TITLE'),
      this.translate.instant('OPERATOR.CANCEL_TEXT'),
      () => this.appState.cancelBooking(id, () => { this.loadPending(); this.loadConfirmed(); })
    );
  }

  onComplete(id: number) {
    this.openConfirm(
      this.translate.instant('OPERATOR.COMPLETE_TITLE'),
      this.translate.instant('OPERATOR.COMPLETE_TEXT'),
      () => this.appState.completeBooking(id, () => this.loadConfirmed())
    );
  }

  openConfirm(title: string, message: string, action: () => void) {
    this.confirmTitle = title;
    this.confirmMessage = message;
    this.pendingAction = action;
    this.showConfirm = true;
  }

  onDialogConfirmed() {
    this.pendingAction?.();
    this.showConfirm = false;
    this.pendingAction = null;
  }

  onDialogCancelled() {
    this.showConfirm = false;
    this.pendingAction = null;
  }
}
