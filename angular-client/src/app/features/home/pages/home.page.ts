import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompanyServiceResponse } from '../../../core/models/company.model';
import { CompanyServiceService } from '../../../core/services/company.service.service';
import { ServiceListComponent } from '../components/service-list.component/service-list.component';
import { BookingFormComponent } from '../components/booking-form.component/booking-form.component';

@Component({
  selector: 'app-home-page',
  imports: [CommonModule, ServiceListComponent, BookingFormComponent],
  templateUrl: './home.page.html'
})
export class HomePage implements OnInit {

  services: CompanyServiceResponse[] = []
  currentPage = 0;
  totalPages = 0;
  serviceSelected: CompanyServiceResponse | null= null;

  constructor(private companyServiceService: CompanyServiceService){}

  onServiceSelected(service: CompanyServiceResponse){
    this.serviceSelected = service;
  }

  onBookingCreated(){
    this.serviceSelected = null;
  }

  onBookingCancelled(){
    this.serviceSelected = null;
  }

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
