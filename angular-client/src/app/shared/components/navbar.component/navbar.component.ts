import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { Router } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, MatMenuModule, MatButtonModule, MatIconModule, MatDividerModule],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  @Input() username: string = "";
  @Input() role: string = "";
  @Input() email: string = "";
  @Input() companyName: string = "";

  constructor(private userService: UserService, private router: Router){}

  onLogout(){
    this.userService.logout();
    this.router.navigate(['/login']);
  }
}
