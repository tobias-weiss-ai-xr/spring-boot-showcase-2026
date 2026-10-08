import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { httpErrorDetail } from '../../core/errors';
import { RiskMap } from '../../risk-map/risk-map';
import { HailEvent, Plot } from '../../models';

/** Assessor portal — Lagebild: hail-event feed + DWD drought risk map. */
@Component({
  selector: 'app-assessor-lagebild',
  imports: [FormsModule, DatePipe, RiskMap],
  templateUrl: './lagebild.html'
})
export class Lagebild implements OnInit {
  private http = inject(HttpClient);

  readonly events = signal<HailEvent[]>([]);
  readonly plots = signal<Plot[]>([]);
  readonly error = signal('');
  readonly loading = signal(true);
  private pending = 0;

  ngOnInit(): void {
    this.pending = 2;
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
}
