package cl.matiasmunoz.comun;

import org.springframework.http.HttpStatus;

public class ExcepcionNegocio extends RuntimeException {
    private final HttpStatus estado;

    public ExcepcionNegocio(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus obtenerEstado() {
        return estado;
    }
}
