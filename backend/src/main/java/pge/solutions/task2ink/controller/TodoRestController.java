package pge.solutions.task2ink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pge.solutions.task2ink.services.ToDoService;

@RestController
@RequestMapping("/api/todos")
public class TodoRestController {

    private final ToDoService toDoService;

    public TodoRestController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }


    @GetMapping("/{uid}/complete")
    public ResponseEntity<String> markAsComplete(@PathVariable String uid, @RequestHeader(value = "User-Agent") String userAgent) {
        toDoService.completeTodo(uid);

        return ResponseEntity.ok("DONE");
    }
}
