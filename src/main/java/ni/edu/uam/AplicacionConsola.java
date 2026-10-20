import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Menú interactivo: todos los datos son ingresados por el usuario. */
public final class AplicacionConsola {
    private final Consola consola = new Consola();
    private Cuenta cuenta;

    public void ejecutar() throws IOException {
        consola.titulo("TRANSFERENCIAS SEGURAS", "Configurá tu cuenta para comenzar");
        System.out.println("  Se trabaja con córdobas enteros, sin centavos.");
        System.out.println("  Los movimientos se procesan en el orden en que los registrás.");
        System.out.println("  Un retiro igual al saldo está permitido.\n");
        long saldo = consola.pedirNumero("Saldo inicial: C$ ", "el saldo inicial", 0, Cuenta.MAX_SALDO_INICIAL);
        int cantidad = (int) consola.pedirNumero("Cantidad de operaciones que vas a registrar: ",
                "la cantidad de operaciones", 1, Cuenta.MAX_OPERACIONES);
        cuenta = new Cuenta(saldo, cantidad);

        while (true) {
            mostrarMenu();
            String opcion = consola.leer("Elegí una opción: ");
            if (opcion.equals("5")) {
                System.out.println("\n  Sesión finalizada. Saldo: " + consola.dinero(cuenta.getSaldo()));
                if (!cuenta.isCompleta()) {
                    System.out.println("  Quedaron " + cuenta.getOperacionesPendientes() + " operaciones sin registrar.");
                }
                System.out.println("  Los datos de esta sesión no se conservan al cerrar.\n");
                return;
            }

            // Se puede usar el menú o escribir, por ejemplo, DEPOSITO seguido del importe.
            String[] comando = opcion.split("\\s+");
            if (comando.length == 2) {
                TipoOperacion tipo;
                try { tipo = TipoOperacion.desdeTexto(comando[0]); }
                catch (IllegalArgumentException e) { consola.error(e.getMessage()); continue; }
                registrar(tipo, comando[1]);
                consola.pausa();
                continue;
            }
            switch (opcion.toUpperCase(Locale.ROOT)) {
                case "1", "DEPOSITO", "DEPÓSITO" -> registrar(TipoOperacion.DEPOSITO, null);
                case "2", "RETIRO" -> registrar(TipoOperacion.RETIRO, null);
                case "3" -> mostrarHistorial();
                case "4" -> mostrarResumen();
                default -> {
                    consola.error(opcion.isEmpty()
                            ? "No elegiste una opción. Escribí un número del 1 al 5."
                            : "La opción '" + opcion + "' no es válida. Elegí del 1 al 5 o escribí DEPOSITO/RETIRO e importe.");
                    continue;
                }
            }
            consola.pausa();
        }
    }

    private void mostrarMenu() {
        consola.titulo("TRANSFERENCIAS SEGURAS", "Tu cuenta · Menú principal");
        System.out.println("  SALDO DISPONIBLE     " + consola.dinero(cuenta.getSaldo()));
        System.out.println("  OPERACIONES          " + cuenta.getCantidadOperaciones() + " de " + cuenta.getOperacionesPrevistas());
        System.out.println("  ESTADO               " + (cuenta.isCompleta() ? "REGISTRO COMPLETO" : "EN PROCESO"));
        System.out.println();
        System.out.println("  1   Registrar depósito");
        System.out.println("  2   Registrar retiro");
        System.out.println("  3   Ver historial de operaciones");
        System.out.println("  4   Ver resumen y análisis");
        System.out.println("  5   Salir");
        System.out.println();
        System.out.println("  También podés escribir DEPOSITO importe o RETIRO importe.");
        System.out.println("  Se aceptan mayúsculas, minúsculas y la palabra depósito con tilde.\n");
    }

    private void registrar(TipoOperacion tipo, String importeIncluido) throws IOException {
        if (cuenta.isCompleta()) {
            consola.aviso("Ya completaste las " + cuenta.getOperacionesPrevistas()
                    + " operaciones. Consultá el historial o el resumen.");
            return;
        }
        consola.titulo(tipo == TipoOperacion.DEPOSITO ? "REGISTRAR DEPÓSITO" : "REGISTRAR RETIRO",
                "Operación " + (cuenta.getCantidadOperaciones() + 1) + " de " + cuenta.getOperacionesPrevistas());
        System.out.println("  Saldo disponible: " + consola.dinero(cuenta.getSaldo()));
        System.out.println("  Importe entre 1 y 1000000. Escribí C para cancelar.\n");
        while (true) {
            String texto = importeIncluido == null ? consola.leer("Importe en córdobas: ") : importeIncluido;
            importeIncluido = null;
            if (texto.equalsIgnoreCase("C")) {
                consola.aviso("Operación cancelada. El saldo y el historial no cambiaron.");
                return;
            }
            try {
                long importe = Consola.validarNumero(texto, "el importe", 1, Cuenta.MAX_IMPORTE);
                Operacion operacion = cuenta.registrar(tipo, importe);
                if (operacion.isAceptada()) {
                    consola.exito((tipo == TipoOperacion.DEPOSITO ? "Depósito" : "Retiro")
                            + " #" + operacion.getNumero() + " aceptado por " + consola.dinero(importe) + ".");
                } else {
                    consola.aviso("Retiro #" + operacion.getNumero() + " rechazado por fondos insuficientes.");
                    System.out.println("  Solicitaste:        " + consola.dinero(importe));
                    System.out.println("  Saldo disponible:   " + consola.dinero(operacion.getSaldoAnterior()));
                    System.out.println("  Dinero faltante:    " + consola.dinero(importe - operacion.getSaldoAnterior()));
                    System.out.println("  El saldo permanece igual. Este intento cuenta como una operación.");
                }
                System.out.println("\n  Saldo anterior: " + consola.dinero(operacion.getSaldoAnterior()));
                System.out.println("  Saldo actual:   " + consola.dinero(operacion.getSaldoPosterior()));
                if (cuenta.isCompleta()) {
                    consola.exito("Completaste todas las operaciones. El resultado final está en la opción 4.");
                }
                System.out.println();
                return;
            } catch (IllegalArgumentException e) {
                consola.error(e.getMessage() + " El dato inválido no cuenta como operación.");
            }
        }
    }

