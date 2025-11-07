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
package io.github.zazalng.utility;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class containing static methods for decoding specific DBF field data types.
 *
 * <p>These methods handle conversions from raw byte arrays or padded strings into
 * appropriate Java types such as {@link LocalDate}, {@link BigDecimal}, and {@link LocalDateTime}.</p>
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache-2.0 license</a>
 */
public final class DBFUtils {
    private DBFUtils() {}

    /**
     * Parses a DBF Date string (format YYYYMMDD) into a {@link LocalDate}.
     *
     * @param text The 8-character string representing the date.
     * @return The parsed {@code LocalDate}, or {@code null} if the string is invalid or empty.
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.isEmpty() || text.length() != 8) return null;
        try {
            int y = Integer.parseInt(text.substring(0, 4));
            int m = Integer.parseInt(text.substring(4, 6));
            int d = Integer.parseInt(text.substring(6, 8));
            return LocalDate.of(y, m, d);
        } catch (Exception e) { return null; }
    }

    /**
     * Parses a DBF Numeric or Float string into a {@link BigDecimal}.
     *
     * @param text The string containing the numeric value, possibly padded with spaces.
     * @return The parsed {@code BigDecimal}, or {@code null} if the string cannot be parsed.
     */
    public static BigDecimal parseNumeric(String text) {
        try {
            BigDecimal bd = new BigDecimal(text.trim());
            // Recreating the BigDecimal ensures it uses non-scientific notation if needed.
            return new BigDecimal(bd.toPlainString());
        } catch (Exception e) { return null; }
    }

    /**
     * Decodes 8 raw bytes representing a VFP Double/Currency type (IEEE 754 64-bit float)
     * into a {@link BigDecimal}.
     *
     * @param data The 8-byte array containing the little-endian binary double.
     * @return The decoded value as a {@code BigDecimal}, or {@code null} if data is insufficient.
     */
    public static BigDecimal parseDoubleBinary(byte[] data) {
        if (data == null || data.length < 8) return null;
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return BigDecimal.valueOf(buf.getDouble());
    }

    /**
     * Decodes 4 raw bytes representing a VFP Integer type into an {@link Integer}.
     *
     * @param data The 4-byte array containing the little-endian binary integer.
     * @return The decoded {@code Integer}, or {@code null} if data is insufficient.
     */
    public static Integer parseIntBinary(byte[] data) {
        if (data == null || data.length < 4) return null;
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt();
    }

    /**
     * Decodes 8 raw bytes representing a VFP or dBase Level 7 DateTime/Timestamp into a {@link LocalDateTime}.
     *
     * <p>The first 4 bytes are the Julian day number (since 1/1/4713 BC).
     * The second 4 bytes are milliseconds since midnight.</p>
     *
     * @param data The 8-byte array containing the little-endian Julian day and milliseconds.
     * @return The decoded {@code LocalDateTime}, or {@code null} if data is insufficient or decoding fails.
     */
    public static LocalDateTime parseDateTime(byte[] data) {
        if (data == null || data.length < 8) return null;
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        try {
            int days = buf.getInt();
            int millis = buf.getInt();

            // 1721426 is the Julian Day Number for Jan 1, 0001 AD (proleptic Gregorian calendar)
            return LocalDate.of(1, 1, 1)
                    .plusDays(days - 1721426)
                    .atStartOfDay()
                    .plusSeconds(millis / 1000L);
        } catch (Exception e) { return null; }
    }
}