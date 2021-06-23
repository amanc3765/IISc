package pods.aman.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;

@SpringBootApplication
@RestController
public class DemoApplication {

	@RequestMapping("/")
	public String home() {
		return "<h2 style=\"font-family:verdana\"> Hey! Welcome to my Docker Demo. </h2>";
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "foo") String fileName,
			@RequestParam(value = "content", defaultValue = "Some random text.\n") String fileContent) {

		String log = "";

		try {

			// Create new file
			// -----------------------------------------------------------------------
			File file = new File(fileName + ".txt");

			if (file.createNewFile()) {
				log += "<p style=\"font-family:verdana\"> File <b>" + file.getName()
						+ "</b> successfully created at <b> " + System.getProperty("user.dir") + "</b> </p>";
			} else {
				log += "<p style=\"font-family:verdana\"> File already exists. </p>";
			}

			// Write to file
			// --------------------------------------------------------------------------
			try {
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(fileContent);
				fileWriter.close();

				log += "<p style=\"font-family:verdana\"> Write to file <b> " + file.getName() + "</b> successful. </p>";
			} catch (IOException e) {
				log += "<p style=\"font-family:verdana\"> Write to file failed. </p>";
				e.printStackTrace();
			}

			// Read from file ------------------------------------------------------------
			try {

				String text = "";

				Scanner fileReader = new Scanner(file);
				while (fileReader.hasNextLine()) {
					String data = fileReader.nextLine();
					text += data + "\n";
				}
				fileReader.close();

				log += "<textarea rows=\"4\" cols=\"50\">" + text + "</textarea><br>";
				log += "<p style=\"font-family:verdana\">  Read from file <b>" + file.getName()
						+ "</b> successful. </p>";

			} catch (FileNotFoundException e) {
				log += "<p style=\"font-family:verdana\"> Read from file failed. </p>";
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return String.format("%s", log);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
