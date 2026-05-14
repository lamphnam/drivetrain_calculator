export function formatNumber(value: number, maximumFractionDigits = 4): string {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits,
  }).format(value);
}

export function formatPowerKw(value: number): string {
  return `${formatNumber(value, 4)} kW`;
}

export function formatRpm(value: number): string {
  return `${formatNumber(value, 4)} rpm`;
}

export function formatTorqueNmm(value: number): string {
  return `${formatNumber(value, 4)} Nmm`;
}

export function formatRatio(value: number): string {
  return formatNumber(value, 4);
}

export function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}
