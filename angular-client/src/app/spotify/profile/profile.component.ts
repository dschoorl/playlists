import { Component, inject, OnInit } from '@angular/core';
import { SpotifyService } from '../spotify.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  private spotifyService = inject(SpotifyService);
  userProfile = this.spotifyService.ro_userProfile;
}
