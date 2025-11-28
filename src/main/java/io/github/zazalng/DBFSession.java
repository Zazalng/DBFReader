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
import io.github.zazalng.entity.DBFRecordWriter;
import io.github.zazalng.entity.DBFRow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.time.LocalDate;

/**
 * Stateful wrapper around a DBF file that keeps the file channel open and provides
 * operations to reload the header and records as well as coordinate exclusive access
 * through file locking.
 *
 * <p>This is the cornerstone for the 2.x series which targets long-lived sessions that can be
 * reused by higher-level services instead of copying DBF files to temporary locations.</p>
 */
public final class DBFSession implements AutoCloseable {
    private final Path path;
    private final DBFEncoding overrideEncoding;

    private FileChannel channel;
    private DBFHeader header;
    private List<DBFRow> records = Collections.emptyList();
    private FileLock lock;
    private boolean closed;

    private static final byte EOF_MARKER = 0x1A;

    private DBFSession(Path path, DBFEncoding encoding) {
        this.path = Objects.requireNonNull(path, "path");
        this.overrideEncoding = encoding;
    }

    /**
     * Opens a new DBF session with the given path.
     *
     * @param path The DBF file path.
     * @return an initialized {@link DBFSession} ready for use.
     * @throws IOException if the file cannot be opened or read.
     */
    public static DBFSession open(Path path) throws IOException {
        return open(path, null);
    }

    /**
     * Opens a new DBF session with an explicit encoding override.
     *
     * @param path     The DBF file path.
     * @param encoding Optional encoding override for text fields.
     * @return an initialized {@link DBFSession} ready for use.
     * @throws IOException if the file cannot be opened or read.
     */
    public static DBFSession open(Path path, DBFEncoding encoding) throws IOException {
        DBFSession session = new DBFSession(path, encoding);
        session.openChannel();
        session.reload();
        return session;
    }

    private void openChannel() throws IOException {
        if (channel != null && channel.isOpen()) {
            return;
        }
        channel = FileChannel.open(path, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("DBF session is already closed");
        }
    }

    private void ensureLoaded() {
        ensureOpen();
        if (header == null) {
            throw new IllegalStateException("DBF session has not been loaded yet");
        }
    }

    /**
     * Reloads the header and record snapshot from disk using the existing channel.
     *
     * @throws IOException if reading from the underlying channel fails.
     */
    public synchronized void reload() throws IOException {
        ensureOpen();
        openChannel();

        channel.position(0L);
        DBFHeader latestHeader = new DBFHeader(channel, overrideEncoding);
        List<DBFRow> latestRecords = latestHeader.readRecords(channel);

        this.header = latestHeader;
        this.records = latestRecords;
    }

    /**
     * Attempts to acquire an exclusive lock on the DBF file without blocking.
     *
     * @return {@code true} if the lock was acquired, {@code false} otherwise.
     * @throws IOException if the channel cannot obtain a lock.
     */
    public synchronized boolean tryLockExclusive() throws IOException {
        ensureOpen();
        openChannel();
        if (lock != null && lock.isValid()) {
            return true;
        }
        lock = channel.tryLock(0L, Long.MAX_VALUE, false);
        return lock != null;
    }

    /**
     * Releases the current lock if present.
     *
     * @throws IOException if releasing the lock fails.
     */
    public synchronized void unlock() throws IOException {
        if (lock != null) {
            try {
                lock.release();
            } finally {
                lock = null;
            }
        }
    }

    /**
     * @return The underlying DBF header from the last {@link #reload()} operation.
     */
    public synchronized DBFHeader header() {
        ensureLoaded();
        return header;
    }

    /**
     * @return The character encoding in effect for this session.
     */
    public synchronized DBFEncoding encoding() {
        ensureLoaded();
        return header.encoding();
    }

    /**
     * @return An unmodifiable view of the field descriptors.
     */
    public synchronized List<DBFField> fields() {
        ensureLoaded();
        return header.fields();
    }

    /**
     * @return An unmodifiable snapshot of the records last read from disk.
     */
    public synchronized List<DBFRow> records() {
        ensureLoaded();
        return records;
    }

    /**
     * Appends a single record to the DBF file using the provided field values.
     *
     * @param values Map of field name to value. Missing entries are treated as {@code null}.
     * @throws IOException if writing to the underlying file fails.
     */
    public synchronized void appendRecord(Map<String, Object> values) throws IOException {
        ensureLoaded();
        appendRecords(Collections.singletonList(values));
    }

