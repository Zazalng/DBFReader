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

import io.github.zazalng.contracts.DBFEncoding;
import io.github.zazalng.contracts.DBFVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the structure and metadata of a DBF file.
 *
 * <p>This class is responsible for reading and parsing the initial 32-byte header
 * block and the subsequent field descriptor array from the DBF file stream. It
 * also calculates the starting position of the data records.</p>
 *
 * @author Zazalng
 * @since 1.0.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache-2.0 license</a>
 */
public final class DBFHeader {
    private final DBFVersion version;
    private final int recordCount;
    private final short headerLength;
    private final short recordLength;
    private final DBFEncoding encoding;
    private final List<DBFField> fields = new ArrayList<>();

    /**
     * Reads and parses the DBF header structure and field descriptors from the given channel.
     * The channel's position is moved to the start of the data records upon successful construction.
     *
     * @param ch The {@code SeekableByteChannel} positioned at the start of the DBF file.
     * @param encoding The {@link DBFEncoding} to use for text fields. If {@code null}, the encoding determined by the LDID byte is used.
     * @throws IOException If an I/O error occurs while reading the channel.
     */
    public DBFHeader(SeekableByteChannel ch, DBFEncoding encoding) throws IOException {
        ByteBuffer headerBuf = ByteBuffer.allocate(32);
        ch.read(headerBuf);
        headerBuf.flip();
        headerBuf.order(ByteOrder.LITTLE_ENDIAN);

        version = DBFVersion.fromByte(headerBuf.get());
        byte year = headerBuf.get();
        byte month = headerBuf.get();
        byte day = headerBuf.get();
        recordCount = headerBuf.getInt();
        headerLength = headerBuf.getShort();
        recordLength = headerBuf.getShort();

        // Skip reserved 17 bytes to reach LDID
        if(encoding == null) {
            headerBuf.position(29);
            int ldid = Byte.toUnsignedInt(headerBuf.get());
            this.encoding = DBFEncoding.fromCode(ldid);
        } else{
            this.encoding = encoding;
        }

        // Read fields
        while (true) {
            ByteBuffer fieldBuf = ByteBuffer.allocate(32);
            int bytesRead = ch.read(fieldBuf);
            if (bytesRead != 32) break;
            fieldBuf.flip();

            byte firstByte = fieldBuf.get(0);
            if (firstByte == 0x0D) break; // end of field descriptors

            DBFField field = DBFField.read(fieldBuf, this.encoding.toCharset());
            fields.add(field);
        }

        ch.position(headerLength); // move to start of records
    }

    /**
     * Gets the version/dialect of the DBF file format.
     * @return The {@link DBFVersion} of the file.
     */
    public DBFVersion version() { return version; }

    /**
     * Gets the total number of data records specified in the header.
     * @return The record count.
     */
    public int recordCount() { return recordCount; }

    /**
     * Gets the total length of the header structure (including field descriptors and the terminal 0x0D), in bytes.
     * @return The header length.
     */
    public short headerLength() { return headerLength; }

    /**
     * Gets the length of a single data record, including the one-byte deletion flag, in bytes.
     * @return The record length.
     */
    public short recordLength() { return recordLength; }

    /**
     * Gets the character encoding defined by the Language Driver ID (LDID) byte in the header.
     * @return The {@link DBFEncoding} of the file.
     */
    public DBFEncoding encoding() { return encoding; }

    /**
     * Gets an unmodifiable list of the {@link DBFField} descriptors parsed from the header.
     * @return An unmodifiable {@code List} of {@link DBFField} objects.
     */
    public List<DBFField> fields() { return Collections.unmodifiableList(fields); }

    /**
     * Reads all data records from the current position of the channel until the record count is reached.
     *
     * @param ch The {@code SeekableByteChannel}, assumed to be positioned at the start of the data records.
     * @return A list of {@link DBFRow} objects representing the data records.
     * @throws IOException If an I/O error occurs during reading.
     */
    public List<DBFRow> readRecords(SeekableByteChannel ch) throws IOException {
        List<DBFRow> list = new ArrayList<>(recordCount);
        ByteBuffer recordBuf = ByteBuffer.allocate(recordLength);

        for (int i = 0; i < recordCount; i++) {
            recordBuf.clear();
            int read = ch.read(recordBuf);
            if (read < recordLength) break;

            recordBuf.flip();
            if (recordBuf.get(0) == 0x2A) continue; // deleted record

            list.add(new DBFRow(fields, recordBuf, encoding.toCharset()));
        }

        return Collections.unmodifiableList(list);
    }
}