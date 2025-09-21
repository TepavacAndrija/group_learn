import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import * as Stomp from '@stomp/stompjs';

import SockJS from 'sockjs-client';

interface Room {
  id: string;
  code: string;
  packId: string;
  packName: string;
  status: 'WAITING' | 'ACTIVE' | 'FINISHED';
  currentQuestionIndex: number;
  currentAnswererId: string;
  hostId: string;
}

interface Question {
  id: string;
  text: string;
  answer?: string;
}

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss',
})
export class GameComponent {
  code = '';
  packId = '';
  pack: any = null;
  questions: any[] = [];
  currentQuestionIndex = 0;
  currentQuestion: any = null;
  myAnswer = '';
  corrections: string[] = [];
  isAnswerer = false;
  playerRole: 'answerer' | 'corrector' = 'corrector';
  room: Room | null = null;
  isHost = false;
  gameStarted = false;
  gameFinished = false;

  private client: any;
  private roomUpdateSubscription: any;
  private gameUpdateSubscription: any;

  private getCurrentUserId(): string {
    return this.authService.getPlayerId();
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.code = this.route.snapshot.queryParams['code'];
    if (!this.code) {
      this.router.navigate(['/rooms']);
      return;
    }
    this.loadRoomAndPack();

    this.connectWebSocket();
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

        // this.roomUpdateSubscription = this.client.subscribe(
        //   `/topic/room/${this.code}`,
        //   (message: Stomp.IMessage) => {
        //     const room = JSON.parse(message.body);
        //     this.handleRoomUpdate(room);
        //   }
        // );

        this.gameUpdateSubscription = this.client.subscribe(
          `/topic/game/${this.code}`,
          (message: Stomp.IMessage) => {
            const update = JSON.parse(message.body);
            this.handleGameUpdate(update);
          }
        );
      },
      onStompError: (error) => {
        console.error('STOMP Error:', error);
      },
    });

    this.client.activate();
  }

  // private handleRoomUpdate(room: Room) {
  //   this.room = room;

  //   if (room.status === 'ACTIVE' && !this.gameStarted) {
  //     this.gameStarted = true;
  //     this.loadQuestion();
  //   } else if (room.status === 'FINISHED') {
  //     this.gameFinished = true;
  //   }
  // }

  private handleGameUpdate(update: any) {
    console.log('Game update received:', update);

    switch (update.type) {
      case 'GAME_STARTED':
        this.gameStarted = true;
        this.room!.currentAnswererId = update.currentAnswererId;
        this.loadQuestion();

        break;

      case 'NEXT_QUESTION':
        this.room!.currentQuestionIndex = update.currentQuestionIndex;
        this.room!.currentAnswererId = update.currentAnswererId;
        this.saveCorrections();
        this.loadQuestion();
        break;

      case 'GAME_FINISHED':
        this.gameFinished = true;
        this.saveCorrections();
        this.loadRoomAndPack();
        break;
    }
  }

  ngOnDestroy() {
    if (this.client) {
      this.client.deactivate();
    }

    if (this.roomUpdateSubscription) {
      this.roomUpdateSubscription.unsubscribe();
    }
    if (this.gameUpdateSubscription) {
      this.gameUpdateSubscription.unsubscribe();
    }
  }

  loadRoomAndPack() {
    this.http
      .get<Room>(`http://localhost:8080/api/rooms/code/${this.code}`)
      .subscribe({
        next: (room) => {
          this.room = room;
          this.isHost = room.hostId === this.getCurrentUserId();
          this.packId = room.packId;

          console.log('Fetching pack details for packId:', this.packId);
          this.http
            .get(`http://localhost:8080/api/packs/${this.packId}`)
            .subscribe((data: any) => {
              this.pack = data;
              this.questions = data.questions;
            });

          if (room.status === 'ACTIVE') {
            this.loadQuestion();
            this.gameStarted = true;
          } else if (room.status === 'FINISHED') {
            alert('This game has already finished.');
            this.gameFinished = true;
            this.router.navigate(['/home']);
          }
        },
      });
  }

  private loadQuestion() {
    if (!this.room) return;

    this.http
      .get<Question>(`http://localhost:8080/api/game/question/${this.room.id}`)
      .subscribe({
        next: (question) => {
          this.questions = [question];
          this.currentQuestion = question;
          this.checkIfCurrentAnswerer();
        },
        error: (err) => {
          console.error('Failed to load question:', err);
        },
      });
  }

  private checkIfCurrentAnswerer() {
    if (!this.room) return;

    this.http
      .get<{ isAnswerer: boolean }>(
        `http://localhost:8080/api/game/is-answerer?roomId=${this.room.id}`
      )
      .subscribe({
        next: (response) => {
          this.isAnswerer = response.isAnswerer;
        },
        error: (err) => {
          console.error('Failed to check if answerer:', err);
        },
      });
  }

  startGame() {
    if (!this.room || !this.isHost) return;

    const payload = { roomId: this.room.id };

    this.http.post('http://localhost:8080/api/game/start', payload).subscribe({
      next: () => {
        console.log('Game started successfully');
      },
      error: (err) => {
        console.error('Failed to start game:', err);
        alert('Failed to start game: ' + err.message);
      },
    });
  }

  addCorrection() {
    if (this.myAnswer.trim()) {
      this.corrections.push(this.myAnswer);
      this.myAnswer = '';
    }
  }

  submitAnswer() {
    if (!this.room || !this.isAnswerer || !this.myAnswer.trim()) return;

    const answer = {
      roomId: this.room.id,
      playerId: this.getCurrentUserId(),
      questionId: this.questions[0].id,
      text: this.myAnswer,
    };

    this.http.post('http://localhost:8080/api/game/answer', answer).subscribe({
      next: () => {
        this.moveToNextQuestion();
      },
      error: (err) => {
        console.error('Failed to submit answer:', err);
      },
    });
  }

  saveCorrections() {
    if (!this.room || !this.currentQuestion) {
      console.error('No room or current question available');
      return;
    }

    const correctionsToSend: { playerId: string; text: string }[] = [];
    const currentUserId = this.getCurrentUserId();

    if (this.corrections && this.corrections.length > 0) {
      for (const correctionText of this.corrections) {
        if (correctionText.trim()) {
          correctionsToSend.push({
            playerId: currentUserId,
            text: correctionText.trim(),
          });
        }
      }
    }

    const correctionsBody = {
      roomId: this.room.id,
      questionId: this.currentQuestion.id,
      corrections: correctionsToSend,
    };

    this.http
      .post('http://localhost:8080/api/game/corrections', correctionsBody)
      .subscribe({
        next: (response) => {
          console.log('Corrections submitted successfully:', response);
          this.corrections = [];
        },
        error: (err) => {
          console.error('Failed to submit corrections:', err);
        },
      });
  }

  moveToNextQuestion() {
    if (!this.room || !this.currentQuestion) {
      console.error('No room or current question available');
      return;
    }
    const body = { roomId: this.room.id };

    this.http.post('http://localhost:8080/api/game/next', body).subscribe({
      next: () => {
        console.log('Moved to next question');
        this.loadQuestion();
      },
      error: (err) => {
        console.error('Failed to move to next question:', err);
        alert('Failed to move to next question: ' + err.message);
      },
    });
  }
}
