import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { httpErrorDetail } from '../../core/errors';
import { BUNDESLAENDER } from '../../models';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html'
})
export class Register {
  readonly name = signal('');
  readonly email = signal('');
  readonly password = signal('');
  readonly bundesland = signal(BUNDESLAENDER[0]);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly bundeslaender = BUNDESLAENDER;

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error.set('');
    this.busy.set(true);
    this.auth.register({
      name: this.name(), email: this.email(), password: this.password(),
      role: 'FARMER', bundesland: this.bundesland()
    })
      .pipe(
        finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.auth.login({ email: this.email(), password: this.password() })
          .subscribe({
            next: () => this.router.navigate(['farmer']),
            error: err => this.error.set(httpErrorDetail(err))
          }),
        error: err => this.error.set(httpErrorDetail(err))
      });
  }
}
