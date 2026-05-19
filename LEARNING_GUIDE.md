# EventHub - Complete Learning Guide

## 🎯 **Project Overview**
**EventHub** is a comprehensive event management platform built with **Angular 17** frontend and **Spring Boot 3** backend. This project demonstrates enterprise-level Java development with modern Angular practices for event creation, registration, and management.

## 🏗️ **Architecture & Tech Stack**

### **Backend (Spring Boot 3)**
- **Spring Boot 3.2** - Latest enterprise Java framework
- **Spring Data JPA** - Object-relational mapping with Hibernate
- **Spring Security 6** - Authentication and authorization
- **Spring Cache** - Redis-based caching
- **PostgreSQL** - Enterprise-grade relational database
- **Maven** - Dependency management and build tool
- **Flyway** - Database migration management
- **Lombok** - Boilerplate code reduction
- **Bean Validation** - Input validation with annotations

### **Frontend (Angular 17)**
- **Angular 17** - Latest version with standalone components
- **TypeScript 5** - Type-safe development
- **Angular Material** - UI component library
- **NgRx** - State management with Redux pattern
- **RxJS** - Reactive programming
- **Angular Router** - Client-side routing with guards
- **Angular Forms** - Reactive forms with validation
- **Angular HTTP Client** - API communication with interceptors

### **DevOps & Tools**
- **Docker & Docker Compose** - Containerization
- **Redis** - Caching and session storage
- **Swagger/OpenAPI** - API documentation
- **JUnit 5** - Unit testing framework
- **Jasmine/Karma** - Frontend testing

## 📚 **Key Learning Concepts**

### **1. Spring Boot Enterprise Architecture**

#### **Layered Architecture Pattern**
```
src/main/java/com/eventhub/
├── EventHubApplication.java    # Main application class
├── controller/                 # REST controllers
├── service/                   # Business logic layer
├── repository/                # Data access layer
├── entity/                    # JPA entities
├── dto/                       # Data transfer objects
├── config/                    # Configuration classes
├── security/                  # Security configuration
└── exception/                 # Exception handling
```

#### **JPA Entity Relationships**
```java
@Entity
@Table(name = "events")
@Data
@EqualsAndHashCode(exclude = {"organizer", "registrations", "ticketTypes"})
@EntityListeners(AuditingEntityListener.class)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Event title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketType> ticketTypes = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### **Spring Data JPA Repository**
```java
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Custom query methods
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);
    
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    
    // Complex JPQL queries
    @Query("SELECT e FROM Event e WHERE e.startDate > :now AND e.status = 'PUBLISHED' ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Native SQL for complex operations
    @Query(value = "SELECT e.* FROM events e JOIN registrations r ON e.id = r.event_id " +
                   "WHERE r.user_id = :userId AND r.status = 'CONFIRMED'", nativeQuery = true)
    List<Event> findUserRegisteredEvents(@Param("userId") Long userId);
    
    // Aggregation queries
    @Query("SELECT SUM(e.registrationCount) FROM Event e WHERE e.organizer = :organizer")
    Long getTotalRegistrationsByOrganizer(@Param("organizer") User organizer);
}
```

#### **Service Layer with Business Logic**
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {
    
    private final EventRepository eventRepository;
    
    @Cacheable(value = "events", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> getAllEvents(Pageable pageable) {
        log.debug("Fetching all events with pagination: {}", pageable);
        return eventRepository.findAll(pageable);
    }
    
    @Transactional
    @CacheEvict(value = {"events", "featuredEvents"}, allEntries = true)
    public Event createEvent(Event event) {
        log.info("Creating new event: {}", event.getTitle());
        
        // Business logic validation
        validateEventForCreation(event);
        
        // Generate slug if not provided
        if (event.getSlug() == null || event.getSlug().isEmpty()) {
            event.setSlug(generateSlug(event.getTitle()));
        }
        
        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return savedEvent;
    }
    
    private void validateEventForCreation(Event event) {
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event start date cannot be in the past");
        }
        
        if (event.getEndDate() != null && event.getEndDate().isBefore(event.getStartDate())) {
            throw new IllegalArgumentException("Event end date must be after start date");
        }
    }
}
```

#### **REST Controller with Validation**
```java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200"})
public class EventController {
    
    private final EventService eventService;
    
    @GetMapping
    public ResponseEntity<Page<Event>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Event> events = eventService.getAllEvents(pageable);
        
        return ResponseEntity.ok(events);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody Event event,
            @AuthenticationPrincipal User currentUser) {
        
        event.setOrganizer(currentUser);
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }
}
```

### **2. Spring Security Configuration**

