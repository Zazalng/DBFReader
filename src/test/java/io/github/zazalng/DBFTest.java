package io.github.zazalng;

import io.github.zazalng.contracts.DBFEncoding;
import io.github.zazalng.entity.DBFField;
import io.github.zazalng.entity.DBFRow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for {@link DBF} reading functionality.
 *
 * The tests create a minimal DBF file on disk (in a temporary file) containing:
 * - a 32-byte header with record count and header length
 * - one 32-byte field descriptor for a CHARACTER field of length 5
 * - field descriptor terminator 0x0D
 * - one record containing the deletion flag and field bytes
 */
public class DBFTest {

    @Test
    public void testReadSimpleDBF() throws IOException {
        Path tmp = Files.createTempFile("test", ".dbf");
        // Open with CREATE and TRUNCATE_EXISTING to ensure a fresh file is written.
        try (SeekableByteChannel ch = Files.newByteChannel(tmp, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // Build a minimal header (32 bytes)
            ByteBuffer header = ByteBuffer.allocate(32);
            // DBF uses little-endian for multi-byte numeric fields in the header.
            header.order(ByteOrder.LITTLE_ENDIAN);
            header.put((byte)0x03); // version
            header.put((byte)0); // year
            header.put((byte)0); // month
            header.put((byte)0); // day
            header.putInt(1); // record count = 1
            header.putShort((short) (32 + 32 + 1)); // header length: 32 + one field desc (32) + terminator
            header.putShort((short) (1 + 5)); // record length: delete flag + 5 bytes
            header.position(29);
            header.put((byte) DBFEncoding.CP1252_03.getCode()); // LDID in header
            // pad remaining
            header.position(32);
            header.flip();
            ch.write(header);

            // Field descriptor (32 bytes) for a 5-char field named NAME
            ByteBuffer field = ByteBuffer.allocate(32);
            // Ensure field descriptor uses little-endian so putInt(0) writes expected bytes.
            field.order(ByteOrder.LITTLE_ENDIAN);
            byte[] nameBytes = new byte[11];
            byte[] bname = "NAME".getBytes(DBFEncoding.CP1252_03.toCharset());
            System.arraycopy(bname, 0, nameBytes, 0, bname.length);
            field.put(nameBytes);
            field.put((byte)'C'); // type CHARACTER
            field.putInt(0); // field address
            field.put((byte)5); // length
            field.put((byte)0); // decimal count
            // fill reserved
            while (field.position() < 32) field.put((byte)0);
            field.flip();
            ch.write(field);

            // Field descriptor terminator
            ch.write(ByteBuffer.wrap(new byte[]{0x0D}));

            // pad to header length
            long pos = ch.position();
            long headerLen = 32 + 32 + 1; // 32 header + 1 field + terminator
            if (pos < headerLen) {
                ch.write(ByteBuffer.allocate((int)(headerLen - pos)));
            }

            // One record: deletion flag (0x20), then 5 ASCII chars 'Hello'
            ByteBuffer record = ByteBuffer.allocate(1 + 5);
            record.put((byte)0x20);
            record.put("Hello".getBytes(DBFEncoding.CP1252_03.toCharset()));
            record.flip();
            ch.write(record);
        }

        // Now read using DBF
        DBF dbf = new DBF(tmp);
        assertNotNull(dbf.getHeader());
        assertEquals(1, dbf.getHeader().recordCount());
        List<DBFField> fields = dbf.getFields();
        assertEquals(1, fields.size());
        assertEquals("NAME", fields.get(0).getName());

        List<DBFRow> rows = dbf.getRecords();
        assertEquals(1, rows.size());
        DBFRow row = rows.get(0);
        assertEquals("Hello", row.get("NAME"));

        // test reload uses same encoding
        dbf.reload();
        assertEquals(1, dbf.getRecords().size());

        Files.deleteIfExists(tmp);
    }
}
