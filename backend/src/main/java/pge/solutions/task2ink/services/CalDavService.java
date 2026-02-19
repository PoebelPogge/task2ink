package pge.solutions.task2ink.services;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.PercentComplete;
import net.fortuna.ical4j.model.property.Status;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.CalDavCredential;
import pge.solutions.task2ink.dto.TodoList;
import pge.solutions.task2ink.exceptions.CalenderConnectionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class CalDavService {

    private String listname = "";


    public void uploadToCalDav(CalDavCredential credentials, String uid) throws Exception {
        Sardine sardine = SardineFactory.begin(credentials.username(), credentials.password());

        String baseUrl = credentials.url();

        sardine.enablePreemptiveAuthentication(baseUrl + ".ics");

        // 2. Die spezifische URL für dieses eine Todo
        String taskUrl = baseUrl + String.format("/%s.ics", uid);

        // 3. ETag von der SPEZIFISCHEN Datei holen
        List<DavResource> resources = sardine.list(taskUrl);
        if (resources.isEmpty()) throw new IOException("Todo nicht gefunden");
        String etag = resources.getFirst().getEtag();
        if (!etag.startsWith("\"")) etag = "\"" + etag + "\"";

        InputStream is = sardine.get(taskUrl);
        Calendar calendar = new CalendarBuilder().build(is);
        VToDo target = calendar.getComponent(Component.VTODO);

        var props = target.getProperties();
        props.removeAll(props.getProperties(Property.STATUS));
        props.add(new Status("COMPLETED"));
        props.removeAll(props.getProperties(Property.PERCENT_COMPLETE));
        props.add(new PercentComplete(100));
        props.removeAll(props.getProperties(Property.COMPLETED));
        props.add(new Completed(new DateTime(true)));

        // 5. PUT auf die taskUrl (nicht auf die Sammel-ICS)
        Map<String, String> headers = new HashMap<>();
        headers.put("If-Match", etag);
        headers.put("Content-Type", "text/calendar; charset=utf-8");

        byte[] data = calendar.toString().getBytes(StandardCharsets.UTF_8);
        sardine.put(taskUrl, new ByteArrayInputStream(data), headers);
        log.info("Task with summary: [{}], and uid: {} was marked as complete", target.getSummary().getValue(), target.getUid().getValue());
    }

    public TodoList getOpenTasks(CalDavCredential credential){
        Calendar calendar = null;
        try {
            calendar = this.connect(credential);
        } catch (CalenderConnectionException e) {
            log.error("Unable to get any Todos from CalDav Server");
            return new TodoList(this.listname, Collections.emptyList());
        }

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
        return new TodoList(this.listname, filteredTodos);
    }

    public Calendar connect(CalDavCredential credential) throws CalenderConnectionException {
        try{
            Sardine sardine = SardineFactory.begin(credential.username(), credential.password());
            String baseURL = credential.url() + ".ics";
            if(sardine.exists(baseURL)){
                List<DavResource> resources = sardine.list(baseURL);
                this.listname = resources.getFirst().getDisplayName();

                InputStream is = sardine.get(baseURL);

                CalendarBuilder builder = new CalendarBuilder();

                return builder.build(is);
            }
        } catch (IOException | ParserException e) {
            throw new CalenderConnectionException(credential.url());
        }
        throw new CalenderConnectionException(credential.url());
    }
}
