package pge.solutions.task2ink.services;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pge.solutions.task2ink.dto.PrintableToDo;
import pge.solutions.task2ink.dto.PrinterResponse;
import pge.solutions.task2ink.mapper.ToDoMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class PrinterProcessService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ToDoMapper toDoMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    public PrinterProcessService(ToDoMapper toDoMapper) {
        this.toDoMapper = toDoMapper;
    }

    @Async
    public void printTask(String listName, VToDo todo){

        PrintableToDo printableToDo = toDoMapper.toPrint(todo, listName);

        String url = "http://localhost:5001/print";
        ResponseEntity<PrinterResponse> response = restTemplate.postForEntity(url, printableToDo, PrinterResponse.class);

        if(!HttpStatus.OK.equals(response.getStatusCode())){
            assert response.getBody() != null;
            log.error("Unable to print todo, see details: {}", response.getBody().message());
        } else {
            log.info("Todo with summary: {} was printed successfully", todo.getSummary().getValue());
        }

    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
