/** Extracts a readable message from an HTTP error (backend returns RFC 7807 ProblemDetail). */
export function httpErrorDetail(err: unknown): string {
  const e = err as { error?: { detail?: string; title?: string }; message?: string; status?: number };
  if (e?.error?.detail) return e.error.detail;
  if (e?.error?.title) return e.error.title;
  if (e?.message) return e.message;
  return 'Request failed';
}
