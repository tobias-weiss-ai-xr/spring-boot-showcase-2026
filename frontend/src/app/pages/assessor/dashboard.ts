import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { RiskMap } from '../../risk-map/risk-map';
import { Claim, HailEvent, AssessRequest, Plot } from '../../models';

@Component({
  selector: 'app-assessor-dashboard',
  imports: [FormsModule, CurrencyPipe, DatePipe, RiskMap],
  templateUrl: './dashboard.html'
})
export class AssessorDashboard implements OnInit {
  private http = inject(HttpClient);

  readonly claims = signal<Claim[]>([]);
  readonly events = signal<HailEvent[]>([]);
  readonly plots = signal<Plot[]>([]);
  readonly statusFilter = signal('ALL');
  readonly selected = signal<Claim | null>(null);
  readonly error = signal('');
  readonly loading = signal(true);
  private pending = 0;

  assess: AssessRequest = { damagePercent: 0, decision: 'APPROVED', assessorNotes: '' };

  readonly statuses = ['ALL', 'SUBMITTED', 'ASSESSED', 'APPROVED', 'REJECTED'];
  readonly filtered = computed(() =>
    this.statusFilter() === 'ALL'
      ? this.claims()
      : this.claims().filter(c => c.status === this.statusFilter()));

  ngOnInit(): void {
    this.pending = 3;
    this.refreshClaims();
    this.http.get<HailEvent[]>('/api/hail-events').subscribe({
      next: e => {
        this.events.set(e);
        this.settle();
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.settle();
      }
    });
    this.http.get<Plot[]>('/api/plots').subscribe({
      next: p => {
        this.plots.set(p);
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

  refreshClaims(): void {
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => this.claims.set(c),
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  open(claim: Claim): void {
    this.selected.set(claim);
    this.assess = {
      damagePercent: claim.damagePercent ?? 0,
      decision: 'APPROVED',
      assessorNotes: claim.assessorNotes ?? ''
    };
  }

  submitAssess(): void {
    const claim = this.selected();
    if (!claim) return;
    this.error.set('');
    this.http.put<Claim>(`/api/claims/${claim.id}/assess`, this.assess).subscribe({
      next: () => {
        this.selected.set(null);
        this.refreshClaims();
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }
}
