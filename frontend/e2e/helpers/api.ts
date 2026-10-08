import { APIRequestContext } from 'playwright';

/**
 * Setup helpers driving the backend REST API directly.
 * Every test seeds its own data (unique emails, unique descriptions) so the
 * suite stays deterministic against the shared H2 dev database.
 */

const API = 'http://localhost:8080/api';
const PASSWORD = 'geheim123';

let counter = 0;
export function uniqueEmail(): string {
  return `e2e-${Date.now()}-${counter++}@test.dev`;
}

export async function registerFarmer(request: APIRequestContext, email: string): Promise<string> {
  const res = await request.post(`${API}/insureds`, {
    data: { name: 'E2E Bauer', email, password: PASSWORD, role: 'FARMER', bundesland: 'HESSEN' },
  });
  expectOk(res, 'register farmer');
  return email;
}

export async function login(
  request: APIRequestContext,
  email: string,
  password = PASSWORD,
): Promise<{ token: string; id: number; role: string }> {
  const res = await request.post(`${API}/auth/login`, { data: { email, password } });
  expectOk(res, 'login');
  return res.json();
}

/** Seeds plot + active policy for a fresh farmer; returns the farmer email and policy id. */
export async function seedPolicy(
  request: APIRequestContext,
  email = uniqueEmail(),
): Promise<{ email: string; policyId: number }> {
  await registerFarmer(request, email);
  const { token, id } = await login(request, email);

  const plotRes = await request.post(`${API}/plots`, {
    headers: auth(token),
    data: {
      cropType: 'WHEAT', hectares: 25, bundesland: 'HESSEN',
      coordinateE: 3700000, coordinateN: 5570000,
      locationDescription: 'E2E Testfeld', insuredId: id,
    },
  });
  expectOk(plotRes, 'create plot');
  const plotId = (await plotRes.json()).id;

  const policyRes = await request.post(`${API}/policies`, {
    headers: auth(token),
    data: {
      coverageEur: 25000, deductible: 'TEN_PERCENT', status: 'ACTIVE',
      coverageStart: '2026-03-01', coverageEnd: '2026-12-31', plotId,
    },
  });
  expectOk(policyRes, 'create policy');
  const policyId = (await policyRes.json()).id;

  return { email, policyId };
}

/** Seeds the full claim pipeline and returns the unique damage description. */
export async function seedClaim(
  request: APIRequestContext,
  description = `Hagelschaden ${uniqueEmail()}`,
): Promise<{ email: string; description: string }> {
  const { email, policyId } = await seedPolicy(request);
  const { token } = await login(request, email);

  const claimRes = await request.post(`${API}/claims`, {
    headers: auth(token),
    data: { damageDate: '2026-07-15', damageDescription: description, policyId },
  });
  expectOk(claimRes, 'create claim');

  return { email, description };
}

function auth(token: string): Record<string, string> {
  return { Authorization: `Bearer ${token}` };
}

function expectOk(res: { ok(): boolean; status(): number }, step: string): void {
  if (!res.ok()) {
    throw new Error(`${step} failed with ${res.status()}`);
  }
}
