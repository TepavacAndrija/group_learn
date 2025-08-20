import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rooms.component.html',
  styleUrl: './rooms.component.scss'
})
export class RoomsComponent {

  rooms: any[] = [];
  packId = '';
  packName = '';

  constructor(private http:HttpClient, private route:ActivatedRoute, private router:Router){}

  ngOnInit(){

    this.route.queryParams.subscribe(params=>{
      this.packName = params['packName'] || '';
      this.packId = params['packId'] || '';
    });

    this.loadRooms();
  }

  loadRooms(){
    this.http.get(`http://localhost:8080/api/rooms`).subscribe((data : any)=>{
      this.rooms=data;
    });
  }

  createRoom(){
    if(!this.packId) return;

    const roomData = {
      packId: this.packId,
      maxPlayers: 6
    };

    this.http.post('http://localhost:8080/api/rooms', roomData).subscribe({
      next: () => {
        this.loadRooms();
      },
      error : (e) => {
        alert("failed to create room" + e);
      }
    });
  }

  joinRoom(code: string) {
    const joinData = { code };

    this.http.post('http://localhost:8080/api/rooms/join', joinData).subscribe({
      next: () => {
        this.router.navigate(['/game'], { queryParams: { code } });
      },
      error: (err) => {
        alert('Failed to join room');
      }
    });
  }
}
