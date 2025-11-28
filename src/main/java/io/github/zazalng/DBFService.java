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
import io.github.zazalng.entity.DBFHeader;
import io.github.zazalng.entity.DBFRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service layer that coordinates access to {@link DBFSession} instances.
 *
 * <p>It combines {@link DBFSessionManager} with in-memory read/write locking so higher level
 * applications (e.g. REST services) can safely serve concurrent readers while serializing writes.</p>
 */
public final class DBFService implements AutoCloseable {
    private final DBFSessionManager sessionManager;
    private final ConcurrentHashMap<Path, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public DBFService() {
        this(new DBFSessionManager());
    }

    public DBFService(DBFSessionManager sessionManager) {
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
    }

    public List<DBFRow> readRecords(Path path) throws IOException {
        return readRecords(path, null);
    }

    public List<DBFRow> readRecords(Path path, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        ReentrantReadWriteLock.ReadLock readLock = lockFor(path).readLock();
        readLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            session.reload();
            return new ArrayList<>(session.records());
        } finally {
            readLock.unlock();
        }
    }

    public DBFHeader readHeader(Path path) throws IOException {
        return readHeader(path, null);
    }

    public DBFHeader readHeader(Path path, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        ReentrantReadWriteLock.ReadLock readLock = lockFor(path).readLock();
        readLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            session.reload();
            return session.header();
        } finally {
            readLock.unlock();
        }
    }

    public void append(Path path, Map<String, Object> row) throws IOException {
        append(path, row, null);
    }

    public void append(Path path, Map<String, Object> row, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(row, "row");
        ReentrantReadWriteLock.WriteLock writeLock = lockFor(path).writeLock();
        writeLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            session.appendRecord(row);
        } finally {
            writeLock.unlock();
        }
    }

    public void appendAll(Path path, List<Map<String, Object>> rows) throws IOException {
        appendAll(path, rows, null);
    }

    public void appendAll(Path path, List<Map<String, Object>> rows, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(rows, "rows");
        if (rows.isEmpty()) {
            return;
        }
        ReentrantReadWriteLock.WriteLock writeLock = lockFor(path).writeLock();
        writeLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            session.appendRecords(rows);
        } finally {
            writeLock.unlock();
        }
    }

    public void update(Path path, int index, Map<String, Object> values) throws IOException {
        update(path, index, values, null);
    }

    public void update(Path path, int index, Map<String, Object> values, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(values, "values");
        ReentrantReadWriteLock.WriteLock writeLock = lockFor(path).writeLock();
        writeLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            session.updateRecord(index, values);
        } finally {
            writeLock.unlock();
        }
    }

    public void delete(Path path, int index) throws IOException {
        delete(path, index, null);
    }

    public void delete(Path path, int index, DBFEncoding encoding) throws IOException {
        mutateDeletion(path, index, encoding, true);
    }

    public void undelete(Path path, int index) throws IOException {
        undelete(path, index, null);
    }

    public void undelete(Path path, int index, DBFEncoding encoding) throws IOException {
        mutateDeletion(path, index, encoding, false);
    }

    private void mutateDeletion(Path path, int index, DBFEncoding encoding, boolean deleted) throws IOException {
        Objects.requireNonNull(path, "path");
        ReentrantReadWriteLock.WriteLock writeLock = lockFor(path).writeLock();
        writeLock.lock();
        try {
            DBFSession session = sessionManager.acquire(path, encoding);
            if (deleted) {
                session.deleteRecord(index);
            } else {
                session.undeleteRecord(index);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public DBFSession session(Path path) throws IOException {
        return session(path, null);
    }

    public DBFSession session(Path path, DBFEncoding encoding) throws IOException {
        Objects.requireNonNull(path, "path");
        ReentrantReadWriteLock.ReadLock readLock = lockFor(path).readLock();
        readLock.lock();
        try {
            return sessionManager.acquire(path, encoding);
        } finally {
            readLock.unlock();
        }
    }

    private ReentrantReadWriteLock lockFor(Path path) {
        Path key = normalize(path);
        return lockMap.computeIfAbsent(key, p -> new ReentrantReadWriteLock());
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    @Override
    public void close() throws IOException {
        sessionManager.close();
        lockMap.clear();
    }
}