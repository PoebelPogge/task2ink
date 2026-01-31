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
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.CalDavCredential;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CalDavService {


    public List<VToDo> connect(CalDavCredential credential){
        try{
            Sardine sardine = SardineFactory.begin(credential.username(), credential.password());
            if(sardine.exists(credential.url())){
                InputStream is = sardine.get(credential.url());

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

                log.debug("Fetched {} open Tasks from CalDav Server", filteredTodos.size());
                return filteredTodos;
            } else {
                log.warn("Could not fetch any information from CalDav Server");
                return Collections.emptyList();
            }
        } catch (IOException | ParserException e) {
            log.error("Unable to read from CalDav Server", e);
            return Collections.emptyList();
        }
    }
}
