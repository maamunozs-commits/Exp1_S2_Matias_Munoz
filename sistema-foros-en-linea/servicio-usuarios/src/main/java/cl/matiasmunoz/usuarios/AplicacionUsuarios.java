package cl.matiasmunoz.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cl.matiasmunoz")
public class AplicacionUsuarios {
    public static void main(String[] argumentos) {
        SpringApplication.run(AplicacionUsuarios.class, argumentos);
    }
}
