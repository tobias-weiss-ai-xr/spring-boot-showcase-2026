import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService smoke test', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  it('logs in via /api/auth/login and exposes the role', () => {
    service.login({ email: 'max@bauernhof.de', password: 'passwort123' }).subscribe(session => {
      expect(session.role).toBe('FARMER');
      expect(service.role()).toBe('FARMER');
      expect(service.insuredId()).toBe(1);
    });

    const req = http.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ email: 'max@bauernhof.de', password: 'passwort123' });
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({ token: 'jwt', id: 1, name: 'Max', role: 'FARMER' });
  });

  it('clears the session on logout', () => {
    service.login({ email: 'a@b.de', password: 'pw' }).subscribe();
    http.expectOne('/api/auth/login').flush({ token: 'jwt', id: 2, name: 'Lisa', role: 'ASSESSOR' });
    expect(service.role()).toBe('ASSESSOR');
    service.logout();
    expect(service.role()).toBeNull();
  });
});
