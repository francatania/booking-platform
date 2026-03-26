import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../core/services/user.service';
import { NavbarComponent } from '../../components/navbar.component/navbar.component';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  imports: [NavbarComponent, RouterOutlet],
  templateUrl: './auth-layout.component.html'
})
export class AuthLayoutComponent implements OnInit{
  username = '';
  role = '';
  email = '';
  constructor(private userService: UserService){}

  ngOnInit(){
    this.username = this.userService.getUser()?.username ?? '';
    this.role = this.userService.getRole() ?? '';
    this.email = this.userService.getUser()?.sub ?? '';
  }
}
