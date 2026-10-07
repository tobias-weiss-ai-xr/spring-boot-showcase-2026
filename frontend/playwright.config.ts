import { defineConfig } from '@playwright/test';

/**
 * CropGuard E2E suite.
 * webServer auto-starts backend (:8080) and Angular dev server (:4200);
 * reuseExistingServer keeps dev servers you already started.
 */
export default defineConfig({
  testDir: './e2e/tests',
  fullyParallel: false, // single worker: tests share the H2 backend; each seeds its own data
  workers: 1,
  retries: process.env.CI ? 2 : 0,
  reporter: [['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    navigationTimeout: 30_000,
  },
  webServer: [
    {
      command: 'C:\\Users\\Tobias\\apache-maven-3.9.9\\bin\\mvn.cmd -q spring-boot:run',
      cwd: '..',
      url: 'http://localhost:8080/actuator/health',
      reuseExistingServer: true,
      timeout: 120_000,
      env: { ...process.env, JAVA_HOME: 'C:\\Program Files\\Java\\jdk-22' },
    },
    {
      command: 'npm start',
      cwd: '.',
      url: 'http://localhost:4200',
      reuseExistingServer: true,
      timeout: 120_000,
    },
  ],
});
