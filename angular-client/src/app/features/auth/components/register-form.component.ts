import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/auth.model';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher.component/language-switcher.component';

@Component({
  selector: 'app-register-form',
  imports: [CommonModule, FormsModule, TranslateModule, LanguageSwitcherComponent, RouterLink],
  templateUrl: './register-form.component.html'
})
export class RegisterFormComponent {
  dto: RegisterRequest = { username: '', firstName: '', lastName: '', email: '', password: '' };
  errorMessage = '';
  isLoading = false;
  showPassword = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService
  ) {}

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.dto).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || this.translate.instant('REGISTER.FAILED');
        this.isLoading = false;
      }
    });
  }
}
