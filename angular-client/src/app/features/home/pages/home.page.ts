import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CompanyResponse, CompanyServiceResponse } from '../../../core/models/company.model';
import { ServiceFilters } from '../../../core/services/company.service.service';
import { ServiceListComponent } from '../components/service-list.component/service-list.component';
import { BookingFormComponent } from '../components/booking-form.component/booking-form.component';
import { AppStateService } from '../../../core/services/app-state.service';

@Component({
  selector: 'app-home-page',
  imports: [CommonModule, FormsModule, ServiceListComponent, BookingFormComponent, TranslateModule],
  templateUrl: './home.page.html'
})
export class HomePage implements OnInit {

  services: CompanyServiceResponse[] = [];
  companies: CompanyResponse[] = [];
  currentPage = 0;
  totalPages = 0;
  serviceSelected: CompanyServiceResponse | null = null;

  filters: ServiceFilters = {};

  constructor(private appState: AppStateService) {}

  ngOnInit() {
    this.appState.getCompanies((companies) => this.companies = companies);
    this.loadServices();
  }

  onServiceSelected(service: CompanyServiceResponse) {
    this.serviceSelected = service;
  }

  onBookingCreated() {
    this.serviceSelected = null;
  }

  onBookingCancelled() {
    this.serviceSelected = null;
  }

  onPageChange(page: number) {
    this.loadServices(page);
  }

  onFilterApply() {
    this.loadServices(0);
  }

  onFilterReset() {
    this.filters = {};
    this.loadServices(0);
  }

  private loadServices(page = 0) {
    this.appState.getServices(page, this.filters, (response) => {
      this.currentPage = response.number;
      this.totalPages = response.totalPages;
      this.services = response.content;
    });
  }
}
