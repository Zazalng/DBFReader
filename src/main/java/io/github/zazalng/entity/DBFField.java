package worldstandard.group.entity;

import worldstandard.group.contracts.DBFDataType;
import worldstandard.group.utility.DBFUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record DBFField(
        String name,
        DBFDataType type,
        int length,
        int decimalCount
) {

    public static DBFField read(ByteBuffer buf, Charset charset) {
        byte[] nameBytes = new byte[11];
        buf.get(nameBytes);
        String name = new String(nameBytes, charset).trim();
        char typeChar = (char) buf.get();
        buf.position(buf.position() + 4); // skip field data address
        int length = Byte.toUnsignedInt(buf.get());
        int decimalCount = Byte.toUnsignedInt(buf.get());
        buf.position(buf.position() + 14); // skip reserved

        DBFDataType type = DBFDataType.fromCode(typeChar);
        return new DBFField(name, type, length, decimalCount);
    }

    public Object decode(byte[] data, Charset charset) {
        String raw = new String(data, charset).trim();
        if (raw.isEmpty()) return null;

        return switch (type) {
            case CHARACTER, VARCHAR -> raw;
            case DATE -> DBFUtils.parseDate(raw);
            case NUMERIC, FLOAT -> DBFUtils.parseNumeric(raw);
            case DOUBLE, CURRENCY -> DBFUtils.parseDoubleBinary(data);
            case INTEGER -> DBFUtils.parseIntBinary(data);
            case LOGICAL -> switch (raw.toUpperCase()) {
                case "T", "Y" -> true;
                case "F", "N" -> false;
                default -> null;
            };
            case DATETIME, TIMESTAMP -> DBFUtils.parseDateTime(data);
            default -> data;
        };
    }
}