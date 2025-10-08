import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Activity } from '../models/activity.model';

export interface GarminLoginRequest {
  email: string;
  password: string;
}

export interface GarminLoginResponse {
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class GarminService {
  constructor(private http: HttpClient) {}

  loginToGarmin(credentials: GarminLoginRequest): Observable<GarminLoginResponse> {
    return this.http.post<GarminLoginResponse>(
      `${environment.apiUrl}/auth/garmin/login`,
      credentials
    );
  }

  fetchActivities(start: number = 0, limit: number = 10): Observable<string> {
    return this.http.get<string>(
      `${environment.apiUrl}/garmin/activities?start=${start}&limit=${limit}`
    );
  }

  downloadActivityFit(activityId: string): Observable<Blob> {
    return this.http.get(
      `${environment.apiUrl}/garmin/activities/${activityId}/download`,
      { responseType: 'blob' }
    );
  }
}
