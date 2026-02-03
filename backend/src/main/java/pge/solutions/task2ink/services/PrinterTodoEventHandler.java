package pge.solutions.task2ink.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pge.solutions.task2ink.dto.NewTodoEvent;

@Service
@Slf4j
public class PrinterTodoEventHandler {

    private final PrinterProcessService printerProcessService;

    public PrinterTodoEventHandler(PrinterProcessService printerProcessService) {
        this.printerProcessService = printerProcessService;
    }


    @EventListener
    public void callPrinter(NewTodoEvent newTodoEvent){
        var todo = newTodoEvent.toDo();
        var name = newTodoEvent.listName();
        printerProcessService.printTask(name, todo);
        log.info("Going to print todo with id: {} and content '{}' via python.", todo.getUid().getValue(), todo.getSummary().getValue());
    }
}
