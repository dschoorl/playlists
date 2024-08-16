import { Component, inject, OnInit, signal } from '@angular/core';
import { SpotifyService } from '../spotify.service';
import { Router } from '@angular/router';
/**
 * Display a button that triggers logging in with Spotify OAuth or disconnect when already connected
 */
@Component({
  selector: 'app-connect',
  standalone: true,
  imports: [],
  templateUrl: './connect.component.html',
  styleUrl: './connect.component.scss',
})
export class ConnectComponent {
  isSpotifyConnected = signal(false);

  constructor(private spotifyService: SpotifyService, private router: Router) {
    console.log('ConnectComponent created');
    this.isSpotifyConnected.set(this.spotifyService.isConnected());
  }

  async onConnectSpotify() {
    const isConnected = await this.spotifyService.authenticate();
    this.isSpotifyConnected.set(isConnected);
    if (isConnected) {
      this.router.navigate(['/main']);
    }
  }

  onDisconnectSpotify() {
    this.spotifyService.logout();
    this.isSpotifyConnected.set(false);
  }
}
