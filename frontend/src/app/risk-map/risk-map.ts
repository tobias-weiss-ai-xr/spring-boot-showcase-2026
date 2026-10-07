import { Component, OnInit, effect, inject, input, signal, viewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Plot, RiskMapData } from '../models';

/** Color scale for drought index 0–10: light green (low) → dark red (high). */
const COLORS = [
  '#c8e6c9', '#66bb6a', '#8bc34a', '#c0ca33', '#ffee58', '#ffca28',
  '#ffa726', '#ff7043', '#ef5350', '#e53935', '#b71c1c'
];

/**
 * Canvas heat map of the DWD drought index grid with plot markers.
 * Coordinates: GK3 meters — same system as the grid, so markers map 1:1.
 */
@Component({
  selector: 'app-risk-map',
  templateUrl: './risk-map.html',
  styles: `
    .map-wrap { position: relative; }
    canvas { width: 100%; height: auto; display: block; border-radius: 6px; }
    .map-tip {
      position: absolute; pointer-events: none; z-index: 2;
      background: #1f2d1f; color: #fff; font-size: 0.75rem;
      padding: 2px 8px; border-radius: 4px;
      transform: translate(-50%, -150%); white-space: nowrap;
    }
    .legend {
      display: flex; align-items: center; gap: 2px; margin-top: 0.5rem;
      font-size: 0.75rem; color: var(--muted);
    }
    .legend .chip { width: 16px; height: 10px; display: inline-block; }
  `
})
export class RiskMap implements OnInit {
  private http = inject(HttpClient);

  readonly plots = input<Plot[]>([]);
  readonly data = signal<RiskMapData | null>(null);
  readonly tip = signal<{ x: number; y: number; text: string } | null>(null);
  readonly colors = COLORS;

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('cv');

  constructor() {
    effect(() => {
      if (this.data() && this.plots()) this.draw();
    });
  }

  ngOnInit(): void {
    this.http.get<RiskMapData>('/api/risk-map', { params: { step: 4 } })
      .subscribe(m => {
        this.data.set(m);
        this.draw();
      });
  }

  private draw(): void {
    const m = this.data();
    if (!m) return;
    const cv = this.canvas().nativeElement;
    const ctx = cv.getContext('2d');
    if (!ctx) return;
    const cw = cv.width / m.ncols;
    const ch = cv.height / m.nrows;
    for (let r = 0; r < m.nrows; r++) {
      for (let c = 0; c < m.ncols; c++) {
        const v = m.values[r][c];
        ctx.fillStyle = v < 0 || v === m.nodata ? '#243322' : COLORS[Math.min(v, 10)];
        ctx.fillRect(c * cw, r * ch, cw + 0.5, ch + 0.5);
      }
    }
    const eff = m.cellsize * m.step;
    ctx.lineWidth = 1.5;
    for (const p of this.plots()) {
      if (p.coordinateE == null || p.coordinateN == null) continue;
      const x = (p.coordinateE - m.xllcorner) / eff * cw;
      const y = (m.ymax - p.coordinateN) / eff * ch;
      ctx.beginPath();
      ctx.arc(x, y, 5, 0, 2 * Math.PI);
      ctx.fillStyle = '#ffffff';
      ctx.fill();
      ctx.strokeStyle = '#1b5e20';
      ctx.stroke();
    }
  }

  onMove(ev: MouseEvent): void {
    const m = this.data();
    if (!m) return;
    const rect = this.canvas().nativeElement.getBoundingClientRect();
    const fx = Math.min(Math.max((ev.clientX - rect.left) / rect.width, 0), 0.9999);
    const fy = Math.min(Math.max((ev.clientY - rect.top) / rect.height, 0), 0.9999);
    const c = Math.floor(fx * m.ncols);
    const r = Math.floor(fy * m.nrows);
    const v = m.values[r][c];
    let text = v < 0 ? 'außerhalb des Rasters' : `Trockenindex ${v}`;
    const eff = m.cellsize * m.step;
    const x = fx * m.ncols;
    const y = fy * m.nrows;
    for (const p of this.plots()) {
      if (p.coordinateE == null || p.coordinateN == null) continue;
      const px = (p.coordinateE - m.xllcorner) / eff;
      const py = (m.ymax - p.coordinateN) / eff;
      if (Math.hypot(px - x, py - y) < 8) {
        text = `${p.locationDescription} · ${p.cropType}`;
        break;
      }
    }
    this.tip.set({ x: ev.offsetX, y: ev.offsetY, text });
  }
}
