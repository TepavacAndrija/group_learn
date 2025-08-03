import { Component } from '@angular/core';
import { Router,RouterOutlet, RouterLink } from '@angular/router';
import { AuthService } from './auth/auth.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet,RouterLink]
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit(){
    //this.logout();
    //just for testing jwt tokens
  }
  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}