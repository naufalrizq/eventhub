import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { EventService, PagedResponse } from '../../core/services/event.service';
import { User } from '../../core/models/user.model';
import { Event } from '../../core/models/event.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule
  ],
  template: `
    <div class="dashboard-container" *ngIf="currentUser$ | async as user">
      <div class="dashboard-header">
        <h1>Welcome back, {{ user.firstName }}!</h1>
        <p>Manage your events and registrations</p>
      </div>

      <div class="dashboard-stats">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <mat-icon class="stat-icon">event</mat-icon>
              <div class="stat-info">
                <h3>{{ (myEvents$ | async)?.totalElements || 0 }}</h3>
                <p>My Events</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <mat-icon class="stat-icon">people</mat-icon>
              <div class="stat-info">
                <h3>0</h3>
                <p>Registrations</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <mat-icon class="stat-icon">trending_up</mat-icon>
              <div class="stat-info">
                <h3>0</h3>
                <p>Total Revenue</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <mat-tab-group class="dashboard-tabs">
        <mat-tab label="My Events">
          <div class="tab-content">
            <div class="tab-header">
              <h2>My Events</h2>
              <button mat-raised-button color="primary" routerLink="/events/create">
                <mat-icon>add</mat-icon>
                Create Event
              </button>
            </div>

            <div class="events-grid" *ngIf="myEvents$ | async as events">
              <mat-card *ngFor="let event of events.content" class="event-card">
                <mat-card-header>
                  <mat-card-title>{{ event.title }}</mat-card-title>
                  <mat-card-subtitle>{{ getEventLocation(event) }}</mat-card-subtitle>
                </mat-card-header>

                <mat-card-content>
                  <div class="event-info">
                    <div class="info-item">
                      <mat-icon>event</mat-icon>
                      <span>{{ event.startDate | date:'mediumDate' }}</span>
                    </div>
                    <div class="info-item">
                      <mat-icon>people</mat-icon>
                      <span>{{ getCurrentAttendees(event) }}/{{ getCapacity(event) }}</span>
                    </div>
                    <div class="info-item">
                <mat-icon>attach_money</mat-icon>
                <span>{{ getEventPrice(event) === 0 ? 'Free' : '$' + getEventPrice(event) }}</span>
                    </div>
                  </div>
                </mat-card-content>

                <mat-card-actions>
                  <button mat-button [routerLink]="['/events', event.id]">View</button>
                  <button mat-button [routerLink]="['/events', event.id, 'edit']">Edit</button>
                  <button mat-button color="primary" 
                          *ngIf="event.status === 'DRAFT'"
                          (click)="publishEvent(event.id)">
                    Publish
                  </button>
                </mat-card-actions>
              </mat-card>

              <div *ngIf="events.content.length === 0" class="empty-state">
                <mat-icon>event_note</mat-icon>
                <h3>No events yet</h3>
                <p>Create your first event to get started</p>
                <button mat-raised-button color="primary" routerLink="/events/create">
                  Create Event
                </button>
              </div>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Registrations">
          <div class="tab-content">
            <h2>My Registrations</h2>
            <div class="empty-state">
              <mat-icon>confirmation_number</mat-icon>
              <h3>No registrations yet</h3>
              <p>Browse events and register for ones you're interested in</p>
              <button mat-raised-button color="primary" routerLink="/events">
                Browse Events
              </button>
            </div>
          </div>
        </mat-tab>

        <mat-tab label="Analytics">
          <div class="tab-content">
            <h2>Event Analytics</h2>
            <div class="empty-state">
              <mat-icon>analytics</mat-icon>
              <h3>Analytics coming soon</h3>
              <p>Track your event performance and attendee engagement</p>
            </div>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .dashboard-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .dashboard-header {
      margin-bottom: 2rem;
    }

    .dashboard-header h1 {
      margin: 0;
      color: #ffffff;
    }

    .dashboard-header p {
      color: #b3b3b3;
      margin: 0.5rem 0 0 0;
    }

    .dashboard-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .stat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
    }

    .stat-info h3 {
      margin: 0;
      font-size: 2rem;
      font-weight: bold;
    }

    .stat-info p {
      margin: 0;
      opacity: 0.9;
    }

    .dashboard-tabs {
      margin-top: 2rem;
    }

    .tab-content {
      padding: 2rem 0;
    }

    .tab-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .events-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1rem;
    }

    .event-card {
      height: fit-content;
    }

    .event-info {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9rem;
    }

    .info-item mat-icon {
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
    }

    .empty-state {
      text-align: center;
      padding: 3rem;
      color: #b3b3b3;
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 1rem;
      opacity: 0.5;
    }

    .empty-state h3 {
      margin: 1rem 0;
      color: #ffffff;
    }

    .empty-state p {
      margin-bottom: 2rem;
    }
  `]
})
export class DashboardComponent implements OnInit {
  currentUser$: Observable<User | null>;
  myEvents$: Observable<PagedResponse<Event>>;

  constructor(
    private authService: AuthService,
    private eventService: EventService
  ) {
    this.currentUser$ = this.authService.currentUser$;
    this.myEvents$ = this.eventService.getMyEvents();
  }

  ngOnInit(): void {}

  publishEvent(eventId: string): void {
    this.eventService.publishEvent(eventId).subscribe({
      next: () => {
        // Refresh the events list
        this.myEvents$ = this.eventService.getMyEvents();
      },
      error: (error) => {
        console.error('Failed to publish event:', error);
      }
    });
  }

  getEventPrice(event: Event): number {
    return event.price ?? event.ticketTypes?.[0]?.price ?? 0;
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
}
