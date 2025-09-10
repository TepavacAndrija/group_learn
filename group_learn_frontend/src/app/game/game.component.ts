import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss'
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.code = this.route.snapshot.queryParams['code'];
    if (!this.code) {
      this.router.navigate(['/rooms']);
      return;
    }

    this.playerRole = 'answerer';
    this.isAnswerer = this.playerRole === 'answerer';

    this.loadRoomAndPack();
  }

  loadRoomAndPack(){
    this.packId = '689fa0409f0fd4e2ccff470d'; 

    this.http.get(`http://localhost:8080/api/packs/${this.packId}`).subscribe((data: any) => {
      this.pack = data;
      this.questions = data.questions;
      this.currentQuestion = this.questions[0];
    });
  }

  nextQuestion() {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
      this.currentQuestion = this.questions[this.currentQuestionIndex];
      this.myAnswer = '';
      this.corrections = [];
      this.switchRoles();
    } else {
      alert('Game over! Well done!');
      this.router.navigate(['/home']);
    }
  }

  switchRoles() {
    this.playerRole = this.playerRole === 'answerer' ? 'corrector' : 'answerer';
    this.isAnswerer = this.playerRole === 'answerer';
  }

    addCorrection() {
    if (this.myAnswer.trim()) {
      this.corrections.push(this.myAnswer);
      this.myAnswer = '';
    }
  }

  submitAnswer() {
    if (!this.myAnswer.trim()) return;
    console.log('Submitted answer:', this.myAnswer);
    this.nextQuestion();
  }
}
