import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-confirmation-dialog',
  imports: [TranslateModule],
  templateUrl: './confirmation-dialog.component.html',
})
export class ConfirmationDialogComponent {
  @Input() title = '';
  @Input() message = '';
  @Input() confirmLabel = 'DIALOG.CONFIRM';
  @Input() cancelLabel = 'DIALOG.CANCEL';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
}
