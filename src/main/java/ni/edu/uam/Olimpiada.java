import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Entrada estándar y salida exacta del enunciado, sin menús ni datos precargados. */
public final class Olimpiada {
    public static void main(String[] args) {
        try {
            resolver(System.in, System.out);
        } catch (IOException | IllegalArgumentException e) {
            // Los mensajes de error nunca se mezclan con la salida evaluada.
            System.err.println("Entrada inválida: " + e.getMessage());
        }
    }

    public static Cuenta resolver(InputStream entrada, PrintStream salida) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        String[] cabecera = leerDatos(lector, "la primera línea (saldo inicial y cantidad de operaciones)");
        long saldo = entero(cabecera[0], "el saldo inicial");
        long cantidad = entero(cabecera[1], "la cantidad de operaciones");
        if (cantidad < 1 || cantidad > Cuenta.MAX_OPERACIONES) {
            throw new IllegalArgumentException("La cantidad de operaciones debe estar entre 1 y 100000.");
        }
        Cuenta cuenta = new Cuenta(saldo, (int) cantidad);
        for (int i = 1; i <= cantidad; i++) {
            String[] datos = leerDatos(lector, "la operación " + i + " (DEPOSITO/RETIRO e importe)");
            try {
                cuenta.registrar(TipoOperacion.desdeTexto(datos[0]), entero(datos[1], "el importe"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Operación " + i + ": " + e.getMessage());
            }
        }
        salida.println(cuenta.resultadoOlimpiada());
        return cuenta;
    }

    private static String[] leerDatos(BufferedReader lector, String contexto) throws IOException {
        String linea = lector.readLine();
        if (linea == null) throw new IllegalArgumentException("Falta " + contexto + ".");
        String[] datos = linea.trim().split("\\s+");
        if (datos.length != 2) {
            throw new IllegalArgumentException("Se necesitan exactamente dos datos en " + contexto + ".");
        }
        return datos;
    }

    private static long entero(String texto, String campo) {
        if (!texto.matches("[0-9]+")) throw new IllegalArgumentException("Usá un entero sin signo ni separadores en " + campo + ".");
        try { return Long.parseLong(texto); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("El número en " + campo + " es demasiado grande.");
        }
    }
}
