package pge.solutions.task2ink.services;

import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.AppConfig;
import pge.solutions.task2ink.dto.NewTodoEvent;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ToDoService {

    private final HashMap<Integer, List<VToDo>> contents = new HashMap<>();

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
            var newTodos = calDavService.connect(appConfig.calendars().get(i));
            var oldTodos = contents.get(i);
            if (null == oldTodos){
                log.info("Got {} Todos initial from Server", newTodos.size());
                contents.put(i, newTodos);
            } else {
                var diff = newTodos.stream().filter(todo -> !oldTodos.contains(todo)).toList();
                if(!diff.isEmpty()){
                    log.info("Got {} new Todos from Server", diff.size());
                    for (VToDo vToDo : diff) {
                        publisher.publishEvent(new NewTodoEvent(vToDo));
                    }
                    contents.put(i, newTodos);
                } else {
                    log.debug("No new Todos from Server");
                }
            }
        }
    }
}
