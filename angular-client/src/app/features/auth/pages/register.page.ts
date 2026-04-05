import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { RegisterFormComponent } from '../components/register-form.component';

@Component({
  selector: 'app-register-page',
  imports: [RegisterFormComponent, TranslateModule],
  template: `
    <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 relative">
      <h1 class="mb-8 text-3xl font-bold text-gray-800">{{ 'REGISTER.TITLE' | translate }}</h1>
      <app-register-form />
    </div>
  `
})
export class RegisterPage {}
