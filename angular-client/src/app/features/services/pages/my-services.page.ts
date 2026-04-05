import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AppStateService } from '../../../core/services/app-state.service';
import { UserService } from '../../../core/services/user.service';
import { CompanyServiceResponse, CreateCompanyServiceRequest, UpdateCompanyServiceRequest } from '../../../core/models/company.model';

@Component({
  selector: 'app-my-services-page',
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './my-services.page.html'
})
export class MyServicesPage implements OnInit {
  services: CompanyServiceResponse[] = [];
  companyId: number;

  showForm = false;
  showConfirm = false;
  editingId: number | null = null;

  form: CreateCompanyServiceRequest = this.emptyForm();

  constructor(private appState: AppStateService, userService: UserService) {
    this.companyId = userService.getUser()?.companyId ?? 0;
  }

  ngOnInit() {
    this.load();
  }

  load() {
    this.appState.getServicesByCompany(this.companyId, (services) => this.services = services);
  }

  openCreate() {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEdit(service: CompanyServiceResponse) {
    this.editingId = service.id;
    this.form = {
      name: service.name,
      description: service.description,
      durationMinutes: service.durationMinutes,
      price: service.price
    };
    this.showForm = true;
  }

  onFormSubmit() {
    this.showConfirm = true;
  }

  onConfirmSave() {
    this.showConfirm = false;
    if (this.editingId == null) {
      this.appState.createService(this.companyId, this.form, () => { this.showForm = false; this.load(); });
    } else {
      const dto: UpdateCompanyServiceRequest = { ...this.form };
      this.appState.editService(this.editingId, dto, () => { this.showForm = false; this.load(); });
    }
  }

  onCancelConfirm() {
    this.showConfirm = false;
  }

  onCancel() {
    this.showForm = false;
  }

  onToggleActive(service: CompanyServiceResponse) {
    this.appState.toggleServiceActive(service.id, service.isActive, () => this.load());
  }

  private emptyForm(): CreateCompanyServiceRequest {
    return { name: '', description: '', durationMinutes: 0, price: 0 };
  }
}
