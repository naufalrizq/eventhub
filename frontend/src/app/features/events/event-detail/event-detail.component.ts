import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { EventService } from '../../../core/services/event.service';
import { Event, EventStatus } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule
  ],
  template: `
    <div class="event-detail-container" *ngIf="event$ | async as event">
      <div class="event-header">
        <img [src]="getEventImage(event)" 
             [alt]="event.title" class="event-hero-image">
        
        <div class="event-header-content">
          <div class="event-meta">
            <mat-chip>{{ event.category }}</mat-chip>
            <mat-chip [color]="getStatusColor(event.status)">{{ event.status }}</mat-chip>
          </div>
          
          <h1>{{ event.title }}</h1>
          
          <div class="event-basic-info">
            <div class="info-item">
              <mat-icon>location_on</mat-icon>
              <span>{{ getEventLocation(event) }}</span>
            </div>
            <div class="info-item">
              <mat-icon>event</mat-icon>
              <span>{{ event.startDate | date:'fullDate' }}</span>
            </div>
            <div class="info-item">
              <mat-icon>schedule</mat-icon>
              <span>{{ event.startDate | date:'shortTime' }} - {{ event.endDate | date:'shortTime' }}</span>
            </div>
            <div class="info-item">
              <mat-icon>people</mat-icon>
              <span>{{ getCurrentAttendees(event) }}/{{ getCapacity(event) }} attendees</span>
            </div>
          </div>

          <div class="event-actions">
            <button mat-raised-button color="primary" 
                    [disabled]="isEventFull(event) || event.status !== 'PUBLISHED'"
                    [routerLink]="['/events', event.id]">
              <mat-icon>event_available</mat-icon>
              {{ isEventFull(event) ? 'Event Full' : 'Register Now' }}
            </button>
            <button mat-button>
              <mat-icon>share</mat-icon>
              Share
            </button>
          </div>
        </div>
      </div>

      <div class="event-content">
        <div class="main-content">
          <mat-card>
            <mat-card-header>
              <mat-card-title>About This Event</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>{{ event.description }}</p>
            </mat-card-content>
          </mat-card>

          <mat-card *ngIf="event.sessions && event.sessions.length > 0">
            <mat-card-header>
              <mat-card-title>Event Sessions</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div *ngFor="let session of event.sessions; let last = last">
                <div class="session-item">
                  <div class="session-time">
                    <mat-icon>schedule</mat-icon>
                    <span>{{ session.startTime | date:'shortTime' }} - {{ session.endTime | date:'shortTime' }}</span>
                  </div>
                  <div class="session-details">
                    <h4>{{ session.title }}</h4>
                    <p>{{ session.description }}</p>
                    <div class="session-speaker" *ngIf="session.speaker">
                      <mat-icon>person</mat-icon>
                      <span>{{ session.speaker }}</span>
                    </div>
                  </div>
                </div>
                <mat-divider *ngIf="!last"></mat-divider>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <div class="sidebar">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Event Details</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="detail-row">
                <strong>Organizer:</strong>
                <span>{{ event.organizer.firstName }} {{ event.organizer.lastName }}</span>
              </div>
              <div class="detail-row">
                <strong>Price:</strong>
                <span>{{ getEventPrice(event) === 0 ? 'Free' : '$' + getEventPrice(event) }}</span>
              </div>
              <div class="detail-row">
                <strong>Created:</strong>
                <span>{{ event.createdAt | date:'mediumDate' }}</span>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card *ngIf="event.ticketTypes && event.ticketTypes.length > 0">
            <mat-card-header>
              <mat-card-title>Ticket Types</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div *ngFor="let ticket of event.ticketTypes" class="ticket-type">
                <div class="ticket-info">
                  <h4>{{ ticket.name }}</h4>
                  <p>{{ ticket.description }}</p>
                  <div class="ticket-price">
                    <strong>\${{ ticket.price }}</strong>
                  </div>
                  <div class="ticket-availability">
                    {{ ticket.availableQuantity }}/{{ ticket.quantity }} available
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .event-detail-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .event-header {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .event-hero-image {
      width: 100%;
      height: 400px;
      object-fit: cover;
      border-radius: 8px;
    }

    .event-header-content {
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .event-meta {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .event-basic-info {
      margin: 1rem 0;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }

    .event-actions {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
    }

    .event-content {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 2rem;
    }

    .main-content mat-card {
      margin-bottom: 2rem;
    }

    .session-item {
      display: flex;
      gap: 1rem;
      padding: 1rem 0;
    }

    .session-time {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      min-width: 150px;
      color: #666;
    }

    .session-details h4 {
      margin: 0 0 0.5rem 0;
    }

    .session-speaker {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-top: 0.5rem;
      color: #666;
    }

    .detail-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 1rem;
    }

    .ticket-type {
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 1rem;
      margin-bottom: 1rem;
    }

    .ticket-type h4 {
      margin: 0 0 0.5rem 0;
    }

    .ticket-price {
      color: #1976d2;
      font-size: 1.2rem;
      margin: 0.5rem 0;
    }

    .ticket-availability {
      color: #666;
      font-size: 0.9rem;
    }

    @media (max-width: 768px) {
      .event-header {
        grid-template-columns: 1fr;
      }
      
      .event-content {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class EventDetailComponent implements OnInit {
  event$: Observable<Event>;

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService
  ) {
    this.event$ = this.route.params.pipe(
      switchMap(params => this.eventService.getEvent(params['id']))
    );
  }

  ngOnInit(): void {}

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
    return event.bannerImageUrl || event.imageUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80';
  }

  getEventLocation(event: Event): string {
    if (event.location) {
      return event.location;
    }

    return [event.venueName, event.address, event.city, event.country].filter(Boolean).join(', ') || 'Online';
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
}
