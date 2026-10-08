import { Component, computed, input } from '@angular/core';

/** Shimmering loading placeholder — renders `lines` stacked skeleton bars. */
@Component({
  selector: 'app-skeleton',
  template: `
    <div class="skeleton-group" aria-hidden="true">
      @for (bar of bars(); track bar) {
        <span class="skeleton"></span>
      }
    </div>
  `
})
export class Skeleton {
  readonly lines = input(3);
  readonly bars = computed(() => Array.from({ length: this.lines() }, (_, i) => i));
}
