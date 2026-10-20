package ni.edu.uam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Aplicación de consola. Compatible con Temurin 21. Sin dependencias externas. */
public class Main {
    private static final BufferedReader ENTRADA = new BufferedReader(new InputStreamReader(System.in));
    private static final Sistema SISTEMA = new Sistema();
    private static final NumberFormat NUMEROS = NumberFormat.getIntegerInstance(Locale.US);
    private static final int FILAS_POR_PAGINA = 15;

    public static void main(String[] args) {
        try {
            while (true) {
                cabecera("VENTAS POR SUCURSAL", "Administración de sucursales e ingresos");
                System.out.println("  1   Agregar sucursal");
                System.out.println("  2   Ver sucursales");
                System.out.println("  3   Registrar ventas");
                System.out.println("  4   Ver ventas");
                System.out.println("  5   Ver resumen y análisis");
                System.out.println("  6   Salir");
                System.out.println();
                System.out.println("  " + SISTEMA.sucursales.size() + " sucursales  ·  "
                        + SISTEMA.ventas.size() + " ventas  ·  " + dinero(SISTEMA.total));
                linea();

                String opcion = leer("Elegí una opción: ");
                if (opcion.equals("6")) {
                    System.out.println("\n  Sesión finalizada. Gracias por usar el sistema.");
                    System.out.println("  Los datos de esta sesión no se guardan al cerrar.\n");
                    return;
                }
                switch (opcion) {
                    case "1" -> agregarSucursal();
                    case "2" -> verSucursales();
                    case "3" -> registrarVenta();
                    case "4" -> verVentas();
                    case "5" -> verResumen();
                    default -> {
                        error(opcion.isEmpty()
                                ? "No elegiste una opción. Escribí un número del 1 al 6."
                                : "La opción '" + opcion + "' no existe. Elegí del 1 al 6.");
                        continue;
                    }
                }
                leer("Presioná Enter para volver al menú... ");
            }
        } catch (FinEntrada e) {
            System.out.println("\n  La entrada se cerró. Sesión finalizada.");
        } catch (IOException e) {
            System.out.println("\n  No se pudo leer la consola. Volvé a ejecutar el programa.");
        }
    }

    private static void agregarSucursal() throws IOException {
        cabecera("AGREGAR SUCURSAL", "Escribí 0 para cancelar");
        System.out.println("  Solo letras A-Z, sin espacios ni tildes.");
        System.out.println("  Mayúsculas y minúsculas identifican la misma sucursal.\n");
        while (true) {
            String nombre = leer("Nombre de la sucursal: ");
            if (nombre.equals("0")) return;
            try {
                Sucursal nueva = SISTEMA.agregar(nombre);
                exito("Sucursal " + nueva.nombre + " agregada.");
                return;
            } catch (IllegalArgumentException e) {
                error(e.getMessage());
            }
        }
    }

