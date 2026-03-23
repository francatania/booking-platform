import { Component, OnInit } from '@angular/core';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { AppStateService } from '../../../../core/services/app-state.service';
import { BookingResponse } from '../../../../core/models/booking.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-bookings',
  imports: [FullCalendarModule, CommonModule],
  templateUrl: './my-bookings.html',
  styles: ``,
})
export class MyBookings implements OnInit {
  bookings: BookingResponse[] = [];
  selectedBooking: BookingResponse | null = null;
  showConfirm = false;

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    editable: false,
    events: [],
    eventClick: (info: EventClickArg) => this.handleEventClick(info)
  };

  constructor(private appState: AppStateService) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  private loadBookings() {
    this.appState.getMyBookings((bookings) => {
      this.bookings = bookings;
      this.calendarOptions = {
        ...this.calendarOptions,
        events: bookings.map(b => ({
          id: String(b.id),
          title: `Service #${b.service_id}`,
          start: b.start_time,
          end: b.end_time,
          color: b.status === 'CANCELLED' ? '#9ca3af' : '#3b82f6',
          extendedProps: { booking: b }
        }))
      };
    });
  }

  handleEventClick(info: EventClickArg) {
    this.selectedBooking = info.event.extendedProps['booking'];
  }

  closeMenu() {
    this.selectedBooking = null;
    this.showConfirm = false;
  }

  onCancelClick() {
    this.showConfirm = true;
  }

  confirmCancel() {
    this.appState.cancelBooking(this.selectedBooking!.id, () => {
      this.selectedBooking = null;
      this.showConfirm = false;
      this.loadBookings();
    });
  }
}
