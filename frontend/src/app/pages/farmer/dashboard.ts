import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';

import { AuthService } from '../../core/auth.service';
import { httpErrorDetail } from '../../core/errors';
import { Plot, Policy, Claim, QuoteResult, CROP_TYPES, BUNDESLAENDER, DEDUCTIBLES } from '../../models';

@Component({
  selector: 'app-farmer-dashboard',
  imports: [FormsModule, CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './dashboard.html'
})
export class FarmerDashboard implements OnInit {
  private auth = inject(AuthService);
  private http = inject(HttpClient);

  readonly insuredId = this.auth.insuredId;
  readonly plots = signal<Plot[]>([]);
  readonly policies = signal<Policy[]>([]);
  readonly claims = signal<Claim[]>([]);
  readonly quoteResult = signal<QuoteResult | null>(null);
  readonly error = signal('');

  quote = { cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN', deductible: 'TEN_PERCENT', coverageEur: 25000, coordinateE: 3700000, coordinateN: 5570000 };
  plotForm = { cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN', coordinateE: 3700000, coordinateN: 5570000, locationDescription: 'Mein Feld' };
  policyForm = { plotId: 0, coverageEur: 25000, deductible: 'TEN_PERCENT', coverageStart: '', coverageEnd: '' };
  claimForm = { policyId: 0, damageDate: '', damageDescription: '' };

  readonly cropTypes = CROP_TYPES;
  readonly bundeslaender = BUNDESLAENDER;
  readonly deductibles = DEDUCTIBLES;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    const id = this.insuredId()!;
    this.http.get<Plot[]>(`/api/plots?insuredId=${id}`).subscribe({
      next: p => {
        this.plots.set(p);
        if (!this.policyForm.plotId && p.length) this.policyForm.plotId = p[0].id;
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
    this.http.get<Policy[]>(`/api/policies?insuredId=${id}`).subscribe({
      next: p => {
        this.policies.set(p);
        if (!this.claimForm.policyId && p.length) this.claimForm.policyId = p[0].id;
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
    this.http.get<Claim[]>(`/api/claims?insuredId=${id}`).subscribe({
      next: c => this.claims.set(c),
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  quoteNow(): void {
    this.error.set('');
    this.quoteResult.set(null);
    this.http.get<QuoteResult>('/api/policies/quote', { params: { ...this.quote } as never }).subscribe({
      next: r => this.quoteResult.set(r),
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  createPlot(): void {
    this.error.set('');
    this.http.post<Plot>('/api/plots', { ...this.plotForm, insuredId: this.insuredId() }).subscribe({
      next: () => {
        this.plotForm.locationDescription = '';
        window.alert('Feld angelegt');
        this.refresh();
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  createPolicy(): void {
    this.error.set('');
    this.http.post<Policy>('/api/policies', { ...this.policyForm, status: 'ACTIVE' }).subscribe({
      next: () => {
        window.alert('Police angelegt');
        this.refresh();
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  fileClaim(): void {
    this.error.set('');
    this.http.post<Claim>('/api/claims', this.claimForm).subscribe({
      next: () => {
        this.claimForm.damageDescription = '';
        window.alert('Schaden gemeldet');
        this.refresh();
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }
}
