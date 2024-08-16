import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { ConnectComponent } from './spotify/connect/connect.component';
import { connectedGuard } from './spotify/spotify.guard';
import { MainComponent } from './main/main.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/start',
  },
  {
    path: 'start',
    component: ConnectComponent,
  },
  { path: 'main', component: MainComponent, canMatch: [connectedGuard] },
];