#### **Security Configuration**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### **JWT Authentication Filter**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain chain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.error("JWT Token has expired");
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
```

### **3. Caching with Redis**

#### **Cache Configuration**
```java
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### **Using Cache Annotations**
```java
@Service
public class EventService {
    
    @Cacheable(value = "events", key = "#id")
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }
    
    @CacheEvict(value = {"events", "featuredEvents"}, allEntries = true)
    public Event updateEvent(Long id, Event eventDetails) {
        // Update logic
    }
    
    @Cacheable(value = "featuredEvents")
    public List<Event> getFeaturedEvents() {
        return eventRepository.findFeaturedEvents();
    }
}
```

### **4. Database Migration with Flyway**

#### **Migration Scripts**
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V2__Create_events_table.sql
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    slug VARCHAR(255) UNIQUE NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    location VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    max_capacity INTEGER,
    registration_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_featured BOOLEAN NOT NULL DEFAULT false,
    image_url VARCHAR(500),
    organizer_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_organizer ON events(organizer_id);
```

### **5. Angular 17 Modern Architecture**

#### **Standalone Components**
```typescript
// event-list.component.ts
@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterModule],
  template: `
    <div class="event-grid">
      <mat-card *ngFor="let event of events$ | async" class="event-card">
        <mat-card-header>
          <mat-card-title>{{ event.title }}</mat-card-title>
          <mat-card-subtitle>{{ event.startDate | date:'medium' }}</mat-card-subtitle>
        </mat-card-header>
        
        <img mat-card-image [src]="event.imageUrl" [alt]="event.title">
        
        <mat-card-content>
          <p>{{ event.description | slice:0:150 }}...</p>
          <div class="event-meta">
            <span class="location">📍 {{ event.location }}</span>
            <span class="category">🏷️ {{ event.category }}</span>
          </div>
        </mat-card-content>
        
        <mat-card-actions>
          <button mat-button [routerLink]="['/events', event.id]">View Details</button>
          <button mat-raised-button color="primary" (click)="register(event)">Register</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events$ = this.store.select(selectAllEvents);
  loading$ = this.store.select(selectEventsLoading);
  
  constructor(private store: Store<AppState>) {}
  
  ngOnInit() {
    this.store.dispatch(EventActions.loadEvents());
  }
  
  register(event: Event) {
    this.store.dispatch(EventActions.registerForEvent({ eventId: event.id }));
  }
}
```

#### **NgRx State Management**
```typescript
// event.state.ts
export interface EventState {
  events: Event[];
  selectedEvent: Event | null;
  loading: boolean;
  error: string | null;
}

export const initialState: EventState = {
  events: [],
  selectedEvent: null,
  loading: false,
  error: null
};

// event.actions.ts
export const EventActions = createActionGroup({
  source: 'Event',
  events: {
    'Load Events': emptyProps(),
    'Load Events Success': props<{ events: Event[] }>(),
    'Load Events Failure': props<{ error: string }>(),
    'Select Event': props<{ eventId: number }>(),
    'Register For Event': props<{ eventId: number }>(),
    'Registration Success': props<{ registration: Registration }>(),
    'Registration Failure': props<{ error: string }>()
  }
});

// event.reducer.ts
export const eventReducer = createReducer(
  initialState,
  on(EventActions.loadEvents, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  on(EventActions.loadEventsSuccess, (state, { events }) => ({
    ...state,
    events,
    loading: false,
    error: null
  })),
  on(EventActions.loadEventsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  }))
);

// event.effects.ts
@Injectable()
export class EventEffects {
  loadEvents$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EventActions.loadEvents),
      switchMap(() =>
        this.eventService.getEvents().pipe(
          map(events => EventActions.loadEventsSuccess({ events })),
          catchError(error => of(EventActions.loadEventsFailure({ error: error.message })))
        )
      )
    )
  );
  
  registerForEvent$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EventActions.registerForEvent),
      switchMap(({ eventId }) =>
        this.registrationService.register(eventId).pipe(
          map(registration => EventActions.registrationSuccess({ registration })),
          catchError(error => of(EventActions.registrationFailure({ error: error.message })))
        )
      )
    )
  );
  
  constructor(
    private actions$: Actions,
    private eventService: EventService,
    private registrationService: RegistrationService
  ) {}
}
```

