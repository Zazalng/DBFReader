package worldstandard.group.entity;

import worldstandard.group.contracts.DBFEncoding;
import worldstandard.group.contracts.DBFVersion;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;


public final class DBFHeader {
    private final DBFVersion version;
    private final int recordCount;
    private final short headerLength;
    private final short recordLength;
    private final DBFEncoding encoding;
    private final List<DBFField> fields = new ArrayList<>();

    public DBFHeader(SeekableByteChannel ch) throws IOException {
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
        headerBuf.position(29);
        int ldid = Byte.toUnsignedInt(headerBuf.get());
        encoding = DBFEncoding.fromCode(ldid);

        System.out.printf(
                "Version=%s, Date=%02X-%02X-%02X, Records=%d, Header=%d, Record=%d, Encoding=%s%n",
                version.getDescription(), year, month, day, recordCount, headerLength, recordLength, encoding
        );

        // Read fields
        while (true) {
            ByteBuffer fieldBuf = ByteBuffer.allocate(32);
            int bytesRead = ch.read(fieldBuf);
            if (bytesRead != 32) break;
            fieldBuf.flip();

            byte firstByte = fieldBuf.get(0);
            if (firstByte == 0x0D) break; // end of field descriptors

            DBFField field = DBFField.read(fieldBuf, encoding.toCharset());
            fields.add(field);
        }

        ch.position(headerLength); // move to start of records
    }

    public DBFVersion version() { return version; }
    public int recordCount() { return recordCount; }
    public short headerLength() { return headerLength; }
    public short recordLength() { return recordLength; }
    public DBFEncoding encoding() { return encoding; }
    public List<DBFField> fields() { return fields; }

    public List<DBFRow> readRecords(SeekableByteChannel ch) throws IOException {
        List<DBFRow> list = new ArrayList<>(recordCount);
        ByteBuffer recordBuf = ByteBuffer.allocate(recordLength);

        for (int i = 0; i < recordCount; i++) {
            recordBuf.clear();
            int read = ch.read(recordBuf);
            if (read < recordLength) break;

            recordBuf.flip();
            if (recordBuf.get(0) == 0x2A) continue; // deleted record

            try {
                list.add(new DBFRow(fields, recordBuf, encoding.toCharset()));
            } catch (BufferUnderflowException e) {
                System.err.println("[WARN] Record " + i + " shorter than expected (" + read + "/" + recordLength + ")");
                break;
            }
        }

        return List.copyOf(list);
    }
}