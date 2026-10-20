package ni.edu.uam;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);

        mostrarTitulo("SISTEMA DE VENTAS POR SUCURSAL");

        System.out.println("Registrá las ventas de tu empresa.");
        System.out.println("El sistema calculará el ingreso de cada sucursal");
        System.out.println("e identificará cuál obtuvo el mayor total.");

        int cantidadVentas = leerEntero(
                entrada,
                "\n¿Cuántas ventas vas a registrar? ",
                1,
                100_000
        );

        // Guarda el nombre de cada sucursal y su ingreso acumulado.
        Map<String, Long> totales = new TreeMap<>();

        // Guarda cuántas ventas tiene cada sucursal.
        Map<String, Integer> cantidades = new TreeMap<>();

        long ingresoGeneral = 0L;

        System.out.println("\nIngresá cada venta con este formato: sucursal importe");
        System.out.println("El importe se escribe sin C$, comas ni decimales.");
        System.out.println("Los nombres solo admiten letras de A-Z y a-z.");
        System.out.println("Usá siempre la misma escritura para una misma sucursal.");
        System.out.println("Mayúsculas y minúsculas se consideran diferentes.");

        for (int i = 1; i <= cantidadVentas; i++) {

            while (true) {
                System.out.printf(
                        "%nVenta %d de %d → ",
                        i,
                        cantidadVentas
                );

                String linea = entrada.nextLine().trim();
                String[] datos = linea.split("\\s+");

                if (datos.length != 2) {
                    System.out.println(
                            "Ingresá el nombre y el importe separados por un espacio."
                    );
                    continue;
                }

                String sucursal = datos[0];

                if (!sucursal.matches("[A-Za-z]+")) {
                    System.out.println(
                            "Nombre inválido. Usá solo letras, sin espacios, "
                                    + "tildes, ñ, números ni símbolos."
                    );
                    continue;
                }

                long importe;

                try {
                    importe = Long.parseLong(datos[1]);
                } catch (NumberFormatException e) {
                    System.out.println(
                            "Importe inválido. Escribí un número entero."
                    );
                    continue;
                }

                if (importe < 1 || importe > 1_000_000) {
                    System.out.println(
                            "El importe debe estar entre 1 y 1000000 córdobas."
                    );
                    continue;
                }

                long nuevoTotal = totales.getOrDefault(sucursal, 0L) + importe;

                totales.put(sucursal, nuevoTotal);

                cantidades.put(
                        sucursal,
                        cantidades.getOrDefault(sucursal, 0) + 1
                );

                ingresoGeneral += importe;

                System.out.println(
                        "Registrada | " + sucursal
                                + " | Acumulado: C$ " + nuevoTotal
                );

                break;
            }
        }

        // Busca el total más alto y aplica el desempate.
        String ganadora = null;
        long mayorTotal = 0L;

        for (Map.Entry<String, Long> registro : totales.entrySet()) {
            String nombre = registro.getKey();
            long total = registro.getValue();

            if (ganadora == null
                    || total > mayorTotal
                    || (total == mayorTotal
                    && nombre.compareTo(ganadora) < 0)) {

                ganadora = nombre;
                mayorTotal = total;
            }
        }

        mostrarTitulo("RESUMEN DE VENTAS");

        // Ajusta el ancho de la tabla a los nombres ingresados.
        int anchoNombre = "SUCURSAL".length();

        for (String nombre : totales.keySet()) {
            anchoNombre = Math.max(anchoNombre, nombre.length());
        }

        String formato = "%-" + anchoNombre + "s | %8s | %18s%n";
        String separador = "-".repeat(anchoNombre + 32);

        System.out.printf(
                formato,
                "SUCURSAL",
                "VENTAS",
                "INGRESO (C$)"
        );

        System.out.println(separador);

        for (Map.Entry<String, Long> registro : totales.entrySet()) {
            System.out.printf(
                    formato,
                    registro.getKey(),
                    cantidades.get(registro.getKey()),
                    registro.getValue()
            );
        }

        System.out.println(separador);
        System.out.println("Ventas registradas: " + cantidadVentas);
        System.out.println("Sucursales diferentes: " + totales.size());
        System.out.println("Ingreso general: C$ " + ingresoGeneral);

        int cantidadEmpatadas = 0;

        for (long total : totales.values()) {
            if (total == mayorTotal) {
                cantidadEmpatadas++;
            }
        }

        mostrarTitulo("RESULTADO DEL ANÁLISIS");

        if (cantidadEmpatadas > 1) {
            System.out.println(
                    "Hay " + cantidadEmpatadas
                            + " sucursales empatadas con C$ " + mayorTotal + ":"
            );

            for (Map.Entry<String, Long> registro : totales.entrySet()) {
                if (registro.getValue() == mayorTotal) {
                    System.out.println("  • " + registro.getKey());
                }
            }

            System.out.println(
                    "\nSe aplica el desempate con String.compareTo()."
            );
            System.out.println(
                    "Gana el nombre menor según ese orden, "
                            + "que distingue mayúsculas de minúsculas."
            );
        } else {
            System.out.println("Se encontró una única sucursal con el mayor ingreso.");
        }

        System.out.println("\nSucursal ganadora: " + ganadora);
        System.out.println("Ingreso acumulado: C$ " + mayorTotal);

        mostrarTitulo("SALIDA SOLICITADA");
        System.out.println(ganadora + " " + mayorTotal);

        entrada.close();
    }

    private static int leerEntero(
            Scanner entrada,
            String mensaje,
            int minimo,
            int maximo
    ) {
        while (true) {
            System.out.print(mensaje);

            try {
                int numero = Integer.parseInt(entrada.nextLine().trim());

                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }

                System.out.println(
                        "Ingresá un número entre " + minimo + " y " + maximo + "."
                );
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número entero válido.");
            }
        }
    }

    private static void mostrarTitulo(String titulo) {
        System.out.println();
        System.out.println("═".repeat(60));
        System.out.println("  " + titulo);
        System.out.println("═".repeat(60));
    }
}