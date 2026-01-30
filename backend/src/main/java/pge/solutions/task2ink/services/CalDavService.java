package pge.solutions.task2ink.services;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.AppConfig;
import pge.solutions.task2ink.dto.CalDavCredential;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class CalDavService {

    private final AppConfig config;

    public CalDavService(AppConfig config) {
        this.config = config;
    }


    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void connect(){
        for (CalDavCredential credentials : config.calendars()) {
            try{
                Sardine sardine = SardineFactory.begin(credentials.username(), credentials.password());
                if(sardine.exists(credentials.url())){
                    InputStream is = sardine.get(credentials.url());

                    CalendarBuilder builder = new CalendarBuilder();
                    Calendar calendar = builder.build(is);
                    List<VToDo> todos = calendar.getComponents(Component.VTODO);

                    List<VToDo> filteredTodos = todos.stream()
                            .filter(todo -> {
                                Completed completed = todo.getDateCompleted();
                                if(null == completed){
                                    return true;
                                }

                                Instant completedAt = completed.getDate().toInstant();
                                return completedAt.isAfter(Instant.now());
                            }).toList();

                    filteredTodos.forEach(System.out::println);
                } else {
                    log.warn("Could not fetch any information from CalDav Server");
                }
            } catch (IOException | ParserException e) {
                log.error("Unable to read from CalDav Server", e);
            }
        }
    }
}
