package worldstandard.group.entity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

// Example implementation for a row
public final class DBFRow {
    private final Map<String, Object> values = new LinkedHashMap<>();

    public DBFRow(List<DBFField> fields, ByteBuffer buffer, Charset charset) {
        buffer.position(1); // skip deletion flag
        for (DBFField f : fields) {
            byte[] bytes = new byte[f.length()];
            buffer.get(bytes);
            Object val = f.decode(bytes, charset);
            values.put(f.name(), val);
        }
    }

    public Object get(String name) { return values.get(name); }
    public Map<String, Object> asMap() { return Collections.unmodifiableMap(values); }
    @Override public String toString() { return values.toString(); }
}