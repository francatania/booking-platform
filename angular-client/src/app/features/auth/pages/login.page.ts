import { Component } from '@angular/core';
import { LoginFormComponent } from '../components/login-form.component';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language.service';

@Component({
  selector: 'app-login-page',
  imports: [LoginFormComponent, TranslateModule],
  templateUrl: './login.page.html'
})
export class LoginPage {
  constructor(public lang: LanguageService) {}
}
