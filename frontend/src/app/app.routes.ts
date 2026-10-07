import { Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { FarmerDashboard } from './pages/farmer/dashboard';
import { AssessorDashboard } from './pages/assessor/dashboard';
import { Forbidden } from './pages/forbidden/forbidden';
import { farmerGuard, assessorGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'farmer', component: FarmerDashboard, canActivate: [farmerGuard] },
  { path: 'assessor', component: AssessorDashboard, canActivate: [assessorGuard] },
  { path: 'forbidden', component: Forbidden },
  { path: '**', redirectTo: 'login' }
];
