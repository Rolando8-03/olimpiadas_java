import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reglas del ejercicio. No lee ni imprime datos de consola. */
public final class Cuenta {
    public static final long MAX_SALDO_INICIAL = 1_000_000_000L;
    public static final long MAX_IMPORTE = 1_000_000L;
    public static final int MAX_OPERACIONES = 100_000;

    private final long saldoInicial;
    private final int operacionesPrevistas;
    private final List<Operacion> operaciones = new ArrayList<>();
    private final List<Operacion> historial = Collections.unmodifiableList(operaciones);
    private long saldo;
    private long totalDepositos;
    private long totalRetiros;
    private long importeRechazado;
    private int cantidadDepositos;
    private int retirosAceptados;
    private int retirosRechazados;

    public Cuenta(long saldoInicial, int operacionesPrevistas) {
        if (saldoInicial < 0 || saldoInicial > MAX_SALDO_INICIAL) {
            throw new IllegalArgumentException("El saldo inicial debe estar entre 0 y 1000000000.");
        }
        if (operacionesPrevistas < 1 || operacionesPrevistas > MAX_OPERACIONES) {
            throw new IllegalArgumentException("La cantidad de operaciones debe estar entre 1 y 100000.");
        }
        this.saldoInicial = saldoInicial;
        this.saldo = saldoInicial;
        this.operacionesPrevistas = operacionesPrevistas;
    }

    public Operacion registrar(TipoOperacion tipo, long importe) {
        if (tipo == null) throw new IllegalArgumentException("Debés indicar DEPOSITO o RETIRO.");
        if (importe < 1 || importe > MAX_IMPORTE) {
            throw new IllegalArgumentException("El importe debe estar entre 1 y 1000000 córdobas enteros.");
        }
        if (isCompleta()) {
            throw new IllegalArgumentException("Ya registraste las " + operacionesPrevistas
                    + " operaciones previstas. Podés consultar el historial o el resumen.");
        }
        long anterior = saldo;
        boolean aceptada = true;
        if (tipo == TipoOperacion.DEPOSITO) {
            saldo += importe;
            totalDepositos += importe;
            cantidadDepositos++;
        } else if (importe <= saldo) {
            // La igualdad se acepta: retirar exactamente el saldo deja la cuenta en cero.
            saldo -= importe;
            totalRetiros += importe;
            retirosAceptados++;
        } else {
            aceptada = false;
            retirosRechazados++;
            importeRechazado += importe;
        }
        Operacion operacion = new Operacion(operaciones.size() + 1, tipo, importe,
                anterior, saldo, aceptada);
        operaciones.add(operacion);
        return operacion;
    }

    public long getSaldoInicial() { return saldoInicial; }
    public long getSaldo() { return saldo; }
    public long getTotalDepositos() { return totalDepositos; }
    public long getTotalRetiros() { return totalRetiros; }
    public long getImporteRechazado() { return importeRechazado; }
    public int getCantidadDepositos() { return cantidadDepositos; }
    public int getRetirosAceptados() { return retirosAceptados; }
    public int getRetirosRechazados() { return retirosRechazados; }
    public int getCantidadOperaciones() { return operaciones.size(); }
    public int getOperacionesPrevistas() { return operacionesPrevistas; }
    public int getOperacionesPendientes() { return operacionesPrevistas - operaciones.size(); }
    public boolean isCompleta() { return operaciones.size() == operacionesPrevistas; }
    public List<Operacion> getHistorial() { return historial; }

    public String resultadoOlimpiada() {
        if (!isCompleta()) {
            throw new IllegalStateException("Faltan operaciones; todavía no hay un resultado final.");
        }
        return "SALDO " + saldo + "\nRECHAZADOS " + retirosRechazados;
    }
}
