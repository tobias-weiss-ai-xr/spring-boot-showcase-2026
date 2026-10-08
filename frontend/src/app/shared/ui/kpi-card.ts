import { Component, input } from '@angular/core';

/** KPI tile for 360° dashboards: big value + label, optional hint line. */
@Component({
  selector: 'app-kpi-card',
  template: `
    <div class="card kpi">
      <span class="kpi-value">{{ value() }}</span>
      <span class="kpi-label">{{ label() }}</span>
      @if (hint()) {
        <span class="kpi-hint">{{ hint() }}</span>
      }
    </div>
  `
})
export class KpiCard {
  readonly label = input.required<string>();
  readonly value = input.required<string | number | null>();
  readonly hint = input('');
}
