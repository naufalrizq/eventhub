import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './layout/header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent],
  templateUrl: './app.component.html',
  styles: [`
    main {
      min-height: calc(100vh - 64px);
      background-color: #121212;
    }
  `]
})
export class AppComponent {
  title = 'EventHub';
}
