import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-login-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './login-form.component.html'
})
export class LoginFormComponent {
  credentials: LoginRequest = { username: '', password: '' };
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Login failed';
        this.isLoading = false;
      }
    });
  }
}
