import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-packs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './packs.component.html',
  styleUrl: './packs.component.scss'
})
export class PacksComponent {
  packs: any[] = [];

  constructor(private http: HttpClient, private router:Router){}

  ngOnInit(){
    this.http.get('http://localhost:8080/api/packs').subscribe((data :any)=>{
      this.packs = data;
    })
  }

  createRoom(packId: string, packName: string){
    this.router.navigate(['/rooms'], { queryParams: {packName, packId}} );
  }
}
