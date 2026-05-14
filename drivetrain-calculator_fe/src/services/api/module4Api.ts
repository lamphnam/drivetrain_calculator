/**
 * Frontend-facing Module 4 API service.
 */

import { apiClient } from './apiClient';
import { module4ApiEndpoints, type Module4ApiContract } from '@/services/api/contracts/module4.contract';

export const module4Api: Module4ApiContract = {
  async calculate(request) {
    return apiClient.post(module4ApiEndpoints.calculate, request);
  },
  async getHistory(designCaseId) {
    return apiClient.get(module4ApiEndpoints.getHistory(designCaseId));
  },
};
