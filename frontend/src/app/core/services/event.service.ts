import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Event, CreateEventRequest, EventCategory, EventStatus } from '../models/event.model';
import { environment } from '../../../environments/environment';

export interface EventFilters {
  category?: EventCategory;
  status?: EventStatus;
  startDate?: Date;
  endDate?: Date;
  location?: string;
  search?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly API_URL = `${environment.apiUrl}/api/events`;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  getEvents(filters?: EventFilters): Observable<PagedResponse<Event>> {
    if (!this.isBrowser()) {
      return of(this.emptyPage(filters?.page ?? 0, filters?.size ?? 10));
    }

    let params = new HttpParams();
    const hasAdvancedFilters = !!(filters?.search || filters?.category || filters?.location || filters?.startDate || filters?.endDate);
    
    if (filters) {
      if (filters.category) params = params.set('category', filters.category);
      if (filters.startDate) params = params.set('startDate', filters.startDate.toISOString());
      if (filters.endDate) params = params.set('endDate', filters.endDate.toISOString());
      if (filters.location) params = params.set('location', filters.location);
      if (filters.search) params = params.set('keyword', filters.search);
      if (filters.page !== undefined) params = params.set('page', filters.page.toString());
      if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    }

    return this.http.get<PagedResponse<Event>>(hasAdvancedFilters ? `${this.API_URL}/advanced-search` : this.API_URL, { params });
  }

  getEvent(id: string): Observable<Event> {
    return this.http.get<Event>(`${this.API_URL}/${id}`);
  }

  createEvent(event: CreateEventRequest): Observable<Event> {
    return this.http.post<Event>(this.API_URL, event);
  }

  updateEvent(id: string, event: Partial<CreateEventRequest>): Observable<Event> {
    return this.http.put<Event>(`${this.API_URL}/${id}`, event);
  }

  deleteEvent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getMyEvents(): Observable<PagedResponse<Event>> {
    if (!this.isBrowser()) {
      return of(this.emptyPage(0, 10));
    }

    return this.http.get<PagedResponse<Event>>(`${this.API_URL}/my-events`);
  }

  publishEvent(id: string): Observable<Event> {
    return this.http.post<Event>(`${this.API_URL}/${id}/publish`, {});
  }

  cancelEvent(id: string): Observable<Event> {
    return this.http.post<Event>(`${this.API_URL}/${id}/cancel`, {});
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  private emptyPage(page: number, size: number): PagedResponse<Event> {
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size,
      number: page
    };
  }
}
