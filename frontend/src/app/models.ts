/** TS models mirroring the CropGuard backend DTOs / entities. */

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  name: string;
  role: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role?: string;
  bundesland: string;
}

export interface Insured {
  id: number;
  name: string;
  email: string;
  role: string;
  bundesland: string;
}

export interface QuoteResult {
  premiumEur: number;
  baseEur: number;
  riskAdjustment: number;
  deductibleAdjustment: number;
  coverageEur: number;
  droughtIndex: number;
  droughtAdjustment: number;
}

export interface Plot {
  id: number;
  cropType: string;
  hectares: number;
  bundesland: string;
  coordinateE: number | null;
  coordinateN: number | null;
  locationDescription: string;
  insured: Insured;
}

export interface Policy {
  id: number;
  coverageEur: number;
  premiumEur: number;
  deductible: string;
  status: string;
  coverageStart: string;
  coverageEnd: string;
  plot: Plot;
}

export interface Claim {
  id: number;
  damageDate: string;
  damageDescription: string;
  damagePercent?: number | null;
  payoutEur?: number | null;
  assessorNotes?: string | null;
  status: string;
  hailEventId: number | null;
  assessedBy?: string | null;
  policy: Policy;
}

export interface AssessRequest {
  damagePercent: number;
  decision: string;
  assessorNotes?: string;
}

export interface HailEvent {
  id: number;
  eventDate: string;
  affectedBundeslaender: string;
  severity: string;
  description: string;
  hailstoneDiameterMm: number | null;
}

/** Option lists matching the backend enums (drives the forms). */
export const CROP_TYPES = [
  'WHEAT', 'BARLEY', 'CORN', 'RAPESEED', 'VINES', 'APPLES', 'PEARS', 'CHERRIES',
  'STRAWBERRIES', 'RASPBERRIES', 'POTATOES', 'VEGETABLES', 'TOBACCO', 'HOPS'
] as const;

export const BUNDESLAENDER = [
  'BAYERN', 'BADEN_WUERTTEMBERG', 'HESSEN', 'RHEINLAND_PFALZ', 'SAARLAND',
  'THUERINGEN', 'SACHSEN', 'SACHSEN_ANHALT', 'NORDRHEIN_WESTFALEN', 'NIEDERSACHSEN',
  'SCHLESWIG_HOLSTEIN', 'MECKLENBURG_VORPOMMERN', 'BRANDENBURG', 'BERLIN', 'BREMEN', 'HAMBURG'
] as const;

export const DEDUCTIBLES = ['NONE', 'FIVE_PERCENT', 'TEN_PERCENT', 'FIFTEEN_PERCENT', 'TWENTY_PERCENT'] as const;