    private static void verSucursales() throws IOException {
        cabecera("SUCURSALES REGISTRADAS", "Nombres unificados en mayúsculas");
        if (SISTEMA.sucursales.isEmpty()) {
            aviso("Todavía no hay sucursales. Agregá una desde la opción 1.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        SISTEMA.sucursales.values().stream()
                .sorted(Comparator.comparing(s -> s.nombre))
                .forEach(s -> filas.add(new String[]{s.nombre, entero(s.cantidad),
                        dinero(s.total), s.cantidad == 0 ? "Sin ventas" : "Con ventas"}));
        tabla(new String[]{"SUCURSAL", "VENTAS", "INGRESO", "ESTADO"}, filas);
    }

    private static void registrarVenta() throws IOException {
        cabecera("REGISTRAR VENTAS", "Escribí 0 en el nombre de sucursal para cancelar");
        if (SISTEMA.sucursales.isEmpty()) {
            aviso("Primero agregá una sucursal desde la opción 1.");
            return;
        }
        while (true) {
            if (SISTEMA.ventas.size() >= Sistema.MAX_VENTAS) {
                aviso("Alcanzaste el límite de 100000 ventas por sesión.");
                return;
            }
            Sucursal sucursal;
            while (true) {
                String nombre = leer("Sucursal de la venta: ");
                if (nombre.equals("0")) return;
                try {
                    sucursal = SISTEMA.buscar(nombre);
                    break;
                } catch (IllegalArgumentException e) {
                    error(e.getMessage());
                }
            }
            System.out.println("  Sucursal seleccionada: " + sucursal.nombre);
            System.out.println("  Importe entero entre 1 y 1000000. Escribí C para cancelar.");
            while (true) {
                String texto = leer("Importe en córdobas: ");
                if (texto.equalsIgnoreCase("C")) return;
                try {
                    long importe = validarImporte(texto);
                    Venta venta = SISTEMA.registrar(sucursal.nombre, importe);
                    exito("Venta #" + venta.numero + " registrada en " + sucursal.nombre
                            + " por " + dinero(importe) + ".");
                    break;
                } catch (IllegalArgumentException e) {
                    error(e.getMessage());
                }
            }
            if (!preguntarOtraVenta()) return;
            System.out.println();
        }
    }

    private static boolean preguntarOtraVenta() throws IOException {
        while (true) {
            String respuesta = leer("¿Registrar otra venta? [S/N]: ");
            if (respuesta.equalsIgnoreCase("S")) return true;
            if (respuesta.equalsIgnoreCase("N")) return false;
            error("Respondé S para registrar otra venta o N para volver.");
        }
    }

    private static void verVentas() throws IOException {
        cabecera("VENTAS REGISTRADAS", "Historial en orden de registro");
        if (SISTEMA.ventas.isEmpty()) {
            aviso("Todavía no registraste ventas. Usá la opción 3.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Venta venta : SISTEMA.ventas) {
            filas.add(new String[]{entero(venta.numero), venta.sucursal, dinero(venta.importe)});
        }
        tabla(new String[]{"N.º", "SUCURSAL", "IMPORTE"}, filas);
        System.out.println("\n  Total de ventas: " + entero(SISTEMA.ventas.size()));
        System.out.println("  Ingreso acumulado: " + dinero(SISTEMA.total) + "\n");
    }

    private static void verResumen() throws IOException {
        cabecera("RESUMEN GENERAL", "Resultados calculados con tus datos");
        long activas = SISTEMA.sucursales.values().stream().filter(s -> s.cantidad > 0).count();
        System.out.println("  Sucursales registradas     " + entero(SISTEMA.sucursales.size()));
        System.out.println("  Sucursales con ventas      " + entero(activas));
        System.out.println("  Sucursales sin ventas      " + entero(SISTEMA.sucursales.size() - activas));
        System.out.println("  Ventas registradas         " + entero(SISTEMA.ventas.size()));
        System.out.println("  INGRESO GENERAL            " + dinero(SISTEMA.total));
        if (SISTEMA.ventas.isEmpty()) {
            aviso("Aún no hay ventas para analizar. Todavía no existe una sucursal ganadora.");
            return;
        }
        System.out.printf(Locale.ROOT, "  Promedio por venta         C$ %,.2f%n",
                (double) SISTEMA.total / SISTEMA.ventas.size());

        List<Sucursal> ranking = SISTEMA.clasificacion();
        System.out.println("\n  CLASIFICACIÓN POR INGRESOS\n");
        List<String[]> filas = new ArrayList<>();
        int posicion = 0;
        for (Sucursal s : ranking) {
            filas.add(new String[]{String.valueOf(++posicion), s.nombre, entero(s.cantidad),
                    dinero(s.total), porcentaje(s.total, SISTEMA.total)});
        }
        tabla(new String[]{"ORDEN", "SUCURSAL", "VENTAS", "INGRESO", "% DEL TOTAL"}, filas);

        Sucursal ganadora = ranking.get(0);
        List<Sucursal> empatadas = ranking.stream().filter(s -> s.total == ganadora.total).toList();
        cabecera("SUCURSAL GANADORA", ganadora.nombre);
        System.out.println("  Ingreso obtenido:     " + dinero(ganadora.total));
        System.out.println("  Ventas realizadas:    " + entero(ganadora.cantidad));
        System.out.println("  Aporte a la empresa:  " + porcentaje(ganadora.total, SISTEMA.total));
        System.out.println("\n  ANÁLISIS");
        if (empatadas.size() > 1) {
            System.out.println("  " + empatadas.size() + " sucursales comparten el mayor ingreso.");
            System.out.println("  Todas acumulan " + dinero(ganadora.total) + ":");
            for (Sucursal s : empatadas) System.out.println("    • " + s.nombre);
            System.out.println("  Gana " + ganadora.nombre + " según String.compareTo().");
            System.out.println("  La comparación usa los nombres unificados en mayúsculas.");
        } else if (activas == 1) {
            System.out.println("  " + ganadora.nombre + " es la única sucursal con ventas registradas.");
        } else {
            Sucursal segunda = ranking.get(1);
            System.out.println("  " + ganadora.nombre + " obtuvo el mayor ingreso sin empate.");
            System.out.println("  Supera a " + segunda.nombre + " por "
                    + dinero(ganadora.total - segunda.total) + ".");
        }
        System.out.println("\n  Resultado en el formato del enunciado:");
        System.out.println("  " + ganadora.nombre + " " + ganadora.total + "\n");
    }

    // La presentación no modifica los valores: los ingresos siempre se guardan en long.
    private static String entero(long valor) { return NUMEROS.format(valor); }
    private static String dinero(long valor) { return "C$ " + entero(valor); }
    private static String porcentaje(long parte, long total) {
        return String.format(Locale.ROOT, "%.2f %%", parte * 100.0 / total);
    }
    private static void linea() { System.out.println("  " + "─".repeat(70)); }
    private static void cabecera(String titulo, String subtitulo) {
        System.out.println();
        System.out.println("  " + "═".repeat(70));
        System.out.println("  " + titulo);
        System.out.println("  " + subtitulo);
        System.out.println("  " + "═".repeat(70));
        System.out.println();
    }
    private static void error(String mensaje) { System.out.println("\n  [ERROR] " + mensaje + "\n"); }
    private static void aviso(String mensaje) { System.out.println("\n  [AVISO] " + mensaje + "\n"); }
    private static void exito(String mensaje) { System.out.println("\n  [OK] " + mensaje + "\n"); }
    private static String leer(String mensaje) throws IOException {
        System.out.print("  " + mensaje);
        String texto = ENTRADA.readLine();
        if (texto == null) throw new FinEntrada();
        return texto.trim();
    }

    private static void tabla(String[] titulos, List<String[]> filas) throws IOException {
        int[] anchos = new int[titulos.length];
        for (int i = 0; i < anchos.length; i++) anchos[i] = titulos[i].length();
        for (String[] fila : filas) {
            for (int i = 0; i < anchos.length; i++) anchos[i] = Math.max(anchos[i], fila[i].length());
        }
        for (int inicio = 0; inicio < filas.size(); inicio += FILAS_POR_PAGINA) {
            borde(anchos, "┌", "┬", "┐");
            fila(titulos, anchos);
            borde(anchos, "├", "┼", "┤");
            int fin = Math.min(inicio + FILAS_POR_PAGINA, filas.size());
            for (int i = inicio; i < fin; i++) fila(filas.get(i), anchos);
            borde(anchos, "└", "┴", "┘");
            System.out.println("  Registros " + (inicio + 1) + "–" + fin + " de " + filas.size());
            if (fin < filas.size()) {
                while (true) {
                    String respuesta = leer("Enter: siguiente página | V: terminar tabla: ");
                    if (respuesta.equalsIgnoreCase("V")) return;
                    if (respuesta.isEmpty()) break;
                    error("Presioná Enter para avanzar o escribí V para terminar la tabla.");
                }
            }
        }
    }
    private static void borde(int[] anchos, String izquierda, String centro, String derecha) {
        StringBuilder texto = new StringBuilder("  " + izquierda);
        for (int i = 0; i < anchos.length; i++) {
            texto.append("─".repeat(anchos[i] + 2)).append(i == anchos.length - 1 ? derecha : centro);
        }
        System.out.println(texto);
    }
    private static void fila(String[] valores, int[] anchos) {
        StringBuilder texto = new StringBuilder("  │");
        for (int i = 0; i < valores.length; i++) {
            texto.append(' ').append(valores[i]).append(" ".repeat(anchos[i] - valores[i].length() + 1)).append('│');
        }
        System.out.println(texto);
    }

    static long validarImporte(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) throw new IllegalArgumentException("Falta el importe. Escribí cuántos córdobas se vendieron.");
        if (texto.startsWith("-")) throw new IllegalArgumentException("Una venta no puede tener un importe negativo.");
        if (texto.contains(".") || texto.contains(",")) throw new IllegalArgumentException("Usá córdobas enteros, sin decimales ni separadores.");
        if (!texto.matches("[0-9]+")) throw new IllegalArgumentException("El importe solo admite dígitos. No escribás letras ni C$.");
        long importe;
        try { importe = Long.parseLong(texto); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("El importe es demasiado grande. El máximo es C$ 1,000,000."); }
        if (importe == 0) throw new IllegalArgumentException("El importe no puede ser cero. El mínimo es C$ 1.");
        if (importe > 1_000_000L) throw new IllegalArgumentException("Una venta no puede superar C$ 1,000,000.");
        return importe;
    }

    static class Sucursal {
        final String nombre;
        long total;
        int cantidad;
        Sucursal(String nombre) { this.nombre = nombre; }
    }
    static class Venta {
        final int numero;
        final String sucursal;
        final long importe;
        Venta(int numero, String sucursal, long importe) {
            this.numero = numero;
            this.sucursal = sucursal;
            this.importe = importe;
        }
    }

    // Lógica del negocio, separada de los mensajes y de la lectura de consola.
    static class Sistema {
        static final int MAX_VENTAS = 100_000;
        final Map<String, Sucursal> sucursales = new LinkedHashMap<>();
        final List<Venta> ventas = new ArrayList<>();
        long total;

        static String normalizar(String nombre) {
            nombre = nombre.trim();
            if (nombre.isEmpty()) throw new IllegalArgumentException("Falta el nombre de la sucursal.");
            if (!nombre.matches("[A-Za-z]+")) throw new IllegalArgumentException("El nombre debe contener solo letras A-Z, sin espacios, tildes, ñ ni números.");
            return nombre.toUpperCase(Locale.ROOT);
        }
        Sucursal agregar(String nombre) {
            String clave = normalizar(nombre);
            if (sucursales.containsKey(clave)) throw new IllegalArgumentException("La sucursal " + clave + " ya existe. Cambiar mayúsculas no crea una sucursal diferente.");
            Sucursal sucursal = new Sucursal(clave);
            sucursales.put(clave, sucursal);
            return sucursal;
        }
        Sucursal buscar(String nombre) {
            String clave = normalizar(nombre);
            Sucursal sucursal = sucursales.get(clave);
            if (sucursal == null) throw new IllegalArgumentException("La sucursal " + clave + " no está registrada. Revisá el nombre o agregala en la opción 1.");
            return sucursal;
        }
        Venta registrar(String nombre, long importe) {
            Sucursal sucursal = buscar(nombre);
            if (importe < 1 || importe > 1_000_000L) throw new IllegalArgumentException("El importe debe estar entre C$ 1 y C$ 1,000,000.");
            if (ventas.size() >= MAX_VENTAS) throw new IllegalArgumentException("Se alcanzó el límite de 100000 ventas por sesión.");
            Venta venta = new Venta(ventas.size() + 1, sucursal.nombre, importe);
            ventas.add(venta);
            sucursal.total += importe;
            sucursal.cantidad++;
            total += importe;
            return venta;
        }
        List<Sucursal> clasificacion() {
            List<Sucursal> lista = new ArrayList<>(sucursales.values());
            lista.sort((a, b) -> {
                int comparacion = Long.compare(b.total, a.total);
                return comparacion != 0 ? comparacion : a.nombre.compareTo(b.nombre);
            });
            return lista;
        }
    }
    private static class FinEntrada extends IOException { }
}
