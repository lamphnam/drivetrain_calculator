/**
 * Mock-only motor catalog data (no longer used by module1Api but kept for reference).
 */

import type { SelectedMotorSummaryDto } from '@/types/api/module1';

export const mockMotorCatalog: SelectedMotorSummaryDto[] = [
  {
    motorId: 1,
    motorCode: 'K132S4',
    displayName: 'K132S4 - 5.5kW',
    manufacturer: 'Siemens',
    description: '3-phase async motor',
    ratedPowerKw: 5.5,
    ratedRpm: 1450,
  },
  {
    motorId: 2,
    motorCode: 'K132M4',
    displayName: 'K132M4 - 7.5kW',
    manufacturer: 'Siemens',
    description: '3-phase async motor',
    ratedPowerKw: 7.5,
    ratedRpm: 1450,
  },
  {
    motorId: 3,
    motorCode: 'K160S4',
    displayName: 'K160S4 - 11kW',
    manufacturer: 'Siemens',
    description: '3-phase async motor',
    ratedPowerKw: 11,
    ratedRpm: 1460,
  },
  {
    motorId: 4,
    motorCode: 'K160M4',
    displayName: 'K160M4 - 15kW',
    manufacturer: 'Siemens',
    description: '3-phase async motor',
    ratedPowerKw: 15,
    ratedRpm: 1460,
  },
  {
    motorId: 5,
    motorCode: 'K180S4',
    displayName: 'K180S4 - 18.5kW',
    manufacturer: 'Siemens',
    description: '3-phase async motor',
    ratedPowerKw: 18.5,
    ratedRpm: 1470,
  },
];
