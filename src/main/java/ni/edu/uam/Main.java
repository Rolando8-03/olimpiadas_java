import java.io.EOFException;
import java.io.IOException;

/** Punto de entrada del proyecto para IntelliJ IDEA y Temurin 21. */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--olimpiada")) {
            Olimpiada.main(args);
            return;
        }
        try {
            new AplicacionConsola().ejecutar();
        } catch (EOFException e) {
            System.out.println("\n  Se cerró la entrada. Sesión finalizada.");
        } catch (IOException e) {
            System.out.println("\n  No se pudo leer la consola. Volvé a ejecutar el programa.");
        }
    }
}
