/**
 * Lightweight FE-only store for current result and recent Module 1 history entries.
 */

import { useSyncExternalStore } from 'react';

import type { Module1CalculationResponseDto } from '@/types/api/module1';

export type Module1HistoryEntry = {
  id: string;
  designCaseId: number;
  createdAt: string;
  title: string;
  summary: string;
  result: Module1CalculationResponseDto;
};

type Module1HistoryState = {
  currentResultId: string | null;
  entries: Module1HistoryEntry[];
};

let state: Module1HistoryState = {
  currentResultId: null,
  entries: [],
};

const listeners = new Set<() => void>();

function emitChange() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

function getSnapshot(): Module1HistoryState {
  return state;
}

function buildHistoryEntry(result: Module1CalculationResponseDto): Module1HistoryEntry {
  const entryId = `m1-${result.caseInfo.designCaseId}-${result.resultInfo.resultId}`;
  return {
    id: entryId,
    designCaseId: result.caseInfo.designCaseId,
    createdAt: result.resultInfo.createdAt,
    title: `Module 1 / ${result.selectedMotor.motorCode}`,
    summary: `${result.inputSummary.requiredPowerKw} kW at ${result.inputSummary.requiredOutputRpm} rpm`,
    result,
  };
}

export function addModule1HistoryEntry(result: Module1CalculationResponseDto): Module1HistoryEntry {
  const nextEntry = buildHistoryEntry(result);
  const existingIndex = state.entries.findIndex((entry) => entry.id === nextEntry.id);
  const nextEntries = [...state.entries];

  if (existingIndex >= 0) {
    nextEntries.splice(existingIndex, 1);
  }

  state = {
    currentResultId: nextEntry.id,
    entries: [nextEntry, ...nextEntries],
  };
  emitChange();

  return nextEntry;
}

export function setCurrentModule1Result(id: string) {
  state = {
    ...state,
    currentResultId: id,
  };
  emitChange();
}

export function getModule1HistoryEntryById(id: string): Module1HistoryEntry | undefined {
  return state.entries.find((entry) => entry.id === id);
}

export function getCurrentModule1HistoryEntry(): Module1HistoryEntry | undefined {
  if (!state.currentResultId) {
    return undefined;
  }

  return getModule1HistoryEntryById(state.currentResultId);
}

export function useModule1HistoryStore() {
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot);
}
