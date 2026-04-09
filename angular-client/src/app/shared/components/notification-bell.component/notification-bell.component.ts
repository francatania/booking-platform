import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { InboxService, InboxNotification } from '../../../core/services/inbox.service';

interface ParsedNotification extends InboxNotification {
  titleParsed: string;
  messageParsed: string;
}

@Component({
  selector: 'app-notification-bell',
  imports: [CommonModule, MatMenuModule, MatButtonModule, MatIconModule, MatDividerModule, MatBadgeModule, TranslateModule],
  templateUrl: './notification-bell.component.html',
})
export class NotificationBellComponent implements OnInit {
  unreadCount = 0;
  parsedNotification: ParsedNotification[] = [];

  constructor(private inbox: InboxService, private translate: TranslateService) {}

  ngOnInit() {
    this.loadUnreadCount();
  }

  loadUnreadCount() {
    this.inbox.getUnreadCount().subscribe({
      next: (res) => (this.unreadCount = res.count),
    });
  }

  openNotifications() {
    this.parsedNotification = [];
    this.inbox.getNotifications().subscribe({
      next: (res) => {
        this.parsedNotification = res.map((n) => {
          const [serviceName, date, startTime] = n.message.split(';');
          const key = `NOTIFICATION.${n.type}`;
          return {
            ...n,
            titleParsed: this.translate.instant(`${key}.TITLE`),
            messageParsed: this.translate.instant(`${key}.MESSAGE`, { serviceName, date, startTime }),
          };
        });
      },
    });
  }

  markAllRead() {
    this.inbox.markAllAsRead().subscribe({
      next: () => {
        this.parsedNotification = this.parsedNotification.map((n) => ({ ...n, is_read: true }));
        this.unreadCount = 0;
      },
    });
  }

  markRead(n: ParsedNotification) {
    if (n.is_read) return;
    this.inbox.markAsRead(n.id).subscribe({
      next: () => {
        n.is_read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
    });
  }
}
