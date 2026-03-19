import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login.page';
import { HomePage } from './features/home/pages/home.page';
import { AdminPage } from './features/admin/pages/admin.page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'home', component: HomePage },
  { path: 'admin', component: AdminPage },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
