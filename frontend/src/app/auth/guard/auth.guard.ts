import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const authRoutes = ['/login', '/register'];
  if (authRoutes.includes(state.url)) {
    if (!authService.loggedIn){
      return true;
    }
    return router.navigateByUrl('/pacientes');
  } else {
    if (authService.loggedIn){
      return true;
    }
    return router.navigateByUrl('/login');
  }
};
