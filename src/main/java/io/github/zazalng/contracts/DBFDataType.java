package worldstandard.group.contracts;

public enum DBFDataType {
    // Standard dBase III+ Types
    CHARACTER('C', "Character/String"),
    NUMERIC('N', "Numeric (Fixed Point)"),
    FLOAT('F', "Float (Binary Floating Point)"),
    DATE('D', "Date (YYYYMMDD)"),
    LOGICAL('L', "Logical (Boolean)"),
    MEMO('M', "Memo (Large Text/Binary Pointer)"),

    // Visual FoxPro (VFP) Types
    CURRENCY('Y', "Currency (VFP)"),
    DATETIME('T', "DateTime (VFP)"),
    DOUBLE('B', "Double (VFP)"),
    INTEGER('I', "Integer (VFP)"),
    GENERAL('G', "General (OLE/Graphics)"),
    PICTURE('P', "Picture (VFP)"),
    VARCHAR('V', "Varchar (VFP Variable Length)"), // Field length needs special handling

    // dBase Level 7 / Other
    AUTOINCREMENT('+', "Autoincrement (dBase Level 7)"),
    TIMESTAMP('@', "Timestamp (dBase Level 7)"),

    // Fallback
    UNKNOWN('?', "Unknown Type");

    private final char code;
    private final String description;

    DBFDataType(char code, String description) {
        this.code = code;
        this.description = description;
    }

    public char getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DBFDataType fromCode(char code) {
        for (var v : values()) if (v.code == code) return v;
        return null;
    }
}