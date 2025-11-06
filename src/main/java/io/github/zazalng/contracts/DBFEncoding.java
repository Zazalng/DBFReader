package worldstandard.group.contracts;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents DBF Language Driver ID (LDID) encodings.
 * Maps the single-byte code in the DBF header to a Java-compatible Charset.
 * Reference based on xBase / dBASE / Visual FoxPro documentation.
 */
public enum DBFEncoding {
    // --- DOS / OEM encodings ---
    CP437_01(0x01, "CP437", "U.S. MS-DOS"),
    CP850_02(0x02, "CP850", "International MS-DOS"),
    CP1252_03(0x03, "windows-1252", "Windows ANSI"),
    CP865_08(0x08, "CP865", "Danish OEM"),
    CP437_09(0x09, "CP437", "Dutch OEM"),
    CP850_0A(0x0A, "CP850", "Dutch OEM*"),
    CP437_0B(0x0B, "CP437", "Finnish OEM"),
    CP437_0D(0x0D, "CP437", "French OEM"),
    CP850_0E(0x0E, "CP850", "French OEM*"),
    CP437_0F(0x0F, "CP437", "German OEM"),
    CP850_10(0x10, "CP850", "German OEM*"),
    CP437_11(0x11, "CP437", "Italian OEM"),
    CP850_12(0x12, "CP850", "Italian OEM*"),
    CP932_13(0x13, "MS932", "Japanese Shift-JIS"),
    CP850_14(0x14, "CP850", "Spanish OEM*"),
    CP437_15(0x15, "CP437", "Swedish OEM"),
    CP850_16(0x16, "CP850", "Swedish OEM*"),
    CP865_17(0x17, "CP865", "Norwegian OEM"),
    CP437_18(0x18, "CP437", "Spanish OEM"),
    CP437_19(0x19, "CP437", "English OEM (Britain)"),
    CP850_1A(0x1A, "CP850", "English OEM (Britain)*"),
    CP437_1B(0x1B, "CP437", "English OEM (U.S.)"),
    CP863_1C(0x1C, "CP863", "French OEM (Canada)"),
    CP850_1D(0x1D, "CP850", "French OEM*"),
    CP852_1F(0x1F, "CP852", "Czech OEM"),
    CP852_22(0x22, "CP852", "Hungarian OEM"),
    CP852_23(0x23, "CP852", "Polish OEM"),
    CP860_24(0x24, "CP860", "Portuguese OEM"),
    CP850_25(0x25, "CP850", "Portuguese OEM*"),
    CP866_26(0x26, "CP866", "Russian OEM"),
    CP850_37(0x37, "CP850", "English OEM (U.S.)*"),
    CP852_40(0x40, "CP852", "Romanian OEM"),
    CP936_4D(0x4D, "GBK", "Chinese GBK (PRC)"),
    CP949_4E(0x4E, "x-windows-949", "Korean (ANSI/OEM)"),
    CP950_4F(0x4F, "x-windows-950", "Chinese Big5 (Taiwan)"),
    CP874_50(0x50, "x-windows-874", "Thai (ANSI/OEM)"),
    CP1252_57(0x57, "windows-1252", "ANSI"),
    CP1252_58(0x58, "windows-1252", "Western European ANSI"),
    CP1252_59(0x59, "windows-1252", "Spanish ANSI"),
    CP852_64(0x64, "CP852", "Eastern European MS-DOS"),
    CP866_65(0x65, "CP866", "Russian MS-DOS"),
    CP865_66(0x66, "CP865", "Nordic MS-DOS"),
    CP861_67(0x67, "CP861", "Icelandic MS-DOS"),
    CP737_6A(0x6A, "CP737", "Greek MS-DOS (437G)"),
    CP857_6B(0x6B, "CP857", "Turkish MS-DOS"),
    CP863_6C(0x6C, "CP863", "French-Canadian MS-DOS"),
    CP950_78(0x78, "x-windows-950", "Taiwan Big5"),
    CP949_79(0x79, "x-windows-949", "Hangul (Wansung)"),
    CP936_7A(0x7A, "GBK", "PRC GBK"),
    CP932_7B(0x7B, "MS932", "Japanese Shift-JIS"),
    CP874_7C(0x7C, "x-windows-874", "Thai Windows/MS-DOS"),
    CP737_86(0x86, "CP737", "Greek OEM"),
    CP852_87(0x87, "CP852", "Slovenian OEM"),
    CP857_88(0x88, "CP857", "Turkish OEM"),
    CP1250_C8(0xC8, "windows-1250", "Eastern European Windows"),
    CP1251_C9(0xC9, "windows-1251", "Russian Windows"),
    CP1254_CA(0xCA, "windows-1254", "Turkish Windows"),
    CP1253_CB(0xCB, "windows-1253", "Greek Windows"),
    CP1257_CC(0xCC, "windows-1257", "Baltic Windows"),

    // --- Default fallback ---
    UNKNOWN(0x00, "windows-1252", "Default (ANSI / unknown)");

    private static final Map<Integer, DBFEncoding> LOOKUP = new HashMap<>();

    static {
        for (DBFEncoding enc : values()) LOOKUP.put(enc.code, enc);
    }

    private final int code;
    private final String charsetName;
    private final String description;

    DBFEncoding(int code, String charsetName, String description) {
        this.code = code;
        this.charsetName = charsetName;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getCharsetName() { return charsetName; }
    public String getDescription() { return description; }
    public Charset toCharset() { return Charset.forName(charsetName); }

    public static DBFEncoding fromCode(int code) {
        return LOOKUP.getOrDefault(code, UNKNOWN);
    }

    @Override
    public String toString() {
        return name() + "(" + String.format("0x%02X", code) + ", " + charsetName + ")";
    }
}