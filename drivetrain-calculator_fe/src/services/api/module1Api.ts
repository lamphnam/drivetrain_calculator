/**
 * Frontend-facing Module 1 API service.
 */

import { apiClient } from './apiClient';
import { module1ApiEndpoints, type Module1ApiContract } from '@/services/api/contracts/module1.contract';

export const module1Api: Module1ApiContract = {
  async getReferenceValues(constantSetId) {
    const query = constantSetId ? `?constantSetId=${constantSetId}` : '';
    return apiClient.get(module1ApiEndpoints.referenceValues + query);
  },
  async calculate(request) {
    return apiClient.post(module1ApiEndpoints.calculate, request);
  },
  async getHistory() {
    return apiClient.get(module1ApiEndpoints.history);
  },
  async getHistoryById(designCaseId) {
    return apiClient.get(module1ApiEndpoints.historyById(designCaseId));
  },
};
