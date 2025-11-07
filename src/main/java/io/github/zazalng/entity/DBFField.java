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
import java.nio.charset.Charset;

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