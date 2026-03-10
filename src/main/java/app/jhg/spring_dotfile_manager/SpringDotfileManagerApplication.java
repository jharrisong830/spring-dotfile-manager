package app.jhg.spring_dotfile_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.core.command.annotation.EnableCommand;

@SpringBootApplication
@EnableCommand
public class SpringDotfileManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDotfileManagerApplication.class, args);
	}

}
