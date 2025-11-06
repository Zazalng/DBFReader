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

package io.github.zazalng.contracts;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents DBF Language Driver ID (LDID) encodings, which define the character set
 * used for text fields within the file.
 *
 * <p>This enum maps the single-byte code found in the DBF header (byte 29) to a
 * Java-compatible {@link Charset} name. The references are based on standard
 * xBase, dBASE, and Visual FoxPro documentation.</p>
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="https://www.gnu.org/licenses/gpl-3.0.html">GNU General Public License v3.0</a>
 */
public enum DBFEncoding {
    // --- DOS / OEM encodings ---
    /** LDID: 0x01 - U.S. MS-DOS (CP437). */
    CP437_01(0x01, "CP437", "U.S. MS-DOS"),
    /** LDID: 0x02 - International MS-DOS (CP850). */
    CP850_02(0x02, "CP850", "International MS-DOS"),
    /** LDID: 0x03 - Windows ANSI (windows-1252). */
    CP1252_03(0x03, "windows-1252", "Windows ANSI"),
    /** LDID: 0x08 - Danish OEM (CP865). */
    CP865_08(0x08, "CP865", "Danish OEM"),
    /** LDID: 0x09 - Dutch OEM (CP437). */
    CP437_09(0x09, "CP437", "Dutch OEM"),
    /** LDID: 0x0A - Dutch OEM* (CP850). */
    CP850_0A(0x0A, "CP850", "Dutch OEM*"),
    /** LDID: 0x0B - Finnish OEM (CP437). */
    CP437_0B(0x0B, "CP437", "Finnish OEM"),
    /** LDID: 0x0D - French OEM (CP437). */
    CP437_0D(0x0D, "CP437", "French OEM"),
    /** LDID: 0x0E - French OEM* (CP850). */
    CP850_0E(0x0E, "CP850", "French OEM*"),
    /** LDID: 0x0F - German OEM (CP437). */
    CP437_0F(0x0F, "CP437", "German OEM"),
    /** LDID: 0x10 - German OEM* (CP850). */
    CP850_10(0x10, "CP850", "German OEM*"),
    /** LDID: 0x11 - Italian OEM (CP437). */
    CP437_11(0x11, "CP437", "Italian OEM"),
    /** LDID: 0x12 - Italian OEM* (CP850). */
    CP850_12(0x12, "CP850", "Italian OEM*"),
    /** LDID: 0x13 - Japanese Shift-JIS (MS932). */
    CP932_13(0x13, "MS932", "Japanese Shift-JIS"),
    /** LDID: 0x14 - Spanish OEM* (CP850). */
    CP850_14(0x14, "CP850", "Spanish OEM*"),
    /** LDID: 0x15 - Swedish OEM (CP437). */
    CP437_15(0x15, "CP437", "Swedish OEM"),
    /** LDID: 0x16 - Swedish OEM* (CP850). */
    CP850_16(0x16, "CP850", "Swedish OEM*"),
    /** LDID: 0x17 - Norwegian OEM (CP865). */
    CP865_17(0x17, "CP865", "Norwegian OEM"),
    /** LDID: 0x18 - Spanish OEM (CP437). */
    CP437_18(0x18, "CP437", "Spanish OEM"),
    /** LDID: 0x19 - English OEM (Britain) (CP437). */
    CP437_19(0x19, "CP437", "English OEM (Britain)"),
    /** LDID: 0x1A - English OEM (Britain)* (CP850). */
    CP850_1A(0x1A, "CP850", "English OEM (Britain)*"),
    /** LDID: 0x1B - English OEM (U.S.) (CP437). */
    CP437_1B(0x1B, "CP437", "English OEM (U.S.)"),
    /** LDID: 0x1C - French OEM (Canada) (CP863). */
    CP863_1C(0x1C, "CP863", "French OEM (Canada)"),
    /** LDID: 0x1D - French OEM* (CP850). */
    CP850_1D(0x1D, "CP850", "French OEM*"),
    /** LDID: 0x1F - Czech OEM (CP852). */
    CP852_1F(0x1F, "CP852", "Czech OEM"),
    /** LDID: 0x22 - Hungarian OEM (CP852). */
    CP852_22(0x22, "CP852", "Hungarian OEM"),
    /** LDID: 0x23 - Polish OEM (CP852). */
    CP852_23(0x23, "CP852", "Polish OEM"),
    /** LDID: 0x24 - Portuguese OEM (CP860). */
    CP860_24(0x24, "CP860", "Portuguese OEM"),
    /** LDID: 0x25 - Portuguese OEM* (CP850). */
    CP850_25(0x25, "CP850", "Portuguese OEM*"),
    /** LDID: 0x26 - Russian OEM (CP866). */
    CP866_26(0x26, "CP866", "Russian OEM"),
    /** LDID: 0x37 - English OEM (U.S.)* (CP850). */
    CP850_37(0x37, "CP850", "English OEM (U.S.)*"),
    /** LDID: 0x40 - Romanian OEM (CP852). */
    CP852_40(0x40, "CP852", "Romanian OEM"),
    /** LDID: 0x4D - Chinese GBK (PRC) (GBK). */
    CP936_4D(0x4D, "GBK", "Chinese GBK (PRC)"),
    /** LDID: 0x4E - Korean (ANSI/OEM) (x-windows-949). */
    CP949_4E(0x4E, "x-windows-949", "Korean (ANSI/OEM)"),
    /** LDID: 0x4F - Chinese Big5 (Taiwan) (x-windows-950). */
    CP950_4F(0x4F, "x-windows-950", "Chinese Big5 (Taiwan)"),
    /** LDID: 0x50 - Thai (ANSI/OEM) (x-windows-874). */
    CP874_50(0x50, "x-windows-874", "Thai (ANSI/OEM)"),
    /** LDID: 0x57 - ANSI (windows-1252). */
    CP1252_57(0x57, "windows-1252", "ANSI"),
    /** LDID: 0x58 - Western European ANSI (windows-1252). */
    CP1252_58(0x58, "windows-1252", "Western European ANSI"),
    /** LDID: 0x59 - Spanish ANSI (windows-1252). */
    CP1252_59(0x59, "windows-1252", "Spanish ANSI"),
    /** LDID: 0x64 - Eastern European MS-DOS (CP852). */
    CP852_64(0x64, "CP852", "Eastern European MS-DOS"),
    /** LDID: 0x65 - Russian MS-DOS (CP866). */
    CP866_65(0x65, "CP866", "Russian MS-DOS"),
    /** LDID: 0x66 - Nordic MS-DOS (CP865). */
    CP865_66(0x66, "CP865", "Nordic MS-DOS"),
    /** LDID: 0x67 - Icelandic MS-DOS (CP861). */
    CP861_67(0x67, "CP861", "Icelandic MS-DOS"),
    /** LDID: 0x6A - Greek MS-DOS (437G) (CP737). */
    CP737_6A(0x6A, "CP737", "Greek MS-DOS (437G)"),
    /** LDID: 0x6B - Turkish MS-DOS (CP857). */
    CP857_6B(0x6B, "CP857", "Turkish MS-DOS"),
    /** LDID: 0x6C - French-Canadian MS-DOS (CP863). */
    CP863_6C(0x6C, "CP863", "French-Canadian MS-DOS"),
    /** LDID: 0x78 - Taiwan Big5 (x-windows-950). */
    CP950_78(0x78, "x-windows-950", "Taiwan Big5"),
    /** LDID: 0x79 - Hangul (Wansung) (x-windows-949). */
    CP949_79(0x79, "x-windows-949", "Hangul (Wansung)"),
    /** LDID: 0x7A - PRC GBK (GBK). */
    CP936_7A(0x7A, "GBK", "PRC GBK"),
    /** LDID: 0x7B - Japanese Shift-JIS (MS932). */
    CP932_7B(0x7B, "MS932", "Japanese Shift-JIS"),
    /** LDID: 0x7C - Thai Windows/MS-DOS (x-windows-874). */
    CP874_7C(0x7C, "x-windows-874", "Thai Windows/MS-DOS"),
    /** LDID: 0x86 - Greek OEM (CP737). */
    CP737_86(0x86, "CP737", "Greek OEM"),
    /** LDID: 0x87 - Slovenian OEM (CP852). */
    CP852_87(0x87, "CP852", "Slovenian OEM"),
    /** LDID: 0x88 - Turkish OEM (CP857). */
    CP857_88(0x88, "CP857", "Turkish OEM"),
    /** LDID: 0xC8 - Eastern European Windows (windows-1250). */
    CP1250_C8(0xC8, "windows-1250", "Eastern European Windows"),
    /** LDID: 0xC9 - Russian Windows (windows-1251). */
    CP1251_C9(0xC9, "windows-1251", "Russian Windows"),
    /** LDID: 0xCA - Turkish Windows (windows-1254). */
    CP1254_CA(0xCA, "windows-1254", "Turkish Windows"),
    /** LDID: 0xCB - Greek Windows (windows-1253). */
    CP1253_CB(0xCB, "windows-1253", "Greek Windows"),
    /** LDID: 0xCC - Baltic Windows (windows-1257). */
    CP1257_CC(0xCC, "windows-1257", "Baltic Windows"),

