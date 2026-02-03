package pge.solutions.task2ink.services;

import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class PrinterProcessService {

    @Async
    public void printTask(VToDo todo){
        try {
            String pythonExecutable = "./venv/bin/python3";
            String scriptPath = "main.py";
            File workingDir = new File("./tools/printer-interface");

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    todo.getSummary().getValue()
            );

            pb.directory(workingDir);

            pb.inheritIO();

            Process process = pb.start();

            int exitCode = process.waitFor();
            if(exitCode != 0){
                log.error("Unable to print todo, see exit code: {}", exitCode);
            } else {
                log.info("Todo with summary: {} was printed successfully", todo.getSummary().getValue());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Unable to print todo, see root cause:", e);

        }
    }
}
