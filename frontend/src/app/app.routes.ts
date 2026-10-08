import { Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Uebersicht } from './pages/farmer/uebersicht';
import { Anbau } from './pages/farmer/anbau';
import { Vertraege } from './pages/farmer/vertraege';
import { Schaeden } from './pages/farmer/schaeden';
import { Lage } from './pages/farmer/lage';
import { Aufgaben } from './pages/assessor/aufgaben';
import { Lagebild } from './pages/assessor/lagebild';
import { Forbidden } from './pages/forbidden/forbidden';
import { farmerGuard, assessorGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  {
    path: 'farmer',
    canActivate: [farmerGuard],
    children: [
      { path: '', redirectTo: 'uebersicht', pathMatch: 'full' }, // legacy /farmer deep link
      { path: 'uebersicht', component: Uebersicht, canActivate: [farmerGuard] },
      { path: 'anbau', component: Anbau, canActivate: [farmerGuard] },
      { path: 'vertraege', component: Vertraege, canActivate: [farmerGuard] },
      { path: 'schaeden', component: Schaeden, canActivate: [farmerGuard] },
      { path: 'lage', component: Lage, canActivate: [farmerGuard] },
    ],
  },
  {
    path: 'assessor',
    canActivate: [assessorGuard],
    children: [
      { path: '', redirectTo: 'aufgaben', pathMatch: 'full' }, // legacy /assessor deep link
      { path: 'aufgaben', component: Aufgaben, canActivate: [assessorGuard] },
      { path: 'lagebild', component: Lagebild, canActivate: [assessorGuard] },
    ],
  },
  { path: 'forbidden', component: Forbidden },
  { path: '**', redirectTo: 'login' }
];
