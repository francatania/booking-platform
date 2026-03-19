import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompanyServiceResponse } from '../../../core/models/company.model';
import { CompanyServiceService } from '../../../core/services/company.service.service';
import { ServiceListComponent } from '../components/service-list.component/service-list.component';

@Component({
  selector: 'app-home-page',
  imports: [CommonModule, ServiceListComponent],
  templateUrl: './home.page.html'
})
export class HomePage {

  services: CompanyServiceResponse[] = []
  currentPage = 0;
  totalPages = 0;

  constructor(private companyServiceService: CompanyServiceService){}

  ngOnInit() {
    this.loadServices();
  }

  onPageChange(page: number) {
    this.loadServices(page);
  }

  private loadServices(page = 0){
    this.companyServiceService.getServices(page).subscribe({
      next: (response) => {
        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.services = response.content;
      },
      error: (err) => console.error(err)//TODO: Mas adelante tengo que llamar a un state service que dispare toast de error. Separar responsabilidades.
    });
  }
}
