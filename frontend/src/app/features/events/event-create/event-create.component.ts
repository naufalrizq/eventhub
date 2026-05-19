import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { EventService } from '../../../core/services/event.service';
import { CreateEventRequest, EventCategory } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSnackBarModule
  ],
  template: `
    <div class="premium-container">
      <div class="ambient-glow"></div>
      
      <div class="glass-card">
        <div class="card-header">
          <h1 class="gradient-text">Create New Event</h1>
          <p class="subtitle">Fill in the details below to launch your next big experience.</p>
        </div>

        <form [formGroup]="eventForm" (ngSubmit)="onSubmit()" class="premium-form">
          
          <div class="form-grid">
            <div class="input-group span-2">
              <label>Event Title <span class="required">*</span></label>
              <input type="text" formControlName="title" placeholder="e.g. Tech Conference 2026">
              <div class="error" *ngIf="eventForm.get('title')?.touched && eventForm.get('title')?.invalid">
                Title is required
              </div>
            </div>

            <div class="input-group span-2">
              <label>Description <span class="required">*</span></label>
              <textarea formControlName="description" rows="4" placeholder="What is this event about?"></textarea>
              <div class="error" *ngIf="eventForm.get('description')?.touched && eventForm.get('description')?.invalid">
                Description is required
              </div>
            </div>

            <div class="input-group">
              <label>Category <span class="required">*</span></label>
              <select formControlName="category">
                <option value="" disabled>Select a category</option>
                <option *ngFor="let cat of categories" [value]="cat">{{ cat }}</option>
              </select>
              <div class="error" *ngIf="eventForm.get('category')?.touched && eventForm.get('category')?.invalid">
                Category is required
              </div>
            </div>

            <div class="input-group">
              <label>Event Type <span class="required">*</span></label>
              <select formControlName="type">
                <option value="IN_PERSON">In Person</option>
                <option value="ONLINE">Online</option>
                <option value="HYBRID">Hybrid</option>
              </select>
            </div>

            <div class="input-group">
              <label>Start Date & Time <span class="required">*</span></label>
              <input type="datetime-local" formControlName="startDate">
              <div class="error" *ngIf="eventForm.get('startDate')?.touched && eventForm.get('startDate')?.invalid">
                Start date is required
              </div>
            </div>

            <div class="input-group">
              <label>End Date & Time <span class="required">*</span></label>
              <input type="datetime-local" formControlName="endDate">
              <div class="error" *ngIf="eventForm.get('endDate')?.touched && eventForm.get('endDate')?.invalid">
                End date is required
              </div>
            </div>

            <div class="input-group span-2">
              <label>Venue Name <span class="required">*</span></label>
              <input type="text" formControlName="venueName" placeholder="e.g. Grand Convention Center">
              <div class="error" *ngIf="eventForm.get('venueName')?.touched && eventForm.get('venueName')?.invalid">
                Venue name is required
              </div>
            </div>

            <div class="input-group span-2">
              <label>Address <span class="required">*</span></label>
              <input type="text" formControlName="address" placeholder="123 Event Street">
              <div class="error" *ngIf="eventForm.get('address')?.touched && eventForm.get('address')?.invalid">
                Address is required
              </div>
            </div>

            <div class="input-group">
              <label>City <span class="required">*</span></label>
              <input type="text" formControlName="city" placeholder="e.g. San Francisco">
              <div class="error" *ngIf="eventForm.get('city')?.touched && eventForm.get('city')?.invalid">
                City is required
              </div>
            </div>

            <div class="input-group">
              <label>Country <span class="required">*</span></label>
              <input type="text" formControlName="country">
              <div class="error" *ngIf="eventForm.get('country')?.touched && eventForm.get('country')?.invalid">
                Country is required
              </div>
            </div>

            <div class="input-group">
              <label>Capacity <span class="required">*</span></label>
              <input type="number" formControlName="capacity" min="1">
              <div class="error" *ngIf="eventForm.get('capacity')?.touched && eventForm.get('capacity')?.invalid">
                Capacity is required
              </div>
            </div>

            <div class="input-group">
              <label>Banner Image URL <span class="optional">(Optional)</span></label>
              <input type="url" formControlName="bannerImageUrl" placeholder="https://example.com/image.jpg">
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" (click)="onCancel()">Cancel</button>
            <button type="submit" class="btn-submit" [disabled]="eventForm.invalid || isLoading">
              <span class="btn-text">{{ isLoading ? 'Creating...' : 'Create Event' }}</span>
              <div class="btn-glow"></div>
            </button>
          </div>

        </form>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: calc(100vh - 64px);
      background-color: #1a1a1a;
      color: #ffe8d6;
      padding: 3rem 1rem;
    }

    .premium-container {
      max-width: 850px;
      margin: 0 auto;
      position: relative;
    }

    .ambient-glow {
      position: absolute;
      top: -150px;
      left: 50%;
      transform: translateX(-50%);
      width: 800px;
      height: 800px;
      background: radial-gradient(circle, rgba(203, 153, 126, 0.15) 0%, rgba(0, 0, 0, 0) 70%);
      pointer-events: none;
      z-index: 0;
    }

    .glass-card {
      position: relative;
      z-index: 1;
      background: rgba(43, 44, 40, 0.7);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 24px;
      padding: 3.5rem;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255,255,255,0.05);
    }

    .card-header {
      text-align: center;
      margin-bottom: 3rem;
    }

    .gradient-text {
      font-size: 2.5rem;
      font-weight: 800;
      margin: 0 0 0.75rem 0;
      background: linear-gradient(135deg, #ffe8d6 0%, #b7b7a4 50%, #a5a58d 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.025em;
    }

    .subtitle {
      color: #ddbea9;
      font-size: 1.125rem;
      margin: 0;
    }

    .premium-form {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2rem 1.5rem;
    }

    .input-group {
      display: flex;
      flex-direction: column;
      gap: 0.625rem;
    }

    .input-group.span-2 {
      grid-column: span 2;
    }

    label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #ddbea9;
      display: flex;
      justify-content: space-between;
      letter-spacing: 0.01em;
    }

    .required {
      color: #f43f5e;
    }

    .optional {
      color: #71717a;
      font-size: 0.75rem;
      font-weight: 400;
    }

    input, textarea, select {
      background: #2b2c28;
      border: 1px solid #6b705c;
      border-radius: 12px;
      padding: 0.875rem 1.25rem;
      color: #ffe8d6;
      font-size: 1rem;
      transition: all 0.2s ease;
      outline: none;
      font-family: inherit;
      color-scheme: dark;
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
    }

    input::placeholder, textarea::placeholder {
      color: #a5a58d;
    }

    input:focus, textarea:focus, select:focus {
      border-color: #ddbea9;
      box-shadow: 0 0 0 3px rgba(221, 190, 169, 0.25), inset 0 2px 4px rgba(0,0,0,0.2);
      background: #6b705c;
    }

    textarea {
      resize: vertical;
      min-height: 120px;
    }

    select {
      appearance: none;
      background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%23ddbea9' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
      background-position: right 1rem center;
      background-repeat: no-repeat;
      background-size: 1.5em 1.5em;
      padding-right: 2.5rem;
      cursor: pointer;
    }

    .error {
      color: #f43f5e;
      font-size: 0.8125rem;
      margin-top: 0.25rem;
      animation: slideDown 0.2s ease-out;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1rem;
      padding-top: 2.5rem;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
    }

    .btn-cancel {
      background: transparent;
      color: #ddbea9;
      border: 1px solid #6b705c;
      padding: 0.75rem 1.75rem;
      border-radius: 12px;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-cancel:hover {
      background: rgba(221, 190, 169, 0.1);
      color: #ffe8d6;
      border-color: #a5a58d;
    }

    .btn-submit {
      position: relative;
      background: linear-gradient(135deg, #cb997e, #6b705c);
      color: white;
      border: none;
      padding: 0.75rem 2.5rem;
      border-radius: 12px;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(203, 153, 126, 0.3);
    }

    .btn-submit:disabled {
      background: #2b2c28;
      color: #a5a58d;
      cursor: not-allowed;
      transform: none !important;
      box-shadow: none !important;
    }

    .btn-submit:not(:disabled):hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px -5px rgba(203, 153, 126, 0.5);
    }

    .btn-text {
      position: relative;
      z-index: 1;
    }

    .btn-glow {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #ddbea9, #a5a58d);
      opacity: 0;
      transition: opacity 0.3s;
      z-index: 0;
    }

    .btn-submit:not(:disabled):hover .btn-glow {
      opacity: 1;
    }

    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }

    @media (max-width: 768px) {
      .glass-card {
        padding: 2rem 1.5rem;
      }
      .form-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
      }
      .input-group.span-2 {
        grid-column: span 1;
      }
      .gradient-text {
        font-size: 2rem;
      }
      .form-actions {
        flex-direction: column;
      }
      .btn-cancel, .btn-submit {
        width: 100%;
      }
    }
  `]
})
export class EventCreateComponent {
  eventForm: FormGroup;
  categories = Object.values(EventCategory);
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      category: ['', Validators.required],
      venueName: ['', Validators.required],
      address: ['', Validators.required],
      city: ['', Validators.required],
      country: ['Indonesia', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      capacity: [50, [Validators.required, Validators.min(1)]],
      type: ['IN_PERSON'],
      bannerImageUrl: ['']
    });
  }

  onSubmit(): void {
    if (this.eventForm.valid) {
      this.isLoading = true;
      const eventData: CreateEventRequest = {
        ...this.eventForm.value,
        startDate: this.toLocalDateTime(this.eventForm.value.startDate),
        endDate: this.toLocalDateTime(this.eventForm.value.endDate)
      };

      this.eventService.createEvent(eventData).subscribe({
        next: (event) => {
          this.isLoading = false;
          this.snackBar.open('Event created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/events', event.id]);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Failed to create event. Please try again.', 'Close', { duration: 5000 });
        }
      });
    } else {
      // Mark all fields as touched to trigger error messages
      Object.keys(this.eventForm.controls).forEach(key => {
        const control = this.eventForm.get(key);
        control?.markAsTouched();
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/events']);
  }

  private toLocalDateTime(value: string | Date): Date {
    return new Date(value);
  }
}