    // --- Default fallback ---
    /** LDID: 0x00 - Default fallback, typically assumed to be Windows ANSI (windows-1252) when not specified. */
    UNKNOWN(0x00, "windows-1252", "Default (ANSI / unknown)");

    private static final Map<Integer, DBFEncoding> LOOKUP = new HashMap<>();

    static {
        for (DBFEncoding enc : values()) LOOKUP.put(enc.code, enc);
    }

    private final int code;
    private final String charsetName;
    private final String description;

    /**
     * Constructs a {@code DBFEncoding} enum constant.
     * @param code The single-byte Language Driver ID from the DBF header (byte 29).
     * @param charsetName The canonical name of the Java-supported character set.
     * @param description A human-readable description of the encoding.
     */
    DBFEncoding(int code, String charsetName, String description) {
        this.code = code;
        this.charsetName = charsetName;
        this.description = description;
    }

    /**
     * Gets the Language Driver ID (LDID) code.
     * @return The integer code (0x00 to 0xFF).
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the Java-compatible character set name.
     * @return The string name of the character set (e.g., "CP437", "windows-1252").
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Gets a human-readable description of the encoding.
     * @return The description of the language/platform encoding.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Converts the encoding name into a {@link Charset} object for I/O operations.
     * @return A {@link Charset} instance.
     */
    public Charset toCharset() {
        return Charset.forName(charsetName);
    }

    /**
     * Retrieves the {@code DBFEncoding} enum constant corresponding to the given LDID code.
     * Defaults to {@link #UNKNOWN} if the code is not found.
     * @param code The integer LDID code to look up.
     * @return The matching {@code DBFEncoding} constant.
     */
    public static DBFEncoding fromCode(int code) {
        return LOOKUP.getOrDefault(code, UNKNOWN);
    }

    /**
     * Provides a descriptive string representation of the encoding.
     * @return The enum name, hex code, and charset name.
     */
    @Override
    public String toString() {
        return name() + "(" + String.format("0x%02X", code) + ", " + charsetName + ")";
    }
}