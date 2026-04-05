import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { LoginRequest } from '../../../core/models/auth.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher.component/language-switcher.component';

@Component({
  selector: 'app-login-form',
  imports: [CommonModule, FormsModule, TranslateModule, LanguageSwitcherComponent, RouterLink],
  templateUrl: './login-form.component.html'
})
export class LoginFormComponent {
  credentials: LoginRequest = { username: '', password: '' };
  errorMessage = '';
  isLoading = false;
  showPassword = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private translate: TranslateService
  ) {}

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        const role = this.userService.getRole();
        if (role === 'USER') {
          this.router.navigate(['/home']);
        } else if (role === 'OPERATOR') {
          this.router.navigate(['/operator']);
        } else if (role === 'MANAGER' || role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.error || this.translate.instant('LOGIN.FAILED');
        this.isLoading = false;
      }
    });
  }
}
