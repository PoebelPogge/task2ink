import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {TodoService} from './services/todo.service';
import { CommonModule } from '@angular/common';
// 2. WICHTIG: Die Material-Komponenten
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    CommonModule,     // Erlaubt *ngFor
    MatListModule,    // Erlaubt <mat-list>
    MatIconModule,    // Erlaubt <mat-icon>
    MatDividerModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  todoService = inject(TodoService)
  todos$ = this.todoService.todos$;
  title = 'task2ink';
}
