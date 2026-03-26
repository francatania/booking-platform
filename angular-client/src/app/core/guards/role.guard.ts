import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserService } from '../services/user.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => () => {
  const role = inject(UserService).getRole();
  return role && allowedRoles.includes(role)
    ? true
    : inject(Router).createUrlTree(['/login']);
};
