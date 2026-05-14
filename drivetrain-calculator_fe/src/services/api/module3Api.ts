/**
 * Frontend-facing Module 3 API service.
 */

import { apiClient } from './apiClient';
import { module3ApiEndpoints, type Module3ApiContract } from '@/services/api/contracts/module3.contract';

export const module3Api: Module3ApiContract = {
  async getMaterials() {
    return apiClient.get(module3ApiEndpoints.getMaterials);
  },
  async calculate(request) {
    return apiClient.post(module3ApiEndpoints.calculate, request);
  },
  async getHistory(designCaseId) {
    return apiClient.get(module3ApiEndpoints.getHistory(designCaseId));
  },
};
