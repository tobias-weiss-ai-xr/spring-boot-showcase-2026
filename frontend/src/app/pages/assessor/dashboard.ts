import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { Claim, HailEvent, AssessRequest } from '../../models';

@Component({
  selector: 'app-assessor-dashboard',
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './dashboard.html'
})
export class AssessorDashboard implements OnInit {
  private http = inject(HttpClient);

  readonly claims = signal<Claim[]>([]);
  readonly events = signal<HailEvent[]>([]);
  readonly statusFilter = signal('ALL');
  readonly selected = signal<Claim | null>(null);
  readonly error = signal('');

  assess: AssessRequest = { damagePercent: 0, decision: 'APPROVED', assessorNotes: '' };

  readonly statuses = ['ALL', 'SUBMITTED', 'ASSESSED', 'APPROVED', 'REJECTED'];
  readonly filtered = computed(() =>
    this.statusFilter() === 'ALL'
      ? this.claims()
      : this.claims().filter(c => c.status === this.statusFilter()));

  ngOnInit(): void {
    this.refreshClaims();
    this.http.get<HailEvent[]>('/api/hail-events').subscribe({
      next: e => this.events.set(e),
      error: e => this.error.set(httpErrorDetail(e))
    });
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
