package pge.solutions.task2ink.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.*;
import pge.solutions.task2ink.mapper.ToDoMapper;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ToDoService {

    @Getter
    private final HashMap<Integer, TodoList> contents = new HashMap<>();

    private final CalDavService calDavService;
    private final AppConfig appConfig;
    private final ApplicationEventPublisher publisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ToDoMapper toDoMapper;

    public ToDoService(CalDavService calDavService, AppConfig appConfig, ApplicationEventPublisher publisher, SimpMessagingTemplate messagingTemplate, ToDoMapper toDoMapper) {
        this.calDavService = calDavService;
        this.appConfig = appConfig;
        this.publisher = publisher;
        this.messagingTemplate = messagingTemplate;
        this.toDoMapper = toDoMapper;
    }

    @Scheduled(fixedRate = 5000)
    public void fetchTodos(){
        for (int i = 0; i < appConfig.calendars().size(); i++) {
            var todoList = calDavService.getOpenTasks(appConfig.calendars().get(i));
            var oldTodos = contents.get(i);
            if (null == oldTodos){
                log.info("Got {} Todos initial from Server", todoList.todos().size());
                contents.put(i, todoList);
            } else {
                var diff = todoList.todos().stream().filter(todo -> !oldTodos.todos().contains(todo)).toList();
                if(!diff.isEmpty()){
                    log.info("Got {} new Todos from Server", diff.size());
                    for (VToDo vToDo : diff) {
                        publisher.publishEvent(new NewTodoEvent(todoList.name(), vToDo));
                    }
                    contents.put(i, todoList);
                } else {
                    log.debug("No new Todos from Server");
                }
            }
        }
        List<ApiToDo> apiTodos = contents.get(0).todos().stream().map(toDoMapper::toApi).toList();
        String destination = "/topic/todos/1";
        messagingTemplate.convertAndSend(destination, apiTodos);
    }

    public ApiToDo completeTodo(String uid) throws Exception {

        CalDavCredential credentials = appConfig.calendars().getFirst(); //TODO! Richtigen Kalender auswählen
        return calDavService.uploadToCalDav(credentials, uid);
    }
}
