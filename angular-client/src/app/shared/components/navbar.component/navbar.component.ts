import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { Router } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language.service';
import { LanguageSwitcherComponent } from '../language-switcher.component/language-switcher.component';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, MatMenuModule, MatButtonModule, MatIconModule, MatDividerModule, TranslateModule, LanguageSwitcherComponent],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  @Input() username: string = "";
  @Input() role: string = "";
  @Input() email: string = "";
  @Input() companyName: string = "";

  constructor(private userService: UserService, private router: Router, public lang: LanguageService){}

  onLogout(){
    this.userService.logout();
    this.router.navigate(['/login']);
  }
}
