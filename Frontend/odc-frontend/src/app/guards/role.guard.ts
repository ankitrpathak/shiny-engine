import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRoles: string[] = route.data['roles'] || [];
  const currentRole = authService.getRole();

  if (currentRole && (expectedRoles.includes(currentRole) || currentRole === 'GOD')) {
    return true;
  }

  // Redirect to appropriate home based on current role
  if (currentRole === 'WASHER') {
    router.navigate(['/washer']);
  } else if (currentRole === 'ADMIN') {
    router.navigate(['/admin']);
  } else {
    router.navigate(['/dashboard']);
  }

  return false;
};
