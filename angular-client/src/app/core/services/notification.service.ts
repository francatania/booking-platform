import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {

  notificationSubject$ = new Subject<{type: 'success'|'error'|'warning', message: string}>()
  Notification$ = this.notificationSubject$.asObservable();

  constructor(){}

  success(msg: string){
    this.notificationSubject$.next({type: 'success', message: msg});
  }

  error(msg: string){
    this.notificationSubject$.next({type: 'error', message: msg});
  }

  warning(msg: string){
    this.notificationSubject$.next({type: 'error', message: msg});
  }
}
