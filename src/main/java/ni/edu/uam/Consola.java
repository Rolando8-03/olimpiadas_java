import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Lectura, validación de formularios y presentación. */
public final class Consola {
    private final BufferedReader entrada = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
    private final NumberFormat numeros = NumberFormat.getIntegerInstance(Locale.US);
    private static final int FILAS_POR_PAGINA = 12;

    public String leer(String mensaje) throws IOException {
        System.out.print("  " + mensaje);
        String linea = entrada.readLine();
        if (linea == null) throw new EOFException("Se cerró la entrada de consola.");
        return linea.trim();
    }

    public long pedirNumero(String mensaje, String campo, long minimo, long maximo)
            throws IOException {
        while (true) {
            try {
                return validarNumero(leer(mensaje), campo, minimo, maximo);
            } catch (IllegalArgumentException e) {
                error(e.getMessage());
            }
        }
    }

    public static long validarNumero(String texto, String campo, long minimo, long maximo) {
        texto = texto.trim();
        if (texto.isEmpty()) throw new IllegalArgumentException("Falta " + campo + ". Completá ese dato.");
        if (texto.startsWith("-")) throw new IllegalArgumentException("No se permiten valores negativos en " + campo + ".");
        if (texto.contains(".") || texto.contains(",")) {
            throw new IllegalArgumentException("Escribí " + campo + " como entero, sin decimales ni separadores.");
        }
        if (!texto.matches("[0-9]+")) {
            throw new IllegalArgumentException("En " + campo + " solo se permiten dígitos, sin letras ni símbolos monetarios.");
        }
        long valor;
        try { valor = Long.parseLong(texto); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("El número en " + campo + " es demasiado grande. El máximo es " + maximo + ".");
        }
        if (valor < minimo) {
            throw new IllegalArgumentException("El mínimo permitido en " + campo + " es " + minimo + ".");
        }
        if (valor > maximo) {
            throw new IllegalArgumentException("El máximo permitido en " + campo + " es " + maximo + ".");
        }
        return valor;
    }

    public String dinero(long importe) { return "C$ " + numeros.format(importe); }
    public String numero(long numero) { return numeros.format(numero); }
    public void titulo(String titulo, String detalle) {
        System.out.println();
        System.out.println("  " + "═".repeat(76));
        System.out.println("  " + titulo);
        System.out.println("  " + detalle);
        System.out.println("  " + "═".repeat(76));
        System.out.println();
    }
    public void error(String mensaje) { System.out.println("\n  [ERROR] " + mensaje + "\n"); }
    public void aviso(String mensaje) { System.out.println("\n  [AVISO] " + mensaje + "\n"); }
    public void exito(String mensaje) { System.out.println("\n  [OK] " + mensaje + "\n"); }
    public void pausa() throws IOException { leer("Presioná Enter para volver al menú... "); }

    public void tabla(String[] encabezados, List<String[]> filas) throws IOException {
        if (filas.isEmpty()) return;
        int[] anchos = new int[encabezados.length];
        for (int i = 0; i < anchos.length; i++) anchos[i] = encabezados[i].length();
        for (String[] fila : filas) {
            for (int i = 0; i < anchos.length; i++) anchos[i] = Math.max(anchos[i], fila[i].length());
        }
        for (int inicio = 0; inicio < filas.size(); inicio += FILAS_POR_PAGINA) {
            borde(anchos, "┌", "┬", "┐");
            fila(encabezados, anchos);
            borde(anchos, "├", "┼", "┤");
            int fin = Math.min(inicio + FILAS_POR_PAGINA, filas.size());
            for (int i = inicio; i < fin; i++) fila(filas.get(i), anchos);
            borde(anchos, "└", "┴", "┘");
            System.out.println("  Registros " + (inicio + 1) + "–" + fin + " de " + filas.size());
            if (fin < filas.size()) {
                while (true) {
                    String opcion = leer("Enter: siguiente página | V: terminar tabla: ");
                    if (opcion.equalsIgnoreCase("V")) return;
                    if (opcion.isEmpty()) break;
                    error("Presioná Enter para avanzar o escribí V para terminar la tabla.");
                }
            }
        }
    }

    private void borde(int[] anchos, String izquierda, String centro, String derecha) {
        StringBuilder salida = new StringBuilder("  " + izquierda);
        for (int i = 0; i < anchos.length; i++) {
            salida.append("─".repeat(anchos[i] + 2));
            salida.append(i == anchos.length - 1 ? derecha : centro);
        }
        System.out.println(salida);
    }
    private void fila(String[] valores, int[] anchos) {
        StringBuilder salida = new StringBuilder("  │");
        for (int i = 0; i < valores.length; i++) {
            salida.append(' ').append(valores[i]);
            salida.append(" ".repeat(anchos[i] - valores[i].length() + 1)).append('│');
        }
        System.out.println(salida);
    }
}
