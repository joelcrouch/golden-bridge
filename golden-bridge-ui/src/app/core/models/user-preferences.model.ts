export interface UserPreferences {
  id: number;
  autoSyncEnabled: boolean;
  syncFrequencyHours: number;
  syncActivityTypes: string;
  goldenCheetahPath?: string;
  notificationsEnabled: boolean;
  emailNotifications: boolean;
  maxSyncDays: number;
  timezone: string;
  createdAt: string;
  updatedAt?: string;
}

export interface UpdatePreferencesRequest {
  autoSyncEnabled?: boolean;
  syncFrequencyHours?: number;
  syncActivityTypes?: string;
  goldenCheetahPath?: string;
  notificationsEnabled?: boolean;
  emailNotifications?: boolean;
  maxSyncDays?: number;
  timezone?: string;
}
