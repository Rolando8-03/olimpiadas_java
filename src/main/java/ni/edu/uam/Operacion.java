/** Registro inmutable de un intento válido, incluso si no hubo fondos para retirarlo. */
public final class Operacion {
    private final int numero;
    private final TipoOperacion tipo;
    private final long importe;
    private final long saldoAnterior;
    private final long saldoPosterior;
    private final boolean aceptada;

    public Operacion(int numero, TipoOperacion tipo, long importe,
                     long saldoAnterior, long saldoPosterior, boolean aceptada) {
        this.numero = numero;
        this.tipo = tipo;
        this.importe = importe;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.aceptada = aceptada;
    }

    public int getNumero() { return numero; }
    public TipoOperacion getTipo() { return tipo; }
    public long getImporte() { return importe; }
    public long getSaldoAnterior() { return saldoAnterior; }
    public long getSaldoPosterior() { return saldoPosterior; }
    public boolean isAceptada() { return aceptada; }
}
