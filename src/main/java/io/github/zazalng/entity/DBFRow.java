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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Represents a single record (row) of data from a DBF file.
 *
 * <p>This class reads and decodes the raw bytes of a record based on the provided
 * {@link DBFField} descriptors and stores the results as a map of field names to decoded Java objects.</p>
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="https://www.gnu.org/licenses/gpl-3.0.html">GNU General Public License v3.0</a>
 */
public final class DBFRow {
    private final Map<String, Object> values = new LinkedHashMap<>();

    /**
     * Constructs a new DBFRow by reading and decoding the raw bytes from the buffer.
     *
     * @param fields The list of field descriptors defining the record structure.
     * @param buffer The {@code ByteBuffer} containing the raw record data, starting with the deletion flag.
     * @param charset The character set to use for decoding text-based fields.
     */
    public DBFRow(List<DBFField> fields, ByteBuffer buffer, Charset charset) {
        buffer.position(1); // skip deletion flag (byte 0)
        for (DBFField f : fields) {
            byte[] bytes = new byte[f.getLength()];
            buffer.get(bytes);
            Object val = f.decode(bytes, charset);
            values.put(f.getName(), val);
        }
    }

    /**
     * Retrieves the decoded value for a field by its name.
     *
     * @param name The name of the field (case-sensitive, matching the {@link DBFField} name).
     * @return The decoded value as a Java object, or {@code null} if the field is not present or the data was null/empty.
     */
    public Object get(String name) {
        return values.get(name);
    }

    /**
     * Retrieves the Map Object of this row
     * @return Row information as Map Object
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Returns the contents of the row as an unmodifiable Map.
     * The keys are the field names (String) and the values are the decoded objects (Object).
     *
     * @return An unmodifiable map containing all field names and their decoded values.
     */
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Provides a string representation of the row's values.
     * @return A string representing the map of field names and values.
     */
    @Override public String toString() {
        return values.toString();
    }
}