import { Component, Input, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { Router } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language.service';
import { LanguageSwitcherComponent } from '../language-switcher.component/language-switcher.component';
import { InboxService, InboxNotification } from '../../../core/services/inbox.service';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, MatMenuModule, MatButtonModule, MatIconModule, MatDividerModule, MatBadgeModule, TranslateModule, LanguageSwitcherComponent],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
  @Input() username: string = "";
  @Input() role: string = "";
  @Input() email: string = "";
  @Input() companyName: string = "";

  unreadCount = 0;
  notifications: InboxNotification[] = [];

  constructor(private userService: UserService, private router: Router, public lang: LanguageService, private inbox: InboxService) {}

  ngOnInit() {
    this.loadUnreadCount();
  }

  loadUnreadCount() {
    this.inbox.getUnreadCount().subscribe({
      next: (res) => this.unreadCount = res.count,
    });
  }

  openNotifications() {
    this.inbox.getNotifications().subscribe({
      next: (res) => this.notifications = res,
    });
  }

  markAllRead() {
    this.inbox.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, is_read: true }));
        this.unreadCount = 0;
      },
    });
  }

  markRead(n: InboxNotification) {
    if (n.is_read) return;
    this.inbox.markAsRead(n.id).subscribe({
      next: () => {
        n.is_read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
    });
  }

  onLogout() {
    this.userService.logout();
    this.router.navigate(['/login']);
  }
}
