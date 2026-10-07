import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

function guard(role: string): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const current = auth.role();
    if (current === role) {
      return true;
    }
    return router.createUrlTree([current ? 'forbidden' : 'login']);
  };
}

export const farmerGuard = guard('FARMER');
export const assessorGuard = guard('ASSESSOR');
