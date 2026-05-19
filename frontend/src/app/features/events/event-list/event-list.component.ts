import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { startWith, debounceTime, switchMap } from 'rxjs/operators';

import { EventService, EventFilters, PagedResponse } from '../../../core/services/event.service';
import { Event, EventCategory, EventStatus } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatPaginatorModule
  ],
  template: `
    <div class="event-list-container">
      <div class="header">
        <h1>Discover Events</h1>
        <button mat-raised-button color="primary" routerLink="/events/create">
          <mat-icon>add</mat-icon>
          Create Event
        </button>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <form [formGroup]="filtersForm">
          <div class="filters-row">
            <mat-form-field appearance="outline">
              <mat-label>Search</mat-label>
              <input matInput formControlName="search" placeholder="Search events...">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Category</mat-label>
              <mat-select formControlName="category">
                <mat-option value="">All Categories</mat-option>
                <mat-option *ngFor="let category of categories" [value]="category">
                  {{ category }}
                </mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Status</mat-label>
              <mat-select formControlName="status">
                <mat-option value="">All Status</mat-option>
                <mat-option *ngFor="let status of statuses" [value]="status">
                  {{ status }}
                </mat-option>
              </mat-select>
            </mat-form-field>
          </div>
        </form>
      </mat-card>

      <!-- Events Grid -->
      <div class="events-grid" *ngIf="events$ | async as pagedEvents">
        <mat-card *ngFor="let event of pagedEvents.content" class="event-card">
          <div mat-card-image class="event-image" [style.backgroundImage]="'url(' + getEventImage(event) + ')'"></div>
          
          <mat-card-header>
            <mat-card-title>{{ event.title }}</mat-card-title>
            <mat-card-subtitle>
              <mat-icon>location_on</mat-icon>
              {{ getEventLocation(event) }}
            </mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <p class="event-description">{{ event.description | slice:0:150 }}...</p>
            
            <div class="event-details">
              <div class="detail-item">
                <mat-icon>event</mat-icon>
                <span>{{ event.startDate | date:'MMM dd, yyyy' }}</span>
              </div>
              <div class="detail-item">
                <mat-icon>schedule</mat-icon>
                <span>{{ event.startDate | date:'HH:mm' }}</span>
              </div>
              <div class="detail-item">
                <mat-icon>people</mat-icon>
                <span>{{ getCurrentAttendees(event) }}/{{ getCapacity(event) }}</span>
              </div>
              <div class="detail-item" *ngIf="getEventPrice(event) > 0">
                <mat-icon>attach_money</mat-icon>
                <span>\${{ getEventPrice(event) }}</span>
              </div>
              <div class="detail-item" *ngIf="getEventPrice(event) === 0">
                <mat-chip color="accent">FREE</mat-chip>
              </div>
            </div>

            <div class="event-tags">
              <mat-chip>{{ event.category }}</mat-chip>
              <mat-chip [color]="getStatusColor(event.status)">{{ event.status }}</mat-chip>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button [routerLink]="['/events', event.id]">
              <mat-icon>visibility</mat-icon>
              View Details
            </button>
            <button mat-raised-button color="primary" 
                    [routerLink]="['/events', event.id]"
                    [disabled]="isEventFull(event)">
              <mat-icon>event_available</mat-icon>
              {{ isEventFull(event) ? 'Full' : 'Register' }}
            </button>
          </mat-card-actions>
        </mat-card>
      </div>

      <!-- Pagination -->
      <mat-paginator 
        *ngIf="events$ | async as pagedEvents"
        [length]="pagedEvents.totalElements"
        [pageSize]="pageSize"
        [pageSizeOptions]="[6, 12, 24]"
        (page)="onPageChange($event)"
        showFirstLastButtons>
      </mat-paginator>
    </div>
  `,
  styles: [`
    .event-list-container {
      padding: 2rem;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .header h1 {
      color: #ffffff;
      font-size: 1.75rem;
      font-weight: 700;
    }

    .filters-card {
      margin-bottom: 2rem;
      padding: 1rem;
    }

    .filters-row {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .filters-row mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .events-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .event-card {
      height: fit-content;
      display: flex;
      flex-direction: column;
    }

    .event-image {
      height: 200px;
      background-color: #2c2c2c;
      background-position: center;
      background-size: cover;
      border-top-left-radius: 12px;
      border-top-right-radius: 12px;
    }

    .event-description {
      margin-bottom: 1rem;
      color: #b3b3b3;
    }

    .event-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      margin-bottom: 1rem;
      color: #e0e0e0;
    }

    .detail-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9rem;
    }

    .detail-item mat-icon {
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
      color: #9e9e9e;
    }

    .event-tags {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-top: 1rem;
    }

    mat-card-actions {
      display: flex;
      justify-content: space-between;
      margin-top: auto;
      padding: 8px;
    }
  `]
})
export class EventListComponent implements OnInit {
  filtersForm: FormGroup;
  events$: Observable<PagedResponse<Event>>;
  
  categories = Object.values(EventCategory);
  statuses = Object.values(EventStatus);
  
  pageSize = 6;
  currentPage = 0;
  
  private filtersSubject = new BehaviorSubject<EventFilters>({});

  constructor(
    private fb: FormBuilder,
    private eventService: EventService
  ) {
    this.filtersForm = this.fb.group({
      search: [''],
      category: [''],
      status: ['']
    });

    this.events$ = combineLatest([
      this.filtersSubject.asObservable(),
      this.filtersForm.valueChanges.pipe(
        startWith(this.filtersForm.value),
        debounceTime(300)
      )
    ]).pipe(
      switchMap(([currentFilters, formFilters]) => this.eventService.getEvents({
        ...currentFilters,
        ...(formFilters as EventFilters),
        page: this.currentPage,
        size: this.pageSize
      }))
    );
  }

  ngOnInit(): void {
    this.loadEvents();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEvents();
  }

  getStatusColor(status: EventStatus): string {
    switch (status) {
      case EventStatus.PUBLISHED: return 'primary';
      case EventStatus.DRAFT: return 'warn';
      case EventStatus.CANCELLED: return 'warn';
      case EventStatus.COMPLETED: return 'accent';
      default: return '';
    }
  }

  getEventImage(event: Event): string {
    return event.bannerImageUrl || event.imageUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=900&q=80';
  }

  getEventLocation(event: Event): string {
    if (event.location) {
      return event.location;
    }

    return [event.venueName, event.city, event.country].filter(Boolean).join(', ') || 'Online';
  }

  getCapacity(event: Event): number {
    return event.capacity ?? event.maxAttendees ?? 0;
  }

  getCurrentAttendees(event: Event): number {
    return event.currentRegistrations ?? event.currentAttendees ?? 0;
  }

  getEventPrice(event: Event): number {
    return event.price ?? event.ticketTypes?.[0]?.price ?? 0;
  }

  isEventFull(event: Event): boolean {
    const capacity = this.getCapacity(event);
    return capacity > 0 && this.getCurrentAttendees(event) >= capacity;
  }

  private loadEvents(): void {
    const filters = {
      ...this.filtersForm.value,
      page: this.currentPage,
      size: this.pageSize
    };
    this.filtersSubject.next(filters);
  }
}
