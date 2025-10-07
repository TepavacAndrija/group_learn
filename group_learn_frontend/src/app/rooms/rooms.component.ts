import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import * as Stomp from '@stomp/stompjs';

import SockJS from 'sockjs-client';
interface Room {
  id: string;
  code: string;
  packName: string;
  currentPlayers: number;
  maxPlayers: number;
  status: 'WAITING' | 'ACTIVE' | 'FINISHED';
}

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './rooms.component.html',
  styleUrl: './rooms.component.scss',
})
export class RoomsComponent implements OnInit, OnDestroy {
  rooms: any[] = [];
  packId = '';
  packName = '';
  joiningRoomCode: string | null = null;
  client: any;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  connectWebSocket() {
    this.client = new Stomp.Client({
      brokerURL: 'ws://localhost:8080/ws',
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {},
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Connected to WebSocket');

        this.client.subscribe('/topic/rooms', (message: Stomp.IMessage) => {
          const data = JSON.parse(message.body);
          console.log('KREIRAO SI ' + message);
          this.loadInitialRooms();
        });

        this.client.subscribe(
          `/user/queue/room-joined`,
          (message: Stomp.IMessage) => {
            const data = JSON.parse(message.body);
            console.log('Received room-joined message:', data);
            if (this.joiningRoomCode && this.joiningRoomCode === data.code) {
              this.router.navigate(['/game'], {
                queryParams: { code: data.code },
              });
              this.joiningRoomCode = null;
            }
          }
        );
      },
      onStompError: (error) => {
        console.error('STOMP Error:', error);
      },
    });

    this.client.activate();
  }

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.packName = params['packName'] || '';
      this.packId = params['packId'] || '';
    });

    this.loadInitialRooms();
    this.connectWebSocket();
  }

  ngOnDestroy() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  loadInitialRooms() {
    this.http
      .get<Room[]>(`http://localhost:8080/api/rooms/broadcast`)
      .subscribe({
        next: (data) => {
          this.rooms = data;
        },
        error: (error) => {
          console.error('Failed to load rooms:', error);
        },
      });
  }

  createRoom() {
    if (!this.packId) return;

    const roomData = {
      packId: this.packId,
    };

    this.http.post('http://localhost:8080/api/rooms', roomData).subscribe();
  }

  joinRoom(code: string) {
    const joinData = { code };

    this.joiningRoomCode = code;

    this.http.post('http://localhost:8080/api/rooms/join', joinData).subscribe({
      next: () => {
        console.log('Join request sent, waiting for WebSocket confirmation...');
      },
      error: (err) => {
        this.joiningRoomCode = null;
        alert('Failed to join room: ' + err.message);
      },
    });
  }

  isConnected(): boolean {
    return this.client && this.client.connected;
  }
}
