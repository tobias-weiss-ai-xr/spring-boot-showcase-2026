import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { httpErrorDetail } from '../../core/errors';
import { RiskMap } from '../../risk-map/risk-map';
import { Skeleton } from '../../shared/ui/skeleton';
import { Plot } from '../../models';

/** Farmer portal — Lage: DWD drought risk map for the farmer's plots. */
@Component({
  selector: 'app-farmer-lage',
  imports: [RiskMap, Skeleton],
  templateUrl: './lage.html'
})
export class Lage implements OnInit {
  private http = inject(HttpClient);

  readonly plots = signal<Plot[]>([]);
  readonly error = signal('');
  readonly loading = signal(true);

  ngOnInit(): void {
    this.http.get<Plot[]>('/api/plots').subscribe({
      next: p => {
        this.plots.set(p);
        this.loading.set(false);
      },
      error: e => {
        this.error.set(httpErrorDetail(e));
        this.loading.set(false);
      }
    });
  }
}
