import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { httpErrorDetail } from '../../core/errors';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html'
})
export class Login {
  readonly email = signal('');
  readonly password = signal('');
  readonly busy = signal(false);
  readonly error = signal('');

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error.set('');
    this.busy.set(true);
    this.auth.login({ email: this.email(), password: this.password() })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.router.navigate([this.auth.role() === 'ASSESSOR' ? 'assessor' : 'farmer']),
        error: err => this.error.set(httpErrorDetail(err))
      });
  }
}
