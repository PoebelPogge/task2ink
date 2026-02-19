package pge.solutions.task2ink.controller;

import com.samskivert.mustache.Mustache;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import pge.solutions.task2ink.services.ToDoService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoRestController {

    private final ToDoService toDoService;

    public TodoRestController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }


    @GetMapping("/{uid}/complete")
    public ResponseEntity<String> markAsComplete(@PathVariable String uid, @RequestHeader(value = "User-Agent") String userAgent) {
        try {
            toDoService.completeTodo(uid);
            if(userAgent.contains("ESP32CAM-Tool")){
                return ResponseEntity.ok("DONE");
            } else {
                String template = StreamUtils.copyToString(new ClassPathResource("templates/success.mustache").getInputStream(), StandardCharsets.UTF_8);

                Map<String, String> data = new HashMap<>();
                data.put("uid", uid);
                data.put("summary", "foobar123");

                String body = Mustache.compiler().compile(template).execute(data);

                return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
