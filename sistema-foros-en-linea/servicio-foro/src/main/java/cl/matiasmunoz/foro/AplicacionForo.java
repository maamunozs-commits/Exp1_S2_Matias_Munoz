package cl.matiasmunoz.foro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cl.matiasmunoz")
public class AplicacionForo {
    public static void main(String[] argumentos) {
        SpringApplication.run(AplicacionForo.class, argumentos);
    }
}
