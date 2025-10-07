import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface QuestionPackDTO {
  name: string;
  questions: string[];
}

@Component({
  selector: 'app-packs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './packs.component.html',
  styleUrl: './packs.component.scss',
})
export class PacksComponent {
  packs: any[] = [];
  newPackName = '';
  showCreateModal = false;
  newPackQuestions: string[] = [''];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadPacks();
  }

  loadPacks() {
    this.http.get<any[]>('http://localhost:8080/api/packs').subscribe({
      next: (data) => {
        this.packs = data;
      },
      error: (err) => {
        console.error('Failed to load packs', err);
      },
    });
  }

  openCreateModal() {
    this.showCreateModal = true;
    this.newPackName = '';
    this.newPackQuestions = [''];
  }

  closeCreateModal() {
    this.showCreateModal = false;
  }

  addQuestion() {
    this.newPackQuestions.push('');
  }

  removeQuestion(index: number) {
    if (this.newPackQuestions.length > 1) {
      this.newPackQuestions.splice(index, 1);
    }
  }

  createPack() {
    const name = this.newPackName.trim();
    const questions = this.newPackQuestions
      .map((q) => q.trim())
      .filter((q) => q.length > 0);

    if (!name) {
      alert('Please enter a pack name');
      return;
    }

    if (questions.length === 0) {
      alert('Please add at least one question');
      return;
    }

    const packDto: QuestionPackDTO = {
      name: name,
      questions: questions,
    };

    this.http.post('http://localhost:8080/api/packs', packDto).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.loadPacks();
      },
      error: (err) => {
        console.error('Failed to create pack', err);
      },
    });
  }

  createRoom(packId: string, packName: string) {
    this.router.navigate(['/rooms'], { queryParams: { packName, packId } });
  }
}
