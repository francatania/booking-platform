import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompanyServiceResponse } from '../../../../core/models/company.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-service-list',
  imports: [CommonModule, TranslateModule],
  templateUrl: './service-list.component.html',
  styles: ``,
})
export class ServiceListComponent {
  @Input() services: CompanyServiceResponse[] = [];
  @Input() currentPage = 0;
  @Input() totalPages = 0;
  @Output() pageChange = new EventEmitter<number>();
  @Output() serviceSelected = new EventEmitter<CompanyServiceResponse>();

  goToPreviousPage() {
    this.pageChange.emit(this.currentPage - 1);
  }

  goToNextPage() {
    this.pageChange.emit(this.currentPage + 1);
  }

  onServiceSelected(service: CompanyServiceResponse){
    this.serviceSelected.emit(service);
  }
}
