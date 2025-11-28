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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * High-level helper for performing write operations against a DBF file. Internally delegates to a
 * {@link DBFSession} while providing a simplified API that callers of the legacy 1.x reader can adopt.
 */
public final class DBFWriter implements AutoCloseable {
    private final DBFSession session;

    private DBFWriter(DBFSession session) {
        this.session = session;
    }

    public static DBFWriter open(Path path) throws IOException {
        return open(path, null);
    }

    public static DBFWriter open(Path path, DBFEncoding encoding) throws IOException {
        return new DBFWriter(DBFSession.open(path, encoding));
    }

    public void append(Map<String, Object> row) throws IOException {
        session.appendRecord(row);
    }

    public void appendAll(List<Map<String, Object>> rows) throws IOException {
        session.appendRecords(rows);
    }

    public void update(int index, Map<String, Object> values) throws IOException {
        session.updateRecord(index, values);
    }

    public void delete(int index) throws IOException {
        session.deleteRecord(index);
    }

    public void undelete(int index) throws IOException {
        session.undeleteRecord(index);
    }

    public DBFSession session() {
        return session;
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}