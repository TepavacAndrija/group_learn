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
  correction = '';
  isAnswerer = false;
  playerRole: 'answerer' | 'corrector' = 'corrector';
  room: Room | null = null;
  isHost = false;
  gameStarted = false;
  gameFinished = false;
  answerSubmitted = false;
  correctionSubmitted = false;
  waitingForCorrections = false;
  submittedAnswerText: string | null = null;

  report: any = null;
  showReport = false;

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

  private handleGameUpdate(update: any) {
    console.log('Game update received:', update);

    switch (update.type) {
      case 'CORRECTION_PHASE':
        this.correctionSubmitted = false;
        this.waitingForCorrections = true;
        this.submittedAnswerText = update.info;
        break;

      case 'GAME_STARTED':
        this.gameStarted = true;
        this.room!.currentAnswererId = update.currentAnswererId;
        this.loadQuestion();
        break;

      case 'NEXT_QUESTION':
        this.room!.currentQuestionIndex = update.currentQuestionIndex;
        this.room!.currentAnswererId = update.currentAnswererId;
        this.correctionSubmitted = false;
        this.answerSubmitted = false;
        this.waitingForCorrections = false;
        this.submittedAnswerText = null;
        this.loadQuestion();
        break;

      case 'GAME_FINISHED':
        this.gameFinished = true;
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
            this.gameFinished = true;
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
          this.answerSubmitted = false;
          this.correctionSubmitted = false;
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
        this.answerSubmitted = true;
      },
      error: (err) => {
        console.error('Failed to submit answer:', err);
      },
    });
  }

  viewReport() {
    if (!this.room) return;

    this.http
      .get(`http://localhost:8080/api/game/report/${this.room.id}`)
      .subscribe({
        next: (data) => {
          this.report = data;
          this.showReport = true;
        },
        error: (err) => {
          console.error('Failed to load report:', err);
        },
      });
  }

  goToHome() {
    this.router.navigate(['/home']);
  }
  submitCorrection() {
    if (!this.room || !this.correction.trim()) return;

    const correction = {
      roomId: this.room.id,
      playerId: this.getCurrentUserId(),
      questionId: this.currentQuestion.id,
      text: this.correction,
    };

    this.http
      .post('http://localhost:8080/api/game/correction', correction)
      .subscribe({
        next: () => {
          this.correctionSubmitted = true;
          this.correction = '';
        },
        error: (err) => {
          console.error('Failed to submit correction:', err);
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

  downloadPdf() {
    if (!this.room) return;

    this.http
      .get(`http://localhost:8080/api/game/report/${this.room.id}/pdf`, {
        responseType: 'blob',
      })
      .subscribe({
        next: (pdfBlob: Blob) => {
          const url = window.URL.createObjectURL(pdfBlob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `game_report_${this.room!.id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Failed to download PDF', err);
          alert(
            'Failed to download report: ' + (err.message || 'Unknown error')
          );
        },
      });
  }
}