    private void mostrarHistorial() throws IOException {
        consola.titulo("HISTORIAL DE OPERACIONES", "Orden de registro · Incluye retiros rechazados");
        if (cuenta.getCantidadOperaciones() == 0) {
            consola.aviso("Todavía no hay movimientos. Registrá un depósito o un retiro.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Operacion op : cuenta.getHistorial()) {
            filas.add(new String[]{String.valueOf(op.getNumero()), op.getTipo().name(),
                    consola.dinero(op.getImporte()), consola.dinero(op.getSaldoAnterior()),
                    consola.dinero(op.getSaldoPosterior()), op.isAceptada() ? "ACEPTADO" : "RECHAZADO"});
        }
        consola.tabla(new String[]{"N.º", "OPERACIÓN", "IMPORTE", "SALDO ANTERIOR", "SALDO POSTERIOR", "ESTADO"}, filas);
        System.out.println();
    }

    private void mostrarResumen() throws IOException {
        consola.titulo("RESUMEN DE LA CUENTA", cuenta.isCompleta() ? "Resultado final" : "Resumen parcial · Aún faltan operaciones");
        List<String[]> resumen = List.of(
                new String[]{"Saldo inicial", consola.dinero(cuenta.getSaldoInicial())},
                new String[]{"Depósitos recibidos", consola.dinero(cuenta.getTotalDepositos())},
                new String[]{"Retiros realizados", consola.dinero(cuenta.getTotalRetiros())},
                new String[]{"SALDO ACTUAL", consola.dinero(cuenta.getSaldo())}
        );
        consola.tabla(new String[]{"CONCEPTO", "IMPORTE"}, resumen);

        System.out.println("\n  CONTROL DE OPERACIONES\n");
        List<String[]> control = List.of(
                new String[]{"Operaciones previstas", consola.numero(cuenta.getOperacionesPrevistas())},
                new String[]{"Operaciones registradas", consola.numero(cuenta.getCantidadOperaciones())},
                new String[]{"Operaciones pendientes", consola.numero(cuenta.getOperacionesPendientes())},
                new String[]{"Depósitos aceptados", consola.numero(cuenta.getCantidadDepositos())},
                new String[]{"Retiros aceptados", consola.numero(cuenta.getRetirosAceptados())},
                new String[]{"Retiros rechazados", consola.numero(cuenta.getRetirosRechazados())}
        );
        consola.tabla(new String[]{"CONCEPTO", "CANTIDAD"}, control);

        System.out.println("\n  ANÁLISIS DE TUS MOVIMIENTOS\n");
        System.out.println("  Saldo inicial + depósitos − retiros aceptados = saldo actual");
        System.out.println("  " + consola.dinero(cuenta.getSaldoInicial()) + " + "
                + consola.dinero(cuenta.getTotalDepositos()) + " − " + consola.dinero(cuenta.getTotalRetiros())
                + " = " + consola.dinero(cuenta.getSaldo()));
        if (cuenta.getCantidadOperaciones() == 0) {
            System.out.println("\n  Aún no hay movimientos para analizar.");
        } else {
            long diferencia = cuenta.getSaldo() - cuenta.getSaldoInicial();
            System.out.println("\n  " + (diferencia > 0
                    ? "El saldo aumentó " + consola.dinero(diferencia) + " respecto al inicio."
                    : diferencia < 0 ? "El saldo disminuyó " + consola.dinero(-diferencia) + " respecto al inicio."
                    : "El saldo actual es igual al saldo inicial."));
            int intentosRetiro = cuenta.getRetirosAceptados() + cuenta.getRetirosRechazados();
            if (intentosRetiro == 0) {
                System.out.println("  No registraste intentos de retiro.");
            } else if (cuenta.getRetirosRechazados() == 0) {
                System.out.println("  Todos los retiros se aceptaron: hubo fondos para cada uno.");
            } else {
                System.out.printf(Locale.ROOT, "  Se rechazaron %d de %d retiros (%.2f %%).%n",
                        cuenta.getRetirosRechazados(), intentosRetiro,
                        cuenta.getRetirosRechazados() * 100.0 / intentosRetiro);
                System.out.println("  Los importes rechazados suman " + consola.dinero(cuenta.getImporteRechazado()) + ".");
                System.out.println("  Esos intentos no descontaron dinero y no se reintentan automáticamente.");
            }
            if (cuenta.getSaldo() == 0) System.out.println("  La cuenta quedó sin saldo disponible.");
        }
        if (cuenta.isCompleta()) {
            consola.titulo("SALIDA DEL EJERCICIO", "Formato exacto del enunciado");
            System.out.println(cuenta.resultadoOlimpiada());
            System.out.println();
        } else {
            consola.aviso("Faltan " + cuenta.getOperacionesPendientes()
                    + " operaciones. Este resumen todavía no es el resultado final.");
        }
    }
}
