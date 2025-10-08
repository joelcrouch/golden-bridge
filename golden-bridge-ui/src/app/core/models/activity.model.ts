export interface Activity {
  id: number;
  garminActivityId: string;
  activityName: string;
  activityType: string;
  activityDate: string;
  durationSeconds?: number;
  distanceMeters?: number;
  calories?: number;
  averageHeartRate?: number;
  maxHeartRate?: number;
  averagePower?: number;
  maxPower?: number;
  elevationGainMeters?: number;
  averageSpeedKmh?: number;
  maxSpeedKmh?: number;
  syncStatus: SyncStatus;
  syncError?: string;
  lastSyncAttempt?: string;
  fitFilePath?: string;
  goldenCheetahPath?: string;
  createdAt: string;
  updatedAt?: string;
}

export enum SyncStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED',
  EXPORTED = 'EXPORTED'
}

export interface SyncHistory {
  id: number;
  syncType: SyncType;
  syncStatus: SyncHistoryStatus;
  syncStartedAt: string;
  syncCompletedAt?: string;
  activitiesProcessed: number;
  activitiesSynced: number;
  activitiesSkipped: number;
  activitiesFailed: number;
  errorMessage?: string;
  syncDetails?: string;
  createdAt: string;
}

export enum SyncType {
  MANUAL = 'MANUAL',
  SCHEDULED = 'SCHEDULED',
  PARTIAL = 'PARTIAL',
  FULL_RESYNC = 'FULL_RESYNC'
}

export enum SyncHistoryStatus {
  STARTED = 'STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}
