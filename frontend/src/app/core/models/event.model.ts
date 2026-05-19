export interface Event {
  id: string;
  title: string;
  description: string;
  shortDescription?: string;
  startDate: Date;
  endDate: Date;
  venueName?: string;
  address?: string;
  city?: string;
  country?: string;
  location?: string;
  capacity?: number;
  maxAttendees?: number;
  currentRegistrations?: number;
  currentAttendees?: number;
  price?: number;
  category: EventCategory;
  status: EventStatus;
  bannerImageUrl?: string;
  imageUrl?: string;
  organizer: {
    id: string;
    firstName: string;
    lastName: string;
    email?: string;
  };
  ticketTypes: TicketType[];
  sessions: EventSession[];
  createdAt: Date;
  updatedAt: Date;
}

export enum EventCategory {
  TECHNOLOGY = 'TECHNOLOGY',
  BUSINESS = 'BUSINESS',
  HEALTH_WELLNESS = 'HEALTH_WELLNESS',
  EDUCATION = 'EDUCATION',
  ARTS_CULTURE = 'ARTS_CULTURE',
  SPORTS_FITNESS = 'SPORTS_FITNESS',
  FOOD_DRINK = 'FOOD_DRINK',
  MUSIC = 'MUSIC',
  NETWORKING = 'NETWORKING',
  CHARITY = 'CHARITY',
  COMMUNITY = 'COMMUNITY',
  OTHER = 'OTHER'
}

export enum EventStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  CANCELLED = 'CANCELLED',
  POSTPONED = 'POSTPONED',
  COMPLETED = 'COMPLETED'
}

export interface TicketType {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  availableQuantity: number;
  eventId: number;
}

export interface EventSession {
  id: number;
  title: string;
  description: string;
  startTime: Date;
  endTime: Date;
  speaker: string;
  eventId: number;
}

export interface Registration {
  id: number;
  userId: number;
  eventId: number;
  ticketTypeId: number;
  registrationDate: Date;
  status: RegistrationStatus;
  paymentStatus: PaymentStatus;
  totalAmount: number;
}

export enum RegistrationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export interface CreateEventRequest {
  title: string;
  description: string;
  startDate: Date;
  endDate: Date;
  venueName: string;
  address: string;
  city: string;
  country: string;
  capacity: number;
  category: EventCategory;
  type?: 'IN_PERSON' | 'ONLINE' | 'HYBRID';
  bannerImageUrl?: string;
}
