import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { Plot, Policy, DEDUCTIBLES } from '../../models';

/** Farmer portal — Verträge: policy list + policy creation. */
@Component({
  selector: 'app-farmer-vertraege',
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './vertraege.html'
})
export class Vertraege implements OnInit {
  private http = inject(HttpClient);

  readonly plots = signal<Plot[]>([]);
  readonly policies = signal<Policy[]>([]);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private pending = 0;
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  policyForm = { plotId: 0, coverageEur: 25000, deductible: 'TEN_PERCENT', coverageStart: '', coverageEnd: '' };

  readonly deductibles = DEDUCTIBLES;

  ngOnInit(): void {
    this.pending = 2;
    this.http.get<Plot[]>('/api/plots').subscribe({
      next: p => {
        this.plots.set(p);
        if (!this.policyForm.plotId && p.length) this.policyForm.plotId = p[0].id;
        this.settle();
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.settle();
      }
    });
    this.http.get<Policy[]>('/api/policies').subscribe({
      next: p => {
        this.policies.set(p);
        this.settle();
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.settle();
      }
    });
  }

  private settle(): void {
    if (--this.pending === 0) this.loading.set(false);
  }

  createPolicy(): void {
    this.error.set('');
    this.http.post<Policy>('/api/policies', { ...this.policyForm, status: 'ACTIVE' }).subscribe({
      next: () => {
        this.notify('Police angelegt');
        this.http.get<Policy[]>('/api/policies').subscribe(p => this.policies.set(p));
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  /** Inline success feedback (auto-dismisses). */
  private notify(msg: string): void {
    this.flash.set(msg);
    clearTimeout(this.flashTimer);
    this.flashTimer = setTimeout(() => this.flash.set(''), 3000);
  }
}