#### **Reactive Forms with Validation**
```typescript
// event-form.component.ts
@Component({
  selector: 'app-event-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatDatepickerModule],
  template: `
    <form [formGroup]="eventForm" (ngSubmit)="onSubmit()">
      <mat-form-field appearance="outline">
        <mat-label>Event Title</mat-label>
        <input matInput formControlName="title" placeholder="Enter event title">
        <mat-error *ngIf="eventForm.get('title')?.hasError('required')">
          Title is required
        </mat-error>
        <mat-error *ngIf="eventForm.get('title')?.hasError('maxlength')">
          Title must not exceed 200 characters
        </mat-error>
      </mat-form-field>
      
      <mat-form-field appearance="outline">
        <mat-label>Description</mat-label>
        <textarea matInput formControlName="description" rows="4"></textarea>
      </mat-form-field>
      
      <mat-form-field appearance="outline">
        <mat-label>Start Date</mat-label>
        <input matInput [matDatepicker]="startPicker" formControlName="startDate">
        <mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle>
        <mat-datepicker #startPicker></mat-datepicker>
        <mat-error *ngIf="eventForm.get('startDate')?.hasError('required')">
          Start date is required
        </mat-error>
      </mat-form-field>
      
      <div class="form-actions">
        <button mat-raised-button color="primary" type="submit" [disabled]="eventForm.invalid">
          Create Event
        </button>
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
      </div>
    </form>
  `
})
export class EventFormComponent implements OnInit {
  eventForm: FormGroup;
  
  constructor(
    private fb: FormBuilder,
    private store: Store<AppState>
  ) {
    this.eventForm = this.createForm();
  }
  
  private createForm(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(2000)]],
      startDate: ['', Validators.required],
      endDate: [''],
      location: ['', [Validators.required, Validators.maxLength(255)]],
      category: [''],
      maxCapacity: ['', [Validators.min(1)]]
    }, {
      validators: [this.dateRangeValidator]
    });
  }
  
  private dateRangeValidator(control: AbstractControl): ValidationErrors | null {
    const startDate = control.get('startDate')?.value;
    const endDate = control.get('endDate')?.value;
    
    if (startDate && endDate && new Date(endDate) <= new Date(startDate)) {
      return { dateRange: true };
    }
    
    return null;
  }
  
  onSubmit() {
    if (this.eventForm.valid) {
      const eventData = this.eventForm.value;
      this.store.dispatch(EventActions.createEvent({ event: eventData }));
    }
  }
}
```

#### **HTTP Interceptors**
```typescript
// auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      const authReq = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
      return next.handle(authReq);
    }
    
    return next.handle(req);
  }
}

// error.interceptor.ts
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private snackBar: MatSnackBar) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred';
        
        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = error.error.message;
        } else {
          // Server-side error
          switch (error.status) {
            case 401:
              errorMessage = 'Unauthorized access';
              break;
            case 403:
              errorMessage = 'Access forbidden';
              break;
            case 404:
              errorMessage = 'Resource not found';
              break;
            case 500:
              errorMessage = 'Internal server error';
              break;
            default:
              errorMessage = error.error?.message || errorMessage;
          }
        }
        
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        return throwError(() => error);
      })
    );
  }
}
```

### **6. Testing Strategies**

#### **Spring Boot Testing**
```java
// Integration Tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class EventControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Test
    void shouldCreateEvent() {
        // Given
        Event event = Event.builder()
            .title("Test Event")
            .description("Test Description")
            .startDate(LocalDateTime.now().plusDays(1))
            .location("Test Location")
            .build();
        
        // When
        ResponseEntity<Event> response = restTemplate.postForEntity("/api/events", event, Event.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Event");
        
        // Verify in database
        Optional<Event> savedEvent = eventRepository.findById(response.getBody().getId());
        assertThat(savedEvent).isPresent();
    }
}

// Unit Tests
@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    
    @Mock
    private EventRepository eventRepository;
    
    @InjectMocks
    private EventService eventService;
    
    @Test
    void shouldCreateEventSuccessfully() {
        // Given
        Event event = new Event();
        event.setTitle("Test Event");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        
        // When
        Event result = eventService.createEvent(event);
        
        // Then
        assertThat(result.getTitle()).isEqualTo("Test Event");
        verify(eventRepository).save(event);
    }
}
```

