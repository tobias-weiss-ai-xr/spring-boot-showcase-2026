import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { httpErrorDetail } from '../../core/errors';
import { Claim, HailEvent, Policy } from '../../models';

/** FNOL wizard — 4 steps (Feld/Police → Ereignis → Umfang → Zusammenfassung), signal step-state.
 *  Submits the same payload as the previous inline form: { policyId, damageDate, damageDescription, hailEventId? }. */
@Component({
  selector: 'app-claim-wizard',
  imports: [FormsModule, DatePipe],
  templateUrl: './claim-wizard.html'
})
export class ClaimWizard {
  private http = inject(HttpClient);

  readonly policies = input.required<Policy[]>();
  readonly hailEvents = input.required<HailEvent[]>();
  readonly submitted = output<Claim>();

  readonly step = signal(1);
  readonly submitting = signal(false);
  readonly error = signal('');

  readonly policyId = signal(0);
  readonly damageDate = signal('');
  readonly hailEventId = signal<number | null>(null);
  readonly description = signal('');

  readonly stepLabels = ['Feld & Police', 'Ereignis', 'Umfang', 'Zusammenfassung'];

  constructor() {
    // preselect the first policy once the list arrives
    effect(() => {
      if (!this.policyId() && this.policies().length) this.policyId.set(this.policies()[0].id);
    });
  }

  /** Per-step gate: "Weiter" stays disabled until the current step is valid. */
  readonly stepValid = computed(() => {
    switch (this.step()) {
      case 1: return this.policyId() > 0;
      case 2: return this.damageDate() !== '';
      case 3: return this.description().trim().length >= 10;
      default: return true;
    }
  });

  readonly selectedPolicy = computed(() => this.policies().find(p => p.id === this.policyId()));
  readonly selectedHailEvent = computed(() => this.hailEvents().find(e => e.id === this.hailEventId()));

  /** Only completed steps are clickable (step-back). */
  goto(target: number): void {
    if (target >= 1 && target < this.step() && !this.submitting()) this.step.set(target);
  }

  next(): void {
    if (this.stepValid()) this.step.update(s => Math.min(s + 1, 4));
  }

  back(): void {
    this.step.update(s => Math.max(s - 1, 1));
  }

  confirm(): void {
    if (this.step() !== 4 || !this.stepValid() || this.submitting()) return;
    this.error.set('');
    this.submitting.set(true);
    const payload: Record<string, unknown> = {
      policyId: this.policyId(),
      damageDate: this.damageDate(),
      damageDescription: this.description()
    };
    if (this.hailEventId()) payload['hailEventId'] = this.hailEventId();
    this.http.post<Claim>('/api/claims', payload).subscribe({
      next: c => {
        this.submitting.set(false);
        this.damageDate.set('');
        this.hailEventId.set(null);
        this.description.set('');
        this.step.set(1);
        this.submitted.emit(c);
      },
      error: e => {
        this.submitting.set(false);
        this.error.set(httpErrorDetail(e));
      }
    });
  }
}
