package pge.solutions.task2ink.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import pge.solutions.task2ink.dto.ApiToDo;
import pge.solutions.task2ink.mapper.ToDoMapper;
import pge.solutions.task2ink.services.ToDoService;

import java.util.List;

@Controller
public class TodoWebSocketController {
    private final ToDoService toDoService;
    private final ToDoMapper toDoMapper;

    public TodoWebSocketController(ToDoService toDoService, ToDoMapper toDoMapper) {
        this.toDoService = toDoService;
        this.toDoMapper = toDoMapper;
    }

    @MessageMapping("/getAll-Todos/{listId}")
    @SendTo("/topic/todos/{listId}")
    public List<ApiToDo> getAll(@DestinationVariable Integer listId){
        var todos =  toDoService.getContents().get(0);
        return todos.todos().stream().map(toDoMapper::toApi).toList();
    }
}
