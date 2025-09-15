import './polyfills';
import { bootstrapApplication } from '@angular/platform-browser';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './app/auth/auth.service';
import { TokenInterceptor } from './app/auth/token.interceptor';
import { authGuard } from './app/auth/auth.guard';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptorsFromDi()),

    importProvidersFrom(FormsModule, ReactiveFormsModule),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,
    },

    provideRouter([
      {
        path: 'login',
        loadComponent: () =>
          import('./app/auth/login/login.component').then(
            (c) => c.LoginComponent
          ),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./app/auth/register/register.component').then(
            (c) => c.RegisterComponent
          ),
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./app/home/home.component').then((c) => c.HomeComponent),
        canActivate: [authGuard],
      },
      {
        path: 'packs',
        loadComponent: () =>
          import('./app/packs/packs.component').then((c) => c.PacksComponent),
      },
      {
        path: 'rooms',
        loadComponent: () =>
          import('./app/rooms/rooms.component').then((c) => c.RoomsComponent),
      },
      {
        path: 'game',
        loadComponent: () =>
          import('./app/game/game.component').then((c) => c.GameComponent),
      },
      {
        path: '',
        redirectTo: '/login',
        pathMatch: 'full',
      },
    ]),
  ],
}).catch((err) => console.error(err));
