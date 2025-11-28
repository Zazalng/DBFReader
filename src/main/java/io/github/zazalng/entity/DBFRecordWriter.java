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
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Helper responsible for converting in-memory representations of DBF rows into their
 * binary layout suitable for writing back to disk.
 */
public final class DBFRecordWriter {
    private final List<DBFField> fields;
    private final Charset charset;
    private final ThreadLocal<ByteBuffer> buffer;

    public DBFRecordWriter(List<DBFField> fields, Charset charset, int recordLength) {
        this.fields = fields;
        this.charset = charset;
        this.buffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(recordLength).order(ByteOrder.BIG_ENDIAN));
    }

    /**
     * Encodes the provided row values into the internal buffer and returns it ready for writing.
     *
     * @param values        Map of field name to value.
     * @param deletedMarker Whether to mark the record as deleted.
     * @return Buffer positioned at zero containing the encoded record.
     */
    public ByteBuffer encode(Map<String, Object> values, boolean deletedMarker) {
        ByteBuffer localBuffer = buffer.get();
        localBuffer.clear();
        localBuffer.put((byte) (deletedMarker ? '*' : ' '));
        for (DBFField field : fields) {
            Object value = values != null ? values.get(field.getName()) : null;
            byte[] encoded = field.encode(value, charset);
            localBuffer.put(encoded, 0, encoded.length);
        }
        localBuffer.flip();
        return localBuffer;
    }
}