import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ConnectComponent } from './spotify/connect/connect.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ConnectComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
