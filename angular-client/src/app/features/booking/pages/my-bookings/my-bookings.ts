import { Component, OnInit } from '@angular/core';
import { CalendarOptions, EventClickArg, EventDropArg, EventContentArg } from '@fullcalendar/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { AppStateService } from '../../../../core/services/app-state.service';
import { BookingResponse } from '../../../../core/models/booking.model';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-my-bookings',
  imports: [FullCalendarModule, CommonModule, TranslateModule],
  templateUrl: './my-bookings.html',
  styles: ``,
})
export class MyBookings implements OnInit {
  bookings: BookingResponse[] = [];
  selectedBooking: BookingResponse | null = null;
  showConfirm = false;
  pendingDrop: EventDropArg | null = null;

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    height: '80vh',
    scrollTime: new Date().toTimeString().slice(0, 8),
    editable: true,
    eventDurationEditable: false,
    events: [],
    eventContent: (arg: EventContentArg) => {
      const start = arg.event.start ? arg.event.start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
      const end = arg.event.end ? arg.event.end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
      return {
        html: `
          <div style="padding: 2px 4px; overflow: hidden; height: 100%;">
            <div style="font-weight: 400; font-size: 0.8rem; white-space: normal; word-break: break-word; line-height: 1.2;">${arg.event.title}</div>
            <div style="font-size: 0.72rem; opacity: 0.9; margin-top: 2px;">${start} – ${end}</div>
          </div>`
      };
    },
    eventClick: (info: EventClickArg) => this.handleEventClick(info),
    eventDrop: (info: EventDropArg) => this.handleEventDrop(info)
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
          title: `${b.service_name}`,
          start: b.start_time,
          end: b.end_time,
          color: b.status === 'PENDING' ? '#f59e0b' : b.status === 'CONFIRMED' ? '#3b82f6' : b.status === 'COMPLETED' ? '#10b981' : '#9ca3af',
          extendedProps: { booking: b }
        }))
      };
    });
  }

  handleEventDrop(info: EventDropArg) {
    const booking: BookingResponse = info.event.extendedProps['booking'];

    if (booking.status === 'CANCELLED') {
      info.revert();
      return;
    }

    this.pendingDrop = info;
  }

  handleEventClick(info: EventClickArg) {
    this.selectedBooking = info.event.extendedProps['booking'];
  }

  private toLocalISO(date: Date): string {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 19);
  }

  closeMenu() {
    this.selectedBooking = null;
    this.showConfirm = false;
  }

  onCancelClick() {
    this.showConfirm = true;
  }

  confirmReschedule() {
    const info = this.pendingDrop!;
    const booking: BookingResponse = info.event.extendedProps['booking'];
    const start = info.event.start!;
    const end = info.event.end ?? new Date(start.getTime() + (new Date(booking.end_time).getTime() - new Date(booking.start_time).getTime()));

    this.pendingDrop = null;
    this.appState.rescheduleBooking(
      booking.id,
      { start_time: this.toLocalISO(start), end_time: this.toLocalISO(end) },
      () => this.loadBookings(),
      () => info.revert()
    );
  }

  cancelReschedule() {
    this.pendingDrop!.revert();
    this.pendingDrop = null;
  }

  confirmCancel() {
    this.appState.cancelBooking(this.selectedBooking!.id, () => {
      this.selectedBooking = null;
      this.showConfirm = false;
      this.loadBookings();
    });
  }
}
