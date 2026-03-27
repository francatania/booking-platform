import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { CompanyServiceResponse } from '../../../../core/models/company.model';
import { AppStateService } from '../../../../core/services/app-state.service';

@Component({
  selector: 'app-booking-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-form.component.html',
  styles: ``,
})
export class BookingFormComponent {
  @Input() service!: CompanyServiceResponse;
  @Output() bookingCreated = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  date = '';
  startTime = '';
  isLoading = false;
  errorMessage = '';
  today = new Date().toISOString().split('T')[0];


  timeSlots = this.generateSlots();

  constructor(private appState: AppStateService) {}

  private generateSlots(): string[] {
    const slots = [];
    for (let h = 0; h < 24; h++) {
      for (const m of [0, 15, 30, 45]) {
        slots.push(`${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`);
      }
    }
    return slots;
  }

  get computedEndTime(): string {
    if (!this.startTime || !this.service) return '';
    const [hours, minutes] = this.startTime.split(':').map(Number);
    const total = hours * 60 + minutes + this.service.durationMinutes;
    const endH = Math.floor(total / 60) % 24;
    const endM = total % 60;
    return `${String(endH).padStart(2, '0')}:${String(endM).padStart(2, '0')}`;
  }

  onBooking(form: NgForm) {
    if (form.invalid) return;

    this.isLoading = true;

    const startIso = `${this.date}T${this.startTime}:00`;
    const endIso = `${this.date}T${this.computedEndTime}:00`;

    this.appState.createBooking(
      { service_id: this.service.id, 
        company_id: this.service.companyId, 
        start_time: startIso, 
        end_time: endIso,
      price: this.service.price },
      () => { this.isLoading = false; this.bookingCreated.emit(); },
      (msg) => { this.isLoading = false; this.errorMessage = msg; }
    );
  }
}
