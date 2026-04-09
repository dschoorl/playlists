import { Component, inject, OnInit, signal } from '@angular/core';
import { SpotifyService } from '../spotify.service';
import { Router } from '@angular/router';
import { ProfileComponent } from '../profile/profile.component';

const CONNECT_IN_PROGRESS_KEY = 'playlists.connect_in_progress';

/**
 * Display a button that triggers logging in with Spotify OAuth or disconnect when already connected
 */
@Component({
  selector: 'app-connect',
  standalone: true,
  imports: [ProfileComponent],
  templateUrl: './connect.component.html',
  styleUrl: './connect.component.scss',
})
export class ConnectComponent implements OnInit {
  isSpotifyConnected = signal(false);

  constructor(
    private spotifyService: SpotifyService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    console.log(
      '[ConnectComponent.ngOnInit] ConnectComponent instance initialized',
    );
    this.isSpotifyConnected.set(this.spotifyService.isConnected());
    if (this.spotifyService.isConnected() && this.isConnectInProgress()) {
      sessionStorage.removeItem(CONNECT_IN_PROGRESS_KEY);
      this.router.navigate(['/main']);
    }
  }

  async onConnectSpotify() {
    console.log('[ConnectComponent.onConnectSpotify()] Connecting to spotify');
    sessionStorage.setItem(CONNECT_IN_PROGRESS_KEY, 'true');
    const isConnected = await this.spotifyService.authenticate();
    this.isSpotifyConnected.set(isConnected);
    if (isConnected) {
      console.log(
        '[ConnectComponent.onConnectSpotify()] Navigate to /main after connecting to Spotify',
      );
      sessionStorage.setItem(CONNECT_IN_PROGRESS_KEY, 'false');
      this.spotifyService.init();
      this.router.navigate(['/main']);
    }
  }

  onDisconnectSpotify() {
    this.spotifyService.logout();
    this.isSpotifyConnected.set(false);
    sessionStorage.removeItem(CONNECT_IN_PROGRESS_KEY);
  }

  onGoMain() {
    this.router.navigate(['/main']);
  }

  private isConnectInProgress() {
    return sessionStorage.getItem(CONNECT_IN_PROGRESS_KEY) === 'true';
  }
}
