import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { httpErrorDetail } from '../../core/errors';
import { Claim, Policy, QuoteResult, BUNDESLAENDER, CROP_TYPES, DEDUCTIBLES } from '../../models';
import { Badge } from '../../shared/ui/badge';
import { EmptyState } from '../../shared/ui/empty-state';
import { KpiCard } from '../../shared/ui/kpi-card';
import { Skeleton } from '../../shared/ui/skeleton';

/** Final claim states — everything else counts as "open". */
const CLAIM_CLOSED = ['APPROVED', 'REJECTED', 'PAID'];

/** Farmer portal — Übersicht: customer-360 (KPIs, Policen, Schaden-Timeline) + live Quote-Rechner. */
@Component({
  selector: 'app-farmer-uebersicht',
  imports: [FormsModule, CurrencyPipe, DecimalPipe, DatePipe, RouterLink, Badge, EmptyState, KpiCard, Skeleton],
  templateUrl: './uebersicht.html'
})
export class Uebersicht implements OnInit {
  private http = inject(HttpClient);

  readonly quoteResult = signal<QuoteResult | null>(null);
  readonly error = signal('');

  readonly policies = signal<Policy[]>([]);
  readonly claims = signal<Claim[]>([]);
  readonly dataError = signal('');
  readonly loading = signal(true);

  readonly activePolicies = computed(() => this.policies().filter(p => p.status === 'ACTIVE'));
  readonly totalCoverage = computed(() => this.activePolicies().reduce((sum, p) => sum + p.coverageEur, 0));
  readonly openClaims = computed(() => this.claims().filter(c => !CLAIM_CLOSED.includes(c.status)));

  /** MeineVH-style Kacheln (tile navigation with live counts). */
  readonly tiles = computed(() => [
    { path: '/farmer/anbau', icon: '🌱', label: 'Anbau', count: 'Anbauverzeichnis führen (WEB AV)' },
    { path: '/farmer/vertraege', icon: '📄', label: 'Meine Verträge', count: `${this.activePolicies().length} aktiv` },
    { path: '/farmer/schaeden', icon: '⛈️', label: 'Schaden', count: `${this.openClaims().length} offen` },
    { path: '/farmer/lage', icon: '🌦️', label: 'Wetter & Lage', count: 'Risiko- und Ereignislage' },
  ]);
  readonly recentClaims = computed(() =>
    [...this.claims()].sort((a, b) => b.damageDate.localeCompare(a.damageDate)).slice(0, 5));

  quote = { cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN', deductible: 'TEN_PERCENT', coverageEur: 25000, coordinateE: 3700000, coordinateN: 5570000 };

  readonly cropTypes = CROP_TYPES;
  readonly bundeslaender = BUNDESLAENDER;
  readonly deductibles = DEDUCTIBLES;

  ngOnInit(): void {
    let pending = 2;
    const settle = (e?: unknown): void => {
      if (e) this.dataError.set(httpErrorDetail(e));
      if (--pending === 0) this.loading.set(false);
    };
    this.http.get<Policy[]>('/api/policies').subscribe({
      next: p => { this.policies.set(p); settle(); },
      error: e => settle(e)
    });
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => { this.claims.set(c); settle(); },
      error: e => settle(e)
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
}
