import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly STORAGE_KEY = 'lang';
  private readonly SUPPORTED = ['en', 'es'];
  private readonly DEFAULT = 'en';

  constructor(private translate: TranslateService) {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    const lang = saved && this.SUPPORTED.includes(saved) ? saved : this.DEFAULT;
    this.translate.use(lang);
  }

  get current(): string {
    return this.translate.currentLang || this.DEFAULT;
  }

  switch(lang: string): void {
    if (!this.SUPPORTED.includes(lang)) lang = this.DEFAULT;
    localStorage.setItem(this.STORAGE_KEY, lang);
    this.translate.use(lang);
  }
}
