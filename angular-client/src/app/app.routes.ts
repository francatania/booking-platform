import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login.page';
import { HomePage } from './features/home/pages/home.page';
import { AdminPage } from './features/admin/pages/admin.page';
import { MyBookings } from './features/booking/pages/my-bookings/my-bookings';
import { AuthLayoutComponent } from './shared/layouts/auth-layout.component/auth-layout.component';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      { path: 'home', component: HomePage },
      { path: 'my-bookings', component: MyBookings },
      { path: 'admin', component: AdminPage },
    ]
  },
  { path: '**', redirectTo: 'login' },
];
