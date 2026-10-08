import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { Badge } from '../../shared/ui/badge';
import { ClaimWizard } from './claim-wizard';
import { Claim, HailEvent, Policy } from '../../models';

/** Farmer portal — Schäden: FNOL wizard (Schaden melden) + claim history. */
@Component({
  selector: 'app-farmer-schaeden',
  imports: [ClaimWizard, Badge, CurrencyPipe, DatePipe],
  templateUrl: './schaeden.html'
})
export class Schaeden implements OnInit {
  private http = inject(HttpClient);

  readonly policies = signal<Policy[]>([]);
  readonly claims = signal<Claim[]>([]);
  readonly hailEvents = signal<HailEvent[]>([]);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private pending = 0;
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.pending = 3;
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
    this.http.get<Claim[]>('/api/claims').subscribe({
      next: c => {
        this.claims.set(c);
        this.settle();
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.settle();
      }
    });
    this.http.get<HailEvent[]>('/api/hail-events').subscribe({
      next: h => {
        this.hailEvents.set(h);
        this.settle();
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.settle();
      }
    });
  }

  /** Wizard finished a POST — prepend the new claim + success feedback. */
  onClaimed(claim: Claim): void {
    this.claims.update(cs => [claim, ...cs]);
    this.notify('Schaden gemeldet');
  }

  hailById(id: number | null): HailEvent | undefined {
    return id == null ? undefined : this.hailEvents().find(h => h.id === id);
  }

  private settle(): void {
    if (--this.pending === 0) this.loading.set(false);
  }

  /** Inline success feedback (auto-dismisses). */
  private notify(msg: string): void {
    this.flash.set(msg);
    clearTimeout(this.flashTimer);
    this.flashTimer = setTimeout(() => this.flash.set(''), 3000);
  }
}
