import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import {ApiToDo} from '../models/todo.model';


@Injectable({ providedIn: 'root' })
export class TodoService {
  private stompClient: Client;
  // Ein BehaviorSubject ist super, um die Liste in der UI automatisch zu füllen
  public todos$ = new BehaviorSubject<ApiToDo[]>([]);

  constructor() {
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws', // Dein Pi-Endpunkt (ohne /ws-tasks Fallback)
      onConnect: () => {
        console.log('Verbunden mit dem Backend!');
        this.subscribeToList(1); // Testweise Liste ID 1 abonnieren
      }
    });
    this.stompClient.activate();
  }

  subscribeToList(listId: number) {
    this.stompClient.subscribe(`/topic/todos/${listId}`, (message) => {
      const todos = JSON.parse(message.body) as ApiToDo[];
      this.todos$.next(todos); // UI informieren
    });

    // Initialen Request senden
    this.stompClient.publish({ destination: `/app/getAll-Todos/${listId}` });
  }
}
