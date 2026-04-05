import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login.page';
import { HomePage } from './features/home/pages/home.page';
import { AdminPage } from './features/admin/pages/admin.page';
import { MyBookings } from './features/booking/pages/my-bookings/my-bookings';
import { AuthLayoutComponent } from './shared/layouts/auth-layout.component/auth-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { OperatorPage } from './features/operator/pages/operator.page';
import { MyServicesPage } from './features/services/pages/my-services.page';
import { RegisterPage } from './features/auth/pages/register.page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'register', component: RegisterPage },
  {
    path: '',
    component: AuthLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomePage, canActivate: [roleGuard(['USER', 'ADMIN', 'SUPER_ADMIN'])] },
      { path: 'my-bookings', component: MyBookings, canActivate: [roleGuard(['USER'])] },
      { path: 'admin', component: AdminPage, canActivate: [roleGuard(['ADMIN', 'MANAGER', 'SUPER_ADMIN'])] },
      { path: 'operator', component: OperatorPage, canActivate: [roleGuard(['OPERATOR', 'ADMIN', 'SUPER_ADMIN'])] },
      { path: 'my-services', component: MyServicesPage, canActivate: [roleGuard(['OPERATOR', 'ADMIN'])] },
    ]
  },
  { path: '**', redirectTo: 'login' },
];
