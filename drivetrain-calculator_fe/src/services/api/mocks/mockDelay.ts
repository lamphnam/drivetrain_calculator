/**
 * Mock-only latency helper that simulates asynchronous API response time.
 */

export async function wait(durationMs = 300): Promise<void> {
  await new Promise((resolve) => {
    setTimeout(resolve, durationMs);
  });
}
