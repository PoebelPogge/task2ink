package pge.solutions.task2ink.services;

import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.AppConfig;
import pge.solutions.task2ink.dto.NewTodoEvent;
import pge.solutions.task2ink.dto.TodoList;

import java.util.HashMap;

@Service
@Slf4j
public class ToDoService {

    private final HashMap<Integer, TodoList> contents = new HashMap<>();

    private final CalDavService calDavService;
    private final AppConfig appConfig;
    private final ApplicationEventPublisher publisher;

    public ToDoService(CalDavService calDavService, AppConfig appConfig, ApplicationEventPublisher publisher) {
        this.calDavService = calDavService;
        this.appConfig = appConfig;
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 5000)
    public void fetchTodos(){
        for (int i = 0; i < appConfig.calendars().size(); i++) {
            var todoList = calDavService.connect(appConfig.calendars().get(i));
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
    }
}
