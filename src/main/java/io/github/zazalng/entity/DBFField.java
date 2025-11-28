/*
 * Copyright 2025 Napapon Kamanee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zazalng.entity;

import io.github.zazalng.utility.DBFUtils;
import io.github.zazalng.contracts.DBFDataType;

import java.nio.ByteBuffer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single field (column) descriptor from the DBF file header.
 *
 * <p>This final class holds the metadata required to interpret data stored in the records,
 * including the field's name, data type, length, and decimal precision. This structure is
 * compatible with older Java Development Kits (e.g., JDK 8).</p>
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache-2.0 license</a>
 */
public final class DBFField{
    private final String name;
    private final DBFDataType type;
    private final int length;
    private final int decimalCount;

    private DBFField(String name, DBFDataType type, int length, int decimalCount) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.decimalCount = decimalCount;
    }

    /**
     * Encodes a Java value into the raw byte representation expected for this field according to the
     * DBF specification. The output length always matches {@link #getLength()}.
     *
     * @param value   The value to encode.
     * @param charset Charset used for textual values. When {@code null}, {@link StandardCharsets#US_ASCII} is used.
     * @return Byte array representing the encoded value, padded or truncated as required.
     * @throws IllegalArgumentException If the value type is incompatible with the DBF field type.
     */
    public byte[] encode(Object value, Charset charset) {
        Charset effectiveCharset = charset != null ? charset : StandardCharsets.US_ASCII;
        byte[] out = new byte[length];

        if (value == null) {
            return out;
        }

        switch (type) {
            case CHARACTER:
            case VARCHAR:
                writeString(value, effectiveCharset, out);
                break;
            case DATE:
                writeDate(value, out);
                break;
            case NUMERIC:
            case FLOAT:
                writeNumeric(value, out);
                break;
            case DOUBLE:
            case CURRENCY:
                writeBinaryDouble(value, out);
                break;
            case INTEGER:
                writeBinaryInteger(value, out);
                break;
            case LOGICAL:
                writeLogical(value, out);
                break;
            case DATETIME:
            case TIMESTAMP:
                writeDateTime(value, out);
                break;
            default:
                throw new IllegalArgumentException("Encoding not supported for field type: " + type);
        }

        return out;
    }

    private void writeString(Object value, Charset charset, byte[] out) {
        String text = value.toString();
        byte[] encoded = text.getBytes(charset);
        int copyLength = Math.min(encoded.length, out.length);
        System.arraycopy(encoded, 0, out, 0, copyLength);
        if (copyLength < out.length) {
            for (int i = copyLength; i < out.length; i++) {
                out[i] = 0x20;
            }
        }
    }

    private void writeDate(Object value, byte[] out) {
        LocalDate date;
        if (value instanceof LocalDate) {
            date = (LocalDate) value;
        } else if (value instanceof java.util.Date) {
            date = new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
        } else if (value instanceof CharSequence) {
            String text = value.toString();
            if (text.matches("\\d{8}")) {
                writeString(text, StandardCharsets.US_ASCII, out);
                return;
            }
            throw new IllegalArgumentException("Unsupported date text: " + text);
        } else {
            throw new IllegalArgumentException("Unsupported date value: " + value.getClass());
        }

        String formatted = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        writeString(formatted, StandardCharsets.US_ASCII, out);
    }

    private void writeNumeric(Object value, byte[] out) {
        BigDecimal decimal;
        if (value instanceof BigDecimal) {
            decimal = (BigDecimal) value;
        } else if (value instanceof Number) {
            decimal = new BigDecimal(value.toString());
        } else if (value instanceof CharSequence) {
            decimal = new BigDecimal(value.toString().trim());
        } else {
            throw new IllegalArgumentException("Unsupported numeric value: " + value.getClass());
        }

        String formatted;
        if (decimalCount > 0) {
            formatted = decimal.setScale(decimalCount, BigDecimal.ROUND_HALF_UP).toPlainString();
        } else {
            formatted = decimal.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
        }

        if (formatted.length() > length) {
            formatted = formatted.substring(0, length);
        }

        byte[] bytes = formatted.getBytes(StandardCharsets.US_ASCII);
        int start = out.length - bytes.length;
        if (start < 0) {
            start = 0;
        }
        System.arraycopy(bytes, 0, out, start, Math.min(bytes.length, out.length));
        for (int i = 0; i < start; i++) {
            out[i] = 0x20;
        }
    }

    private void writeBinaryDouble(Object value, byte[] out) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Expected numeric value for DOUBLE/CURRENCY but got " + value.getClass());
        }
        double dbl = ((Number) value).doubleValue();
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).putDouble(dbl);
        System.arraycopy(buffer, 0, out, 0, Math.min(buffer.length, out.length));
    }

    private void writeBinaryInteger(Object value, byte[] out) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Expected numeric value for INTEGER but got " + value.getClass());
        }
        int intVal = ((Number) value).intValue();
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(intVal);
        System.arraycopy(buffer, 0, out, 0, Math.min(buffer.length, out.length));
    }

    private void writeLogical(Object value, byte[] out) {
        char c;
        if (value instanceof Boolean) {
            c = (Boolean) value ? 'T' : 'F';
        } else if (value instanceof CharSequence) {
            String text = value.toString().trim().toUpperCase();
            if (text.isEmpty()) {
                c = '?';
            } else {
                c = text.charAt(0);
            }
        } else {
            throw new IllegalArgumentException("Unsupported logical value: " + value.getClass());
        }
        out[0] = (byte) c;
        for (int i = 1; i < out.length; i++) {
            out[i] = 0x20;
        }
    }

    private void writeDateTime(Object value, byte[] out) {
        LocalDateTime dateTime;
        if (value instanceof LocalDateTime) {
            dateTime = (LocalDateTime) value;
        } else if (value instanceof java.util.Date) {
            dateTime = LocalDateTime.ofInstant(((java.util.Date) value).toInstant(), java.time.ZoneOffset.UTC);
        } else {
            throw new IllegalArgumentException("Unsupported datetime value: " + value.getClass());
        }

        // Convert to Julian day and milliseconds since midnight
        long epochDay = dateTime.toLocalDate().toEpochDay();
        long julianDay = epochDay + 2440588L; // 2440588 is Julian day for Unix epoch
        int millis = dateTime.toLocalTime().toSecondOfDay() * 1000;

        ByteBuffer buf = ByteBuffer.wrap(out).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buf.putInt((int) julianDay);
        buf.putInt(millis);
    }
    /**
     * Reads a single 32-byte field descriptor from the {@code ByteBuffer}.
     * The buffer's position is advanced by 32 bytes to point to the next descriptor or the end-of-fields marker.
     *
     * @param buf The {@code ByteBuffer} containing the field descriptor data.
     * @param charset The character set used to decode the field name (e.g., {@link io.github.zazalng.contracts.DBFEncoding#toCharset()}).
     * @return A new {@code DBFField} instance containing the parsed metadata.
     */
    public static DBFField read(ByteBuffer buf, Charset charset) {
        byte[] nameBytes = new byte[11];
        buf.get(nameBytes);
        String name = new String(nameBytes, charset).trim();
        char typeChar = (char) buf.get();
        buf.position(buf.position() + 4); // skip field data address (4 bytes)
        int length = Byte.toUnsignedInt(buf.get());
        int decimalCount = Byte.toUnsignedInt(buf.get());
        buf.position(buf.position() + 14); // skip reserved (14 bytes)

        DBFDataType type = DBFDataType.fromCode(typeChar);
        return new DBFField(name, type, length, decimalCount);
    }

    /**
     * Decodes the raw byte data for this field into an appropriate Java object based on the field's type.
     *
     * @param data The raw byte array containing the field data for a single record.
     * @param charset The character set used for decoding text-based types (e.g., CHARACTER, NUMERIC, DATE).
     * @return The decoded value as an {@code Object} (e.g., String, Date, Double, Integer, Boolean). Returns {@code null} if the raw data is empty, invalid, or cannot be parsed. Returns the raw byte array for unhandled types (MEMO, GENERAL, etc.).
     */
    public Object decode(byte[] data, Charset charset) {
        String raw = new String(data, charset).trim();
        if (raw.isEmpty()) return null;

        switch (type) {
            case CHARACTER:
            case VARCHAR:
                return raw;

            case DATE:
                return DBFUtils.parseDate(raw);

            case NUMERIC:
            case FLOAT:
                return DBFUtils.parseNumeric(raw);

            case DOUBLE:
            case CURRENCY:
                return DBFUtils.parseDoubleBinary(data);

            case INTEGER:
                return DBFUtils.parseIntBinary(data);

            case LOGICAL:
                String val = raw.toUpperCase();
                switch (val) {
                    case "T":
                    case "Y":
                        return true;
                    case "F":
                    case "N":
                        return false;
                    default:
                        return null;
                }

            case DATETIME:
            case TIMESTAMP:
                return DBFUtils.parseDateTime(data);

            default:
                return data;
        }

    }

    /**
     * Gets the name of the field (up to 10 characters).
     * @return The field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the data type of the field.
     * @return The {@link DBFDataType} enum constant.
     */
    public DBFDataType getType() {
        return type;
    }

    /**
     * Gets the total length of the field data in bytes.
     * @return The field length.
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the number of decimal places for Numeric and Float fields.
     * @return The decimal count.
     */
    public int getDecimalCount() {
        return decimalCount;
    }
}