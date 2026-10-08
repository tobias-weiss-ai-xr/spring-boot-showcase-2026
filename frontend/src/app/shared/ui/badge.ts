import { Component, input } from '@angular/core';

/** Status pill — text stays the raw backend enum (e2e asserts on it); tint comes from `.badge.<lowercase status>`. */
@Component({
  selector: 'app-badge',
  template: `<span class="badge" [class]="'badge ' + status().toLowerCase()">{{ status() }}</span>`
})
export class Badge {
  readonly status = input.required<string>();
}
