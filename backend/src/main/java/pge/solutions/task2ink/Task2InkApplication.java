package pge.solutions.task2ink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Task2InkApplication {

	public static void main(String[] args) {
		SpringApplication.run(Task2InkApplication.class, args);
	}

}
