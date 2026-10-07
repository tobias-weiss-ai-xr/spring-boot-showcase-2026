import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <div class="auth-card">
      <h1>Zugriff verweigert</h1>
      <p class="muted">Dieser Bereich ist für Ihre Rolle nicht freigegeben.</p>
      <p><a routerLink="/login">Zur Anmeldung</a></p>
    </div>
  `
})
export class Forbidden {}
