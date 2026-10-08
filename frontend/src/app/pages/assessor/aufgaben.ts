import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { Claim, AssessRequest } from '../../models';

/** Assessor portal — Aufgaben: claim workbench queue + assessment panel. */
@Component({
  selector: 'app-assessor-aufgaben',
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './aufgaben.html'
})
export class Aufgaben implements OnInit {
  private http = inject(HttpClient);

  readonly claims = signal<Claim[]>([]);
  readonly statusFilter = signal('ALL');
  readonly selected = signal<Claim | null>(null);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  assess: AssessRequest = { damagePercent: 0, decision: 'APPROVED', assessorNotes: '' };

  readonly statuses = ['ALL', 'SUBMITTED', 'ASSESSED', 'APPROVED', 'REJECTED'];
  readonly filtered = computed(() =>
    this.statusFilter() === 'ALL'
      ? this.claims()
      : this.claims().filter(c => c.status === this.statusFilter()));

  ngOnInit(): void {
    this.refreshClaims();
  }

  refreshClaims(): void {
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => {
        this.claims.set(c);
        this.loading.set(false);
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.loading.set(false);
      }
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
        this.notify(`Schadenfall #${claim.id} bearbeitet (${this.assess.decision})`);
        this.refreshClaims();
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
