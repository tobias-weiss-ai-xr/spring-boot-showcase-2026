import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { Claim, Policy } from '../../models';

/** Farmer portal — Schäden: claim filing + claim history. */
@Component({
  selector: 'app-farmer-schaeden',
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './schaeden.html'
})
export class Schaeden implements OnInit {
  private http = inject(HttpClient);

  readonly policies = signal<Policy[]>([]);
  readonly claims = signal<Claim[]>([]);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private pending = 0;
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  claimForm = { policyId: 0, damageDate: '', damageDescription: '' };

  ngOnInit(): void {
    this.pending = 2;
    this.http.get<Policy[]>('/api/policies').subscribe({
      next: p => {
        this.policies.set(p);
        if (!this.claimForm.policyId && p.length) this.claimForm.policyId = p[0].id;
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
  }

  private settle(): void {
    if (--this.pending === 0) this.loading.set(false);
  }

  fileClaim(): void {
    this.error.set('');
    this.http.post<Claim>('/api/claims', this.claimForm).subscribe({
      next: () => {
        this.claimForm.damageDescription = '';
        this.notify('Schaden gemeldet');
        this.http.get<Claim[]>('/api/claims').subscribe(c => this.claims.set(c));
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
