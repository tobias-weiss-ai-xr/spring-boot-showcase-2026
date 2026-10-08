import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/auth.service';
import { httpErrorDetail } from '../../core/errors';
import { Plot, CROP_TYPES, BUNDESLAENDER } from '../../models';

/** Farmer portal — Anbau: plot management (create + overview of fields). */
@Component({
  selector: 'app-farmer-anbau',
  imports: [FormsModule],
  templateUrl: './anbau.html'
})
export class Anbau implements OnInit {
  private auth = inject(AuthService);
  private http = inject(HttpClient);

  readonly plots = signal<Plot[]>([]);
  readonly error = signal('');
  readonly flash = signal('');
  readonly loading = signal(true);
  private flashTimer: ReturnType<typeof setTimeout> | undefined;

  plotForm = { cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN', coordinateE: 3700000, coordinateN: 5570000, locationDescription: 'Mein Feld' };

  readonly cropTypes = CROP_TYPES;
  readonly bundeslaender = BUNDESLAENDER;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.http.get<Plot[]>(`/api/plots?insuredId=${this.auth.insuredId()}`).subscribe({
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

  createPlot(): void {
    this.error.set('');
    this.http.post<Plot>('/api/plots', { ...this.plotForm, insuredId: this.auth.insuredId() }).subscribe({
      next: () => {
        this.plotForm.locationDescription = '';
        this.notify('Feld angelegt');
        this.refresh();
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
