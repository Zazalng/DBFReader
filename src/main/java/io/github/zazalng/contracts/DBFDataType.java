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

/**
 * Defines the possible data types for fields within a DBF file.
 *
 * <p>The types cover standard dBase III+ formats as well as extensions
 * found in later versions like Visual FoxPro (VFP) and dBase Level 7.</p>
 *
 * @author Zazalng
 * @since 1.0.0
 */
public enum DBFDataType {
    // Standard dBase III+ Types
    /** Standard dBase Character/String field. */
    CHARACTER('C', "Character/String"),
    /** Standard dBase Numeric field with a fixed decimal point. */
    NUMERIC('N', "Numeric (Fixed Point)"),
    /** Standard dBase Float field using binary floating-point representation. */
    FLOAT('F', "Float (Binary Floating Point)"),
    /** Standard dBase Date field (format YYYYMMDD). */
    DATE('D', "Date (YYYYMMDD)"),
    /** Standard dBase Logical/Boolean field. */
    LOGICAL('L', "Logical (Boolean)"),
    /** Standard dBase Memo field, which holds a pointer to an external memo file (.dbt, .fpt). */
    MEMO('M', "Memo (Large Text/Binary Pointer)"),

    // Visual FoxPro (VFP) Types
    /** Visual FoxPro Currency type. */
    CURRENCY('Y', "Currency (VFP)"),
    /** Visual FoxPro DateTime type. */
    DATETIME('T', "DateTime (VFP)"),
    /** Visual FoxPro Double precision floating-point type. */
    DOUBLE('B', "Double (VFP)"),
    /** Visual FoxPro Integer type. */
    INTEGER('I', "Integer (VFP)"),
    /** Visual FoxPro General field (typically OLE or graphics data). */
    GENERAL('G', "General (OLE/Graphics)"),
    /** Visual FoxPro Picture field. */
    PICTURE('P', "Picture (VFP)"),
    /** Visual FoxPro Varchar, a variable-length character field (requires special header handling). */
    VARCHAR('V', "Varchar (VFP Variable Length)"), // Field length needs special handling

    // dBase Level 7 / Other
    /** dBase Level 7 Autoincrement field. */
    AUTOINCREMENT('+', "Autoincrement (dBase Level 7)"),
    /** dBase Level 7 Timestamp field. */
    TIMESTAMP('@', "Timestamp (dBase Level 7)"),

    // Fallback
    /** Fallback for an unknown or unsupported field type code. */
    UNKNOWN('?', "Unknown Type");

    private final char code;
    private final String description;

    /**
     * Constructs a {@code DBFDataType} enum constant.
     * @param code The single-character code used in the DBF file header.
     * @param description A human-readable description of the data type.
     */
    DBFDataType(char code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Gets the single-character code that represents this data type in the DBF header.
     * @return The field type code (e.g., 'C', 'N', 'D').
     */
    public char getCode() {
        return code;
    }

    /**
     * Gets a human-readable description of the data type.
     * @return The data type description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Attempts to find the {@code DBFDataType} enum constant corresponding to the given character code.
     * @param code The character code from the DBF header.
     * @return The matching {@code DBFDataType}, or {@code null} if the code is not recognized.
     */
    public static DBFDataType fromCode(char code) {
        for (var v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        return null;
    }
}