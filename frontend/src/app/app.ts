import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './core/auth.service';

interface NavItem {
  path: string;
  label: string;
}

const FARMER_NAV: NavItem[] = [
  { path: '/farmer/uebersicht', label: 'Übersicht' },
  { path: '/farmer/anbau', label: 'Anbau' },
  { path: '/farmer/vertraege', label: 'Meine Verträge' },
  { path: '/farmer/schaeden', label: 'Schaden' },
  { path: '/farmer/lage', label: 'Wetter & Lage' },
];

const ASSESSOR_NAV: NavItem[] = [
  { path: '/assessor/aufgaben', label: 'Aufgaben' },
  { path: '/assessor/lagebild', label: 'Lagebild' },
];

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly auth = inject(AuthService);

  protected readonly nav = computed<NavItem[]>(() => {
    switch (this.auth.role()) {
      case 'FARMER': return FARMER_NAV;
      case 'ASSESSOR': return ASSESSOR_NAV;
      default: return [];
    }
  });

  protected readonly portalTitle = computed(() =>
    this.auth.role() === 'ASSESSOR' ? 'Sachverständigen-Portal' : 'MeineCropGuard — Ihr Portal');

  constructor(private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['login']);
  }
}
