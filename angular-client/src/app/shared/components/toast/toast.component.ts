import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';

interface Toast {
  type: 'success' | 'error' | 'warning';
  message: string;
}

@Component({
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './toast.component.html',
})
export class ToastComponent implements OnInit, OnDestroy {
  toast: Toast | null = null;
  private sub!: Subscription;
  private timer: any;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.sub = this.notificationService.Notification$.subscribe((notification) => {
      clearTimeout(this.timer);
      this.toast = notification;
      this.timer = setTimeout(() => (this.toast = null), 4000);
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
    clearTimeout(this.timer);
  }

  dismiss() {
    this.toast = null;
    clearTimeout(this.timer);
  }
}
