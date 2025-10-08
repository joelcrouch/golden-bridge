import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ExportResponse {
  success: boolean;
  message: string;
  activityId?: number;
  activityName?: string;
  goldenCheetahPath?: string;
  syncStatus?: string;
  totalExported?: number;
  activities?: any[];
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GoldenCheetahService {
  constructor(private http: HttpClient) {}

  exportActivity(activityId: number): Observable<ExportResponse> {
    return this.http.post<ExportResponse>(
      `${environment.apiUrl}/golden-cheetah/export/${activityId}`,
      {}
    );
  }

  exportAllActivities(): Observable<ExportResponse> {
    return this.http.post<ExportResponse>(
      `${environment.apiUrl}/golden-cheetah/export-all`,
      {}
    );
  }
}
