/**
 * Shared HTTP client abstraction using Fetch API.
 */

import type { ApiRequestOptions } from '@/types/api/common';

const BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export interface ApiClient {
  get<TResponse>(path: string, options?: ApiRequestOptions): Promise<TResponse>;
  post<TResponse, TBody>(
    path: string,
    body: TBody,
    options?: ApiRequestOptions,
  ): Promise<TResponse>;
}

async function handleResponse<TResponse>(response: Response): Promise<TResponse> {
  const data = await response.json();
  
  if (!response.ok) {
    // Throw error object that matches the expected ApiErrorResponseDto shape
    throw { response: data };
  }
  
  return data as TResponse;
}

export const apiClient: ApiClient = {
  async get<TResponse>(path: string, options?: ApiRequestOptions) {
    const url = `${BASE_URL}${path}`;
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });
    return handleResponse<TResponse>(response);
  },
  async post<TResponse, TBody>(path: string, body: TBody, options?: ApiRequestOptions) {
    const url = `${BASE_URL}${path}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
      body: JSON.stringify(body),
    });
    return handleResponse<TResponse>(response);
  },
};
