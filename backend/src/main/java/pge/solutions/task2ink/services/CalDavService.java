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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pge.solutions.task2ink.dto.CalDavCredential;
import pge.solutions.task2ink.dto.TodoList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CalDavService {

    private String listname = "";

    private void getListName(CalDavCredential credential) throws IOException, InterruptedException {
        String listUrl = credential.url();
        String collectionUrl = listUrl.endsWith(".ics") ? listUrl.substring(0, listUrl.lastIndexOf("/")) : listUrl;

        String auth = credential.username() + ":" + credential.password();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        String xmlBody = """
            <?xml version="1.0" encoding="utf-8" ?>
            <D:propfind xmlns:D="DAV:">
              <D:prop><D:displayname/></D:prop>
            </D:propfind>
            """;

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(collectionUrl))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "text/xml")
                    .header("Depth", "1")
                    .method("PROPFIND", HttpRequest.BodyPublishers.ofString(xmlBody))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        String xmlResponse = response.body();

        String calenderID = extractWithRegex(credential.url());
        this.listname = getDisplayNameForUuid(xmlResponse, calenderID);
    }

    private String extractWithRegex(String url) {
        // Muster für eine klassische UUID
        Pattern uuidPattern = Pattern.compile(".*/([0-9a-zA-Z-]*).+");
        Matcher matcher = uuidPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getDisplayNameForUuid(String xmlResponse, String targetId) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

            // Wir holen alle "response" Elemente
            NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");

            for (int i = 0; i < responses.getLength(); i++) {
                Element response = (Element) responses.item(i);

                // 1. Die URL (href) dieses Blocks prüfen
                String href = response.getElementsByTagNameNS("DAV:", "href")
                        .item(0).getTextContent();

                // Wir prüfen, ob die gesuchte UUID in der URL vorkommt
                // Und wir nehmen nur den Eintrag OHNE .ics/.xml Endung für den sauberen Namen
                if (href.contains(targetId) && !href.endsWith(".ics") && !href.endsWith(".xml")) {

                    // 2. Den Displaynamen aus diesem spezifischen Block extrahieren
                    NodeList displayNames = response.getElementsByTagNameNS("DAV:", "displayname");
                    if (displayNames.getLength() > 0) {
                        String listName = displayNames.item(0).getTextContent();
                        log.info("Got list with name: " + listName);
                        return listName;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to read list name, using default name", e);
        }
        return "Unbekannte Liste";
    }

    public TodoList connect(CalDavCredential credential){
        if("".equals(this.listname)){
            this.listname = "Todo List";
            try {
                getListName(credential);
            } catch (IOException | InterruptedException e) {
                log.warn("Unable to get list name, continuing with default");
            }
        }

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
                return new TodoList(this.listname, filteredTodos);
            } else {
                log.warn("Could not fetch any information from CalDav Server");
                return new TodoList(this.listname, Collections.emptyList());
            }
        } catch (IOException | ParserException e) {
            log.error("Unable to read from CalDav Server", e);
            return new TodoList(this.listname, Collections.emptyList());
        }
    }
}
