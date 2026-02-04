package pge.solutions.task2ink.services;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VToDo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pge.solutions.task2ink.dto.PrintableToDo;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class PrinterProcessService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void printTask(String listName, VToDo todo){
        try {
            String pythonExecutable = "./venv/bin/python3";
            String scriptPath = "main.py";
            File workingDir = new File("./tools/printer-interface");

            PrintableToDo printableToDo = new PrintableToDo(todo.getUid().getValue(),listName, todo.getSummary().getValue(),todo.getDescription().getValue(),todo.getDue().getDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    objectMapper.writeValueAsString(printableToDo)
            );

            pb.directory(workingDir);

            Process process = pb.start();

            StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream(), log::info);
            StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream(), log::error);

            // Beide in den Executor schmeißen
            executor.submit(stdoutGobbler);
            executor.submit(stderrGobbler);

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

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
