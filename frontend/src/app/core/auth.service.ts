import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse, RegisterRequest, Insured } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'cropguard.auth';
  private readonly session = signal<LoginResponse | null>(this.load());
  readonly user = this.session.asReadonly();
  readonly role = computed(() => this.session()?.role ?? null);
  readonly insuredId = computed(() => this.session()?.id ?? null);
  readonly token = computed(() => this.session()?.token ?? null);

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', credentials)
      .pipe(tap(session => this.save(session)));
  }

  register(dto: RegisterRequest): Observable<Insured> {
    return this.http.post<Insured>('/api/insureds', dto);
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.session.set(null);
  }

  private save(session: LoginResponse): void {
    localStorage.setItem(this.storageKey, JSON.stringify(session));
    this.session.set(session);
  }

  private load(): LoginResponse | null {
    const raw = localStorage.getItem(this.storageKey);
    return raw ? JSON.parse(raw) as LoginResponse : null;
  }
}
