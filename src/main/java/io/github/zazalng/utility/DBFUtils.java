package worldstandard.group.utility;


import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DBFUtils {
    private DBFUtils() {}

    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank() || text.length() != 8) return null;
        try {
            int y = Integer.parseInt(text.substring(0, 4));
            int m = Integer.parseInt(text.substring(4, 6));
            int d = Integer.parseInt(text.substring(6, 8));
            return LocalDate.of(y, m, d);
        } catch (Exception e) { return null; }
    }

    public static BigDecimal parseNumeric(String text) {
        try {
            BigDecimal bd = new BigDecimal(text.trim());
            return new BigDecimal(bd.toPlainString()); // non-scientific
        } catch (Exception e) { return null; }
    }

    public static BigDecimal parseDoubleBinary(byte[] data) {
        if (data == null || data.length < 8) return null;
        var buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return BigDecimal.valueOf(buf.getDouble());
    }

    public static Integer parseIntBinary(byte[] data) {
        if (data == null || data.length < 4) return null;
        var buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt();
    }

    public static LocalDateTime parseDateTime(byte[] data) {
        if (data == null || data.length < 8) return null;
        var buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        try {
            int days = buf.getInt();
            int millis = buf.getInt();
            return LocalDate.of(1,1,1).plusDays(days - 1721426)
                    .atStartOfDay().plusSeconds(millis / 1000L);
        } catch (Exception e) { return null; }
    }
}