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
 * Defines the possible versions or dialects of the DBF file format,
 * based on the file type byte (the first byte) in the DBF header.
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="https://www.gnu.org/licenses/gpl-3.0.html">GNU General Public License v3.0</a>
 */
public enum DBFVersion {
    // Standard dBase / FoxBASE
    /** Version code 0x02: FoxBASE. */
    FOXBASE((byte) 0x02, "FoxBASE"),
    /** Version code 0x03: FoxBASE+/dBASE III Plus, without a separate memo file. */
    DBASE_III_PLUS_NO_MEMO((byte) 0x03, "FoxBASE+/dBASE III Plus, no memo"),
    /** Version code 0xF5: FoxPro 2.x (or earlier) with a memo file. */
    FOXPRO_2X_MEMO((byte) 0xF5, "FoxPro 2.x (or earlier) with memo"),
    /** Version code 0x83: FoxBASE+/dBASE III Plus, with a separate memo file. */
    FOXBASE_MEMO((byte) 0x83, "FoxBASE+/dBASE III Plus, with memo"),
    /** Version code 0xFB: FoxBASE (simple). */
    FOXBASE_SIMPLE((byte) 0xFB, "FoxBASE"),

    // dBase IV
    /** Version code 0x04: dBASE IV, without a separate memo file. */
    DBASE_IV_NO_MEMO((byte) 0x04, "dBASE IV, no memo"), // Note: Often 0x04 is grouped with 0x03
    /** Version code 0x8B: dBASE IV with a separate memo file. */
    DBASE_IV_MEMO((byte) 0x8B, "dBASE IV with memo"),
    /** Version code 0x43: dBASE IV SQL table files, without a separate memo file. */
    DBASE_IV_SQL_NO_MEMO((byte) 0x43, "dBASE IV SQL table files, no memo"),
    /** Version code 0xCB: dBASE IV SQL table files, with a separate memo file. */
    DBASE_IV_SQL_MEMO((byte) 0xCB, "dBASE IV SQL table files, with memo"),

    // Visual FoxPro
    /** Version code 0x30: Visual FoxPro (VFP) with or without a memo file. */
    VISUAL_FOXPRO((byte) 0x30, "Visual FoxPro"),
    /** Version code 0x31: Visual FoxPro with autoincrement enabled. */
    VISUAL_FOXPRO_AUTOINCREMENT((byte) 0x31, "Visual FoxPro, autoincrement enabled"),
    /** Version code 0x32: Visual FoxPro with Varchar/Varbinary fields. */
    VISUAL_FOXPRO_VARCHAR((byte) 0x32, "Visual FoxPro with Varchar/Varbinary"),

    // Other / Unknown
    /** Version code 0xE5: HiPer-Six format with SMT memo file. */
    HIPER_SIX((byte) 0xE5, "HiPer-Six format with SMT memo file"),
    /** Placeholder for an unsupported or unrecognized version code. */
    UNKNOWN((byte) 0xFF, "Unknown DBF Version"); // Placeholder for unsupported versions

    private final byte code;
    private final String description;

    /**
     * Constructs a {@code DBFVersion} enum constant.
     * @param code The single byte code found in the first byte of the DBF file header.
     * @param description A human-readable description of the version.
     */
    DBFVersion(byte code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Gets the raw byte code representing the DBF version.
     * @return The version code as a byte.
     */
    public byte getCode() {
        return code;
    }

    /**
     * Gets a human-readable description of the DBF version.
     * @return The version description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Factory method to resolve the {@code DBFVersion} from the raw byte code found in the file header.
     * It compares the unsigned integer value of the byte to the known version codes.
     *
     * @param b The raw byte code from the DBF header.
     * @return The matching {@code DBFVersion}, or {@link #UNKNOWN} if no match is found.
     */
    public static DBFVersion fromByte(byte b) {
        // Use unsigned integer comparison since some codes are above 127 (e.g., 0x83)
        int val = Byte.toUnsignedInt(b);
        for (DBFVersion v : values()) {
            if (Byte.toUnsignedInt(v.code) == val) {
                return v;
            }
        }
        return UNKNOWN;
    }
}