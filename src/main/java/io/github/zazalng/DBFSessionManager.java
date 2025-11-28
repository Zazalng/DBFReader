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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple lifecycle manager for {@link DBFSession} instances.
 *
 * <p>Keeps a cache of open sessions keyed by normalized absolute paths so that callers building a
 * centralized access service can reuse existing file channels instead of opening a new channel per request.</p>
 */
public final class DBFSessionManager implements AutoCloseable {
    private final Map<Path, DBFSession> sessions = new ConcurrentHashMap<>();

    /**
     * Acquires a session for the provided DBF file, reusing an existing open session if available.
     *
     * @param path The path to the DBF file.
     * @return An open {@link DBFSession}.
     * @throws IOException if the session cannot be created or reloaded.
     */
    public DBFSession acquire(Path path) throws IOException {
        return acquire(path, null);
    }

    /**
     * Acquires a session with an optional encoding override.
     *
     * @param path     The path to the DBF file.
     * @param encoding Encoding override for textual fields, or {@code null} to use the LDID.
     * @return An open {@link DBFSession}.
     * @throws IOException if the session cannot be created or reloaded.
     */
    public DBFSession acquire(Path path, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        Path key = normalize(path);

        DBFSession existing = sessions.get(key);
        if (existing != null && existing.isOpen()) {
            if (encoding != null && existing.encoding() != encoding) {
                // If the encoding differs, force a reload with the override by recreating the session.
                remove(key);
            } else {
                return existing;
            }
        }

        DBFSession session = DBFSession.open(key, encoding);
        sessions.put(key, session);
        return session;
    }

    /**
     * Removes and closes a session associated with the given path if it exists.
     *
     * @param path The path whose session should be closed.
     * @throws IOException if closing the session fails.
     */
    public void close(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        remove(normalize(path));
    }

    private void remove(Path key) throws IOException {
        DBFSession session = sessions.remove(key);
        if (session != null) {
            session.close();
        }
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Closes all cached sessions.
     *
     * @throws IOException if any session fails to close.
     */
    @Override
    public void close() throws IOException {
        IOException failure = null;
        for (Map.Entry<Path, DBFSession> entry : sessions.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        sessions.clear();
        if (failure != null) {
            throw failure;
        }
    }
}