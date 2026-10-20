import java.text.Normalizer;
import java.util.Locale;

/** Las dos operaciones permitidas por el enunciado. */
public enum TipoOperacion {
    DEPOSITO, RETIRO;

    public static TipoOperacion desdeTexto(String texto) {
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
        return switch (normalizado) {
            case "DEPOSITO" -> DEPOSITO;
            case "RETIRO" -> RETIRO;
            default -> throw new IllegalArgumentException(
                    "La operación '" + texto + "' no existe. Escribí DEPOSITO o RETIRO.");
        };
    }
}
