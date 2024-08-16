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
    return true;
  }

  return new RedirectCommand(router.parseUrl('/start'));
};
