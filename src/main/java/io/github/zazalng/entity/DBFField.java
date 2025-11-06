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

import io.github.zazalng.contracts.DBFDataType;
import io.github.zazalng.utility.DBFUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Represents a single field (column) descriptor from the DBF file header.
 * This Java record holds the metadata required to interpret data stored in the records.
 *
 * @param name The name of the field (up to 10 characters).
 * @param type The {@link DBFDataType} of the field.
 * @param length The total length of the field data in bytes.
 * @param decimalCount The number of decimal places for Numeric and Float fields.
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="https://www.gnu.org/licenses/gpl-3.0.html">GNU General Public License v3.0</a>
 */
public record DBFField(
        String name,
        DBFDataType type,
        int length,
        int decimalCount
) {

    /**
     * Reads a single 32-byte field descriptor from the {@code ByteBuffer}.
     * The buffer's position is advanced by 32 bytes.
     *
     * @param buf The {@code ByteBuffer} containing the field descriptor data.
     * @param charset The character set used to decode the field name.
     * @return A new {@code DBFField} record instance.
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
     * @param charset The character set used for decoding text-based types.
     * @return The decoded value as an {@code Object} (e.g., String, Date, Double, Integer, Boolean, or raw byte array for unhandled types). Returns {@code null} if the raw data is empty or invalid.
     */
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