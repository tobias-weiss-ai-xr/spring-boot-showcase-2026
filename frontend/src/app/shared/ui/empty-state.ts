import { Component, input } from '@angular/core';

/** Friendly zero-data placeholder for empty lists. */
@Component({
  selector: 'app-empty-state',
  template: `
    <div class="empty-state">
      <span class="empty-icon" aria-hidden="true">{{ icon() }}</span>
      <p>{{ message() }}</p>
    </div>
  `
})
export class EmptyState {
  readonly message = input.required<string>();
  readonly icon = input('🌾');
}
