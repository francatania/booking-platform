import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login.page';
import { HomePage } from './features/home/pages/home.page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'home', component: HomePage },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
