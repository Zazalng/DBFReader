package worldstandard.group.contracts;

import java.util.Arrays;

public enum DBFVersion {
    // Standard dBase / FoxBASE
    FOXBASE((byte) 0x02, "FoxBASE"),
    DBASE_III_PLUS_NO_MEMO((byte) 0x03, "FoxBASE+/dBASE III Plus, no memo"),
    FOXPRO_2X_MEMO((byte) 0xF5, "FoxPro 2.x (or earlier) with memo"),
    FOXBASE_MEMO((byte) 0x83, "FoxBASE+/dBASE III Plus, with memo"),
    FOXBASE_SIMPLE((byte) 0xFB, "FoxBASE"),

    // dBase IV
    DBASE_IV_NO_MEMO((byte) 0x04, "dBASE IV, no memo"), // Note: Often 0x04 is grouped with 0x03
    DBASE_IV_MEMO((byte) 0x8B, "dBASE IV with memo"),
    DBASE_IV_SQL_NO_MEMO((byte) 0x43, "dBASE IV SQL table files, no memo"),
    DBASE_IV_SQL_MEMO((byte) 0xCB, "dBASE IV SQL table files, with memo"),

    // Visual FoxPro
    VISUAL_FOXPRO((byte) 0x30, "Visual FoxPro"),
    VISUAL_FOXPRO_AUTOINCREMENT((byte) 0x31, "Visual FoxPro, autoincrement enabled"),
    VISUAL_FOXPRO_VARCHAR((byte) 0x32, "Visual FoxPro with Varchar/Varbinary"),

    // Other / Unknown
    HIPER_SIX((byte) 0xE5, "HiPer-Six format with SMT memo file"),
    UNKNOWN((byte) 0xFF, "Unknown DBF Version"); // Placeholder for unsupported versions

    private final byte code;
    private final String description;

    DBFVersion(byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Factory method to resolve the DBFVersion from the raw byte code.
     */
    public static DBFVersion fromByte(byte b) {
        int val = Byte.toUnsignedInt(b);
        for (var v : values()) if (v.code == val) return v;
        return UNKNOWN;
    }
}
