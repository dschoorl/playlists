import { inject } from '@angular/core';
import {
  CanMatchFn,
  GuardResult,
  MaybeAsync,
  RedirectCommand,
  Route,
  Router,
  UrlSegment,
} from '@angular/router';
import { SpotifyService } from './spotify.service';

export const connectedGuard: CanMatchFn = function (
  route: Route,
  segments: UrlSegment[]
): MaybeAsync<GuardResult> {
  const spotifyService = inject(SpotifyService);
  const router = inject(Router);

  // this guard is only applied to /main/* paths, we do not need to consider the actual path
  if (spotifyService.isConnected()) {
    console.log(
      'Connected to Spotify, continue to ' +
        segments.map((s) => s.path).join('/')
    );
    return true;
  }

  console.log('Not connected to Spotify, redirect to /connect');
  return new RedirectCommand(router.parseUrl('/connect'));
};
