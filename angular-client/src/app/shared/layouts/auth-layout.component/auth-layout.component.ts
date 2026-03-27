import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../core/services/user.service';
import { NavbarComponent } from '../../components/navbar.component/navbar.component';
import { RouterOutlet } from '@angular/router';
import { CompanyService } from '../../../core/services/company-service';

@Component({
  selector: 'app-auth-layout',
  imports: [NavbarComponent, RouterOutlet],
  templateUrl: './auth-layout.component.html'
})
export class AuthLayoutComponent implements OnInit{
  username = '';
  role = '';
  email = '';
  companyId = 0;
  companyName = '';
  constructor(private userService: UserService, private companyService: CompanyService){}

  ngOnInit(){
    this.username = this.userService.getUser()?.username ?? '';
    this.role = this.userService.getRole() ?? '';
    this.email = this.userService.getUser()?.sub ?? '';
    this.companyId = this.userService.getUser()?.companyId ?? 0;
    if(this.role != 'USER' && this.role != ''){
      this.loadCompany();
    }
    
  }

  loadCompany(){
    this.companyService.getCompanyName(this.companyId).subscribe({
      next: (company)=> this.companyName = company.name
    })
  }
}
