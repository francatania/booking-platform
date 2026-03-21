import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  @Input() username: string = "";
  @Input() role: string = "";

  constructor(private userService: UserService, private router: Router){}
  
  onLogout(){
    this.userService.logout();
    this.router.navigate(['/login']);
  }
}
