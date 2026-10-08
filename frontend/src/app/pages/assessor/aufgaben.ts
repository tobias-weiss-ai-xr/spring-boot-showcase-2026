import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { AssessRequest, Claim, HailEvent } from '../../models';
import { KpiCard } from '../../shared/ui/kpi-card';
import { Skeleton } from '../../shared/ui/skeleton';

/** Final claim states — everything else is open work for the assessor. */
const CLAIM_CLOSED = ['APPROVED', 'REJECTED', 'PAID'];

/** Severity rank for queue sorting; claims without a linked hail event rank last. */
const SEVERITY_RANK: Record<string, number> = { LIGHT: 0, MODERATE: 1, SEVERE: 2, DEVASTATING: 3 };

/** Assessor workbench — Aufgaben: KPIs, filterable/sortable claim queue + assess panel. */
@Component({
  selector: 'app-assessor-aufgaben',
  imports: [FormsModule, CurrencyPipe, DatePipe, KpiCard, Skeleton],
  templateUrl: './aufgaben.html'
})
export class Aufgaben implements OnInit {
  private http = inject(HttpClient);

  readonly claims = signal<Claim[]>([]);
  private readonly hailSeverities = signal<Record<number, string>>({});
  readonly statusFilter = signal('ALL');
  readonly sort = signal('newest');
  readonly selected = signal<Claim | null>(null);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  assess: AssessRequest = { damagePercent: 0, decision: 'APPROVED', assessorNotes: '' };

  readonly statuses = ['ALL', 'SUBMITTED', 'UNDER_REVIEW', 'ASSESSED', 'APPROVED', 'REJECTED', 'PAID'];
  readonly sortModes = [
    { value: 'newest', label: 'Neueste zuerst' },
    { value: 'oldest', label: 'Älteste zuerst' },
    { value: 'severity', label: 'Schweregrad' }
  ];

  readonly openClaims = computed(() => this.claims().filter(c => !CLAIM_CLOSED.includes(c.status)));
  readonly openAmount = computed(() => this.openClaims().reduce((s, c) => s + c.policy.coverageEur, 0));
  readonly payoutTotal = computed(() => this.claims().reduce((s, c) => s + (c.payoutEur ?? 0), 0));

  readonly filtered = computed(() => {
    const sev = this.hailSeverities();
    const rank = (c: Claim): number => SEVERITY_RANK[sev[c.hailEventId ?? -1] ?? ''] ?? -1;
    const list = this.statusFilter() === 'ALL'
      ? [...this.claims()]
      : this.claims().filter(c => c.status === this.statusFilter());
    switch (this.sort()) {
      case 'oldest': return list.sort((a, b) => a.damageDate.localeCompare(b.damageDate));
      case 'severity': return list.sort((a, b) => rank(b) - rank(a) || a.damageDate.localeCompare(b.damageDate));
      default: return list.sort((a, b) => b.damageDate.localeCompare(a.damageDate));
    }
  });

  ngOnInit(): void {
    let pending = 2;
    const settle = (e?: unknown): void => {
      if (e) this.error.set(httpErrorDetail(e));
      if (--pending === 0) this.loading.set(false);
    };
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => { this.claims.set(c); settle(); },
      error: e => settle(e)
    });
    // hail events only feed the severity column / sort — a failure here is not fatal
    this.http.get<HailEvent[]>('/api/hail-events').subscribe({
      next: events => {
        const map: Record<number, string> = {};
        for (const e of events) map[e.id] = e.severity;
        this.hailSeverities.set(map);
        settle();
      },
      error: e => settle(e)
    });
  }

  /** Entry age: full days since the damage was reported. */
  ageDays(claim: Claim): number {
    return Math.max(0, Math.floor((Date.now() - Date.parse(claim.damageDate)) / 86_400_000));
  }

  /** Severity of the causing hail event, if the claim is linked to one. */
  severityOf(claim: Claim): string | null {
    return this.hailSeverities()[claim.hailEventId ?? -1] ?? null;
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
        this.notify(`Schadenfall #${claim.id} bearbeitet (${this.assess.decision})`);
        this.refreshClaims();
      },
      error: e => this.error.set(httpErrorDetail(e))
    });
  }

  refreshClaims(): void {
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => this.claims.set(c),
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
