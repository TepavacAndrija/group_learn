import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import * as Stomp from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit, OnDestroy {
  constructor(private http: HttpClient, private router: Router) {}

  client: any;
  roomCode: string = '';
  isJoining: boolean = false;
  errorMessage: string = '';

  ngOnInit(): void {
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    if (this.client) {
      this.client.deactivate();
    }
  }

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
        this.client.subscribe(
          `/user/queue/room-joined`,
          (message: Stomp.IMessage) => {
            const data = JSON.parse(message.body);
            console.log('Received room-joined message:', data);

            if (this.isJoining && this.roomCode === data.code) {
              this.router.navigate(['/game'], {
                queryParams: { code: data.code },
              });
              this.isJoining = false;
              this.errorMessage = '';
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

  joinRoom() {
    if (!this.roomCode.trim()) {
      this.errorMessage = 'Please enter a room code.';
      return;
    }
    this.isJoining = true;
    this.errorMessage = '';

    const joinData = { code: this.roomCode.trim().toUpperCase() };

    this.http.post('http://localhost:8080/api/rooms/join', joinData).subscribe({
      next: () => {
        console.log('Join request sent, waiting for WebSocket confirmation...');
      },
      error: (err) => {
        this.isJoining = false;
        this.errorMessage =
          err.error?.message ||
          'Failed to join room. Please check the code and try again.';
        console.error('Failed to join room:', err);
      },
    });
  }

  isConnected(): boolean {
    return this.client && this.client.connected;
  }
}
