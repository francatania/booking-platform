import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompanyServiceResponse } from '../../../../core/models/company.model';

@Component({
  selector: 'app-service-list',
  imports: [CommonModule],
  templateUrl: './service-list.component.html',
  styles: ``,
})
export class ServiceListComponent {
  @Input() services: CompanyServiceResponse[] = [];
  @Input() currentPage = 0;
  @Input() totalPages = 0;
  @Output() pageChange = new EventEmitter<number>();

  goToPreviousPage() {
    this.pageChange.emit(this.currentPage - 1);
  }

  goToNextPage() {
    this.pageChange.emit(this.currentPage + 1);
  }
}