    /**
     * Appends multiple records in a single I/O operation batch.
     *
     * @param rows List of row maps to append. Empty or {@code null} input is ignored.
     * @throws IOException if writing to the underlying file fails.
     */
    public synchronized void appendRecords(List<Map<String, Object>> rows) throws IOException {
        ensureLoaded();
        if (rows == null || rows.isEmpty()) {
            return;
        }

        openChannel();

        boolean acquiredLock = false;
        try {
            if (lock == null || !lock.isValid()) {
                lock = channel.lock(0L, Long.MAX_VALUE, false);
                acquiredLock = true;
            }

            int recordLength = Short.toUnsignedInt(header.recordLength());
            int headerLength = Short.toUnsignedInt(header.headerLength());
            long position = headerLength + ((long) header.recordCount() * recordLength);

            DBFRecordWriter writer = new DBFRecordWriter(header.fields(), header.encoding().toCharset(), recordLength);

            for (Map<String, Object> row : rows) {
                ByteBuffer encoded = writer.encode(row, false);
                channel.position(position);
                channel.write(encoded);
                position += recordLength;
            }

            channel.position(position);
            channel.write(ByteBuffer.wrap(new byte[]{EOF_MARKER}));

            int newCount = header.recordCount() + rows.size();
            updateHeaderMetadata(newCount);

            channel.force(true);
            reload();
        } finally {
            if (acquiredLock) {
                unlock();
            }
        }
    }

    /**
     * @return The DBF version/dialect of the loaded file.
     */
    public synchronized DBFVersion version() {
        ensureLoaded();
        return header.version();
    }

    /**
     * @return {@code true} if the session is still open, {@code false} if {@link #close()} has been called.
     */
    public synchronized boolean isOpen() {
        return !closed;
    }

    /**
     * Closes the session, releasing locks and the underlying channel.
     *
     * @throws IOException if closing the channel or releasing the lock fails.
     */
    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            unlock();
        } finally {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            closed = true;
        }
    }

    /**
     * @return The original path backing this session.
     */
    public Path path() {
        return path;
    }

    private void updateHeaderMetadata(int newRecordCount) throws IOException {
        channel.position(0L);
        ByteBuffer headerBuffer = ByteBuffer.allocate(32);
        channel.read(headerBuffer);
        byte[] data = headerBuffer.array();

        LocalDate today = LocalDate.now();
        data[1] = (byte) (today.getYear() - 1900);
        data[2] = (byte) today.getMonthValue();
        data[3] = (byte) today.getDayOfMonth();

        ByteBuffer.wrap(data, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(newRecordCount);

        channel.position(0L);
        channel.write(ByteBuffer.wrap(data));
    }

    /**
     * Rewrites a record at the specified index with the provided values.
     *
     * @param index 0-based index of the record to update.
     * @param values Map containing the new field values.
     * @throws IOException if writing fails or index is invalid.
     */
    public synchronized void updateRecord(int index, Map<String, Object> values) throws IOException {
        ensureLoaded();
        Objects.requireNonNull(values, "values");
        if (index < 0 || index >= header.recordCount()) {
            throw new IndexOutOfBoundsException("Record index out of range: " + index);
        }

        openChannel();

        boolean acquiredLock = false;
        try {
            if (lock == null || !lock.isValid()) {
                lock = channel.lock(0L, Long.MAX_VALUE, false);
                acquiredLock = true;
            }

            int recordLength = Short.toUnsignedInt(header.recordLength());
            int headerLength = Short.toUnsignedInt(header.headerLength());
            long position = headerLength + ((long) index * recordLength);

            DBFRecordWriter writer = new DBFRecordWriter(header.fields(), header.encoding().toCharset(), recordLength);
            ByteBuffer encoded = writer.encode(values, false);

            channel.position(position);
            channel.write(encoded);
            channel.force(true);
            reload();
        } finally {
            if (acquiredLock) {
                unlock();
            }
        }
    }

    /**
     * Marks a record as deleted (soft delete) by setting the deletion flag.
     *
     * @param index Index of the record to mark as deleted.
     * @throws IOException if the index is invalid or writing fails.
     */
    public synchronized void deleteRecord(int index) throws IOException {
        updateDeletionFlag(index, true);
    }

    /**
     * Restores a previously deleted record by clearing the deletion flag.
     *
     * @param index Index of the record to restore.
     * @throws IOException if the index is invalid or writing fails.
     */
    public synchronized void undeleteRecord(int index) throws IOException {
        updateDeletionFlag(index, false);
    }

    private void updateDeletionFlag(int index, boolean deleted) throws IOException {
        ensureLoaded();
        if (index < 0 || index >= header.recordCount()) {
            throw new IndexOutOfBoundsException("Record index out of range: " + index);
        }

        openChannel();

        boolean acquiredLock = false;
        try {
            if (lock == null || !lock.isValid()) {
                lock = channel.lock(0L, Long.MAX_VALUE, false);
                acquiredLock = true;
            }

            int recordLength = Short.toUnsignedInt(header.recordLength());
            int headerLength = Short.toUnsignedInt(header.headerLength());
            long position = headerLength + ((long) index * recordLength);

            channel.position(position);
            channel.write(ByteBuffer.wrap(new byte[]{(byte) (deleted ? '*' : ' ')}));
            channel.force(true);
            reload();
        } finally {
            if (acquiredLock) {
                unlock();
            }
        }
    }
}