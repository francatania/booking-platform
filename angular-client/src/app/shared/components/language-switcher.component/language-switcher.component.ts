import { Component } from '@angular/core';
import { LanguageService } from '../../../core/services/language.service';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-language-switcher',
  imports: [MatIconModule],
  templateUrl: './language-switcher.component.html',
})
export class LanguageSwitcherComponent {
  constructor(public lang: LanguageService){}
}
