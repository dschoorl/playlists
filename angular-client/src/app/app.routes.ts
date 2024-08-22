import { Routes } from '@angular/router';
import { ConnectComponent } from './spotify/connect/connect.component';
import { connectedGuard } from './spotify/spotify.guard';
import { MainComponent } from './main/main.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/main',
  },
  {
    path: 'connect',
    component: ConnectComponent,
    title: 'Playlist: Connect',
  },
  {
    path: 'main',
    component: MainComponent,
    canMatch: [connectedGuard],
    title: 'Playlist: Release year',
    runGuardsAndResolvers: 'always',
  },
  {
    path: '**',
    redirectTo: '/main',
  },
];
