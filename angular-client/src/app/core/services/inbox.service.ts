import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface InboxNotification {
  id: number;
  user_id: number;
  type: string;
  title: string;
  message: string;
  is_read: boolean;
  booking_id: number | null;
  created_at: string;
}

@Injectable({
  providedIn: 'root',
})
export class InboxService {
  private BASE_URL = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  getNotifications(limit = 20, offset = 0): Observable<InboxNotification[]> {
    return this.http.get<InboxNotification[]>(this.BASE_URL, {
      params: { limit, offset },
    });
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.BASE_URL}/unread-count`);
  }

  markAsRead(id: number): Observable<InboxNotification> {
    return this.http.patch<InboxNotification>(`${this.BASE_URL}/${id}/read`, {});
  }

  markAllAsRead(): Observable<{ updated: number }> {
    return this.http.patch<{ updated: number }>(`${this.BASE_URL}/read-all`, {});
  }
}
