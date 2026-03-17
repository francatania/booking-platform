import { Component } from '@angular/core';
import { LoginFormComponent } from '../components/login-form.component';

@Component({
  selector: 'app-login-page',
  imports: [LoginFormComponent],
  templateUrl: './login.page.html'
})
export class LoginPage {}
