import { Component, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';

import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly auth = inject(AuthService);

  constructor(private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['login']);
  }
}
