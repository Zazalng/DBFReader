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

package io.github.zazalng;

import io.github.zazalng.contracts.DBFEncoding;
import io.github.zazalng.contracts.DBFVersion;
import io.github.zazalng.entity.DBFField;
import io.github.zazalng.entity.DBFHeader;
import io.github.zazalng.entity.DBFRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Minimal DBF reader focused on reading and accessing DBF data.
 *
 * <p>This implementation aims to be conservative and extensible, focusing on core DBF file
 * features like reading the header, field descriptors, and all data records as a snapshot.
 * It also supports up to 60 charsets possible for text encoding.</p>
 *
 * <b>Key Features:</b>
 * <ul>
 * <li>Read header and field descriptors.</li>
 * <li>Read all records as a snapshot at construction and on {@link #reload()}.</li>
 * </ul>
 *
 * <b>Limitations / Notes:</b>
 * <ul>
 * <li>Memo fields (.dbt/.fpt/.dbt-like memo) are not parsed here (placeholder).</li>
 * <li>Visual FoxPro-specific binary types, timestamps, and some Level 7 features are partially unsupported.</li>
 * </ul>
 *
 * @author Zazalng
 * @version 1.0.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache-2.0 license</a>
 * @since 1.0.0
 */
public class DBF implements AutoCloseable {
    private final DBFSession session;

    /**
     * Constructs a new DBF reader instance by enforcing the specified encoding and immediately attempts to load the file data.
     * @param path The path to the DBF file. Must not be null.
     * @param encoding The character encoding to use for text fields. If null, defaults based on header info.
     * @throws IOException If an I/O error occurs while reading the file.
     * @throws NullPointerException If the provided path is null.
     */
    public DBF(Path path, DBFEncoding encoding) throws IOException{
        Objects.requireNonNull(path, "path");
        this.session = DBFSession.open(path, encoding);
    }

    /**
     * Constructs a new DBF reader instance and immediately attempts to load the file data.
     * @param path The path to the DBF file. Must not be null.
     * @throws IOException If an I/O error occurs while reading the file.
     * @throws NullPointerException If the provided path is null.
     */
    public DBF(Path path) throws IOException {
        this(path, null);
    }

    /**
     * Clears the current data and reloads the DBF file from the disk.
     * This will re-read the header and all records, updating the internal state.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public void reload() throws IOException {
        session.reload();
    }

    /**
     * Gets the parsed header information for the DBF file.
     * @return The {@link DBFHeader} object.
     */
    public DBFHeader getHeader() {
        return session.header();
    }

    /**
     * Gets a list of field descriptors (columns) as defined in the DBF header.
     * @return An unmodifiable {@code List} of {@link DBFField} objects.
     */
    public List<DBFField> getFields() {
        return session.fields();
    }

    /**
     * Gets the list of all data records read from the DBF file.
     * This is a snapshot taken during the last initialization or {@link #reload()} call.
     * @return An unmodifiable {@code List} of {@link DBFRow} objects.
     */
    public List<DBFRow> getRecords() {
        return session.records();
    }

    /**
     * Gets the version/dialect of the DBF file format.
     * @return The {@link DBFVersion} of the file.
     */
    public DBFVersion getVersion() {
        return session.version();
    }

    /**
     * Provides access to the underlying session for advanced use-cases.
     *
     * @return The live {@link DBFSession} instance backing this facade.
     */
    public DBFSession getSession() {
        return session;
    }

    /**
     * Closes the underlying session, releasing any open file handles and locks.
     *
     * @throws IOException if closing the session fails.
     */
    @Override
    public void close() throws IOException {
        session.close();
    }
}