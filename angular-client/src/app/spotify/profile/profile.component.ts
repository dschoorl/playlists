import { Component, computed, inject, OnInit } from '@angular/core';
import { SpotifyService } from '../spotify.service';
import { UserProfile } from '@spotify/web-api-ts-sdk';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  private spotifyService = inject(SpotifyService);
  userProfile = computed<UserProfile | undefined>(() =>
    this.spotifyService.ro_userProfile()
  );
}