#### **Angular Testing**
```typescript
// Component Testing
describe('EventListComponent', () => {
  let component: EventListComponent;
  let fixture: ComponentFixture<EventListComponent>;
  let store: MockStore;
  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventListComponent],
      providers: [
        provideMockStore({
          initialState: {
            events: {
              events: [],
              loading: false,
              error: null
            }
          }
        })
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(EventListComponent);
    component = fixture.componentInstance;
    store = TestBed.inject(MockStore);
  });
  
  it('should dispatch loadEvents on init', () => {
    const dispatchSpy = spyOn(store, 'dispatch');
    
    component.ngOnInit();
    
    expect(dispatchSpy).toHaveBeenCalledWith(EventActions.loadEvents());
  });
  
  it('should display events', () => {
    const mockEvents = [
      { id: 1, title: 'Event 1', description: 'Description 1' },
      { id: 2, title: 'Event 2', description: 'Description 2' }
    ];
    
    store.setState({
      events: {
        events: mockEvents,
        loading: false,
        error: null
      }
    });
    
    fixture.detectChanges();
    
    const eventCards = fixture.debugElement.queryAll(By.css('.event-card'));
    expect(eventCards.length).toBe(2);
  });
});

// Service Testing
describe('EventService', () => {
  let service: EventService;
  let httpMock: HttpTestingController;
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventService]
    });
    
    service = TestBed.inject(EventService);
    httpMock = TestBed.inject(HttpTestingController);
  });
  
  it('should fetch events', () => {
    const mockEvents = [
      { id: 1, title: 'Event 1' },
      { id: 2, title: 'Event 2' }
    ];
    
    service.getEvents().subscribe(events => {
      expect(events.length).toBe(2);
      expect(events).toEqual(mockEvents);
    });
    
    const req = httpMock.expectOne('/api/events');
    expect(req.request.method).toBe('GET');
    req.flush(mockEvents);
  });
  
  afterEach(() => {
    httpMock.verify();
  });
});
```

## 🎨 **UI/UX with Angular Material**

### **Material Design Implementation**
```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideStore(reducers),
    provideEffects([EventEffects, AuthEffects]),
    importProvidersFrom([
      MatToolbarModule,
      MatSidenavModule,
      MatCardModule,
      MatButtonModule,
      MatFormFieldModule,
      MatInputModule,
      MatDatepickerModule,
      MatNativeDateModule,
      MatSnackBarModule,
      MatDialogModule,
      MatTableModule,
      MatPaginatorModule,
      MatSortModule
    ])
  ]
};
```

### **Responsive Layout**
```scss
// event-list.component.scss
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
  padding: 1rem;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    padding: 0.5rem;
  }
}

.event-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  mat-card-content {
    flex: 1;
  }
  
  .event-meta {
    display: flex;
    justify-content: space-between;
    margin-top: 1rem;
    font-size: 0.875rem;
    color: rgba(0, 0, 0, 0.6);
  }
}
```

## 🔒 **Security Implementation**

### **Method-Level Security**
```java
@RestController
@PreAuthorize("hasRole('USER')")
public class EventController {
    
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        // Only organizers and admins can create events
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @eventService.isOwner(#id, authentication.name))")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event event) {
        // Only admins or event owners can update
    }
}
```

### **Input Validation**
```java
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer maxCapacity;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
}
```

## 🎓 **Skills Demonstrated**

### **Backend Skills**
- ✅ **Spring Boot 3** - Latest enterprise framework
- ✅ **Spring Data JPA** - Advanced ORM with custom queries
- ✅ **Spring Security 6** - Authentication and authorization
- ✅ **PostgreSQL** - Complex relational database design
- ✅ **Redis Caching** - Performance optimization
- ✅ **JWT Authentication** - Stateless security
- ✅ **Bean Validation** - Input validation with annotations
- ✅ **Flyway Migrations** - Database version control
- ✅ **Exception Handling** - Global error management
- ✅ **Testing** - Unit and integration tests

### **Frontend Skills**
- ✅ **Angular 17** - Latest version with standalone components
- ✅ **TypeScript 5** - Advanced type system
- ✅ **NgRx** - State management with Redux pattern
- ✅ **RxJS** - Reactive programming
- ✅ **Angular Material** - Professional UI components
- ✅ **Reactive Forms** - Complex form validation
- ✅ **HTTP Interceptors** - Request/response handling
- ✅ **Route Guards** - Navigation security
- ✅ **Testing** - Component and service testing

### **Architecture & Patterns**
- ✅ **Layered Architecture** - Separation of concerns
- ✅ **Repository Pattern** - Data access abstraction
- ✅ **Service Layer** - Business logic encapsulation
- ✅ **DTO Pattern** - Data transfer objects
- ✅ **Observer Pattern** - Reactive programming
- ✅ **Dependency Injection** - Loose coupling
- ✅ **SOLID Principles** - Clean code practices

## 🚀 **Deployment & Production**

### **Docker Configuration**
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-slim
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### **Production Configuration**
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:eventhub}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      
logging:
  level:
    com.eventhub: INFO
    org.springframework.security: WARN
```

This comprehensive learning guide demonstrates enterprise-level Java development with Spring Boot and modern Angular practices, making it perfect for job interviews and professional development.