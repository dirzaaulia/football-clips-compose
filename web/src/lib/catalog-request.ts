/** Retry a transient catalog failure once; callers still receive real errors. */
export async function catalogRequest(url: string): Promise<Response> {
  for (let attempt = 0; attempt < 2; attempt += 1) {
    try {
      const response = await fetch(url, {
        next: { revalidate: 30 },
        signal: AbortSignal.timeout(15_000),
      });
      if (response.status < 500 || attempt === 1) return response;
      await response.body?.cancel();
    } catch (error) {
      if (attempt === 1) throw error;
    }
    await new Promise(resolve => setTimeout(resolve, 400));
  }
  throw new Error('Catalog request failed; retry.');
}
