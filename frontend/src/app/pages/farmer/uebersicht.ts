import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DecimalPipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { QuoteResult, CROP_TYPES, BUNDESLAENDER, DEDUCTIBLES } from '../../models';

/** Farmer portal — Übersicht: live premium quote calculator. */
@Component({
  selector: 'app-farmer-uebersicht',
  imports: [FormsModule, CurrencyPipe, DecimalPipe],
  templateUrl: './uebersicht.html'
})
export class Uebersicht {
  private http = inject(HttpClient);

  readonly quoteResult = signal<QuoteResult | null>(null);
  readonly error = signal('');

  quote = { cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN', deductible: 'TEN_PERCENT', coverageEur: 25000, coordinateE: 3700000, coordinateN: 5570000 };

  readonly cropTypes = CROP_TYPES;
  readonly bundeslaender = BUNDESLAENDER;
  readonly deductibles = DEDUCTIBLES;

  quoteNow(): void {
    this.error.set('');
    this.quoteResult.set(null);
    this.http.get<QuoteResult>('/api/policies/quote', { params: { ...this.quote } as never }).subscribe({
      next: r => this.quoteResult.set(r),
      error: e => this.error.set(httpErrorDetail(e))
    });
  }
}
