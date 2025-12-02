# 🗃️ DBFReader — Lightweight Java `.dbf` File Reader

![Maven Central](https://img.shields.io/maven-central/v/io.github.zazalng/dbf-reader.svg?label=Maven%20Central)
![Java](https://img.shields.io/badge/JDK-8%2B-green)
![License](https://img.shields.io/github/license/Zazalng/DBFReader)
[![Javadoc](https://img.shields.io/badge/javadoc-latest-blue.svg)](https://zazalng.github.io/DBFReader/latest/)

**DBFReader** is a lightweight, zero-dependency library for reading `.DBF` (dBASE/FoxPro/Clipper) database files using Java NIO.  
It’s built for **speed, correctness, and clarity**, fully supporting `DBF III–VII` variants, including modern encodings and date/time types.


---

## ✨ Features

- 🧩 Reads classic `.dbf` files (dBASE III/IV, Visual FoxPro, Clipper)
- ⚙️ Fully implemented in classic LTS Java (`JDK 8`, compatible with JDK 25+)
- 💾 Supports all core field types (`CHARACTER`, `NUMERIC`, `DATE`, `LOGICAL`, `FLOAT`, etc.)
- 🕓 Handles binary and timestamp/date-time encodings
- 🌐 Automatic charset detection from LDID (Language Driver ID)
- 🔢 Accurate numeric parsing with `BigDecimal` (non-scientific)
- 📦 Simple API with immutable object of List/Map and no reflection
- 🚀 Zero external dependencies

---

## 📦 Installation

Add this to your **Maven** project:

```xml
<dependency>
    <groupId>io.github.zazalng</groupId>
    <artifactId>DBFReader</artifactId>
    <version>1.2.0</version>
</dependency>
```

Or using **Gradle**:

```groovy
implementation 'io.github.zazalng:DBFReader:1.2.0'
```

---

## 💡 Example Usage

```java
import io.github.zazalng.dbf.*;

import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        Path dbfFile = Path.of("data/sample.dbf");
        DBF dbf = new DBF(dbfFile);

        System.out.println("Version: " + dbf.getVersion());
        System.out.println("Fields: " + dbf.getFields().size());
        System.out.println("Records: " + dbf.getRecords().size());

        for (var row : dbf.getRecords()) {
            System.out.println(row.asMap());
        }
    }
}
```

Output:

```
Version: dBASE IV
Fields: 5
Records: 120
{ID=1, NAME="Alice", SALARY=53250.00, ACTIVE=true, JOINED=2024-01-03}
```

---

## 🧠 Design Highlights

* Uses `SeekableByteChannel` and `ByteBuffer` for efficient streaming I/O
* Clean, modular structure (`DBF`, `DBFHeader`, `DBFField`, `DBFRow`, `DBFUtils`)
* Each row and field is type-decoded to the most appropriate Java object
* Supports code pages via LDID → Java `Charset` mapping
* Throws minimal checked exceptions, keeping reading logic concise

---

## 📚 API Overview

| Class         | Description                                          |
| ------------- | ---------------------------------------------------- |
| `DBF`         | Main entry point. Loads header and records.          |
| `DBFHeader`   | Parses header metadata and field descriptors.        |
| `DBFField`    | Represents a column definition and its type.         |
| `DBFRow`      | Represents one record (row) as a map of field→value. |
| `DBFUtils`    | Utility for parsing date/time/numeric formats.       |
| `DBFVersion`  | Enum for known dBASE versions.                       |
| `DBFEncoding` | (Optional) Helper for LDID to charset mapping.       |

---

## 🧩 Supported Field Types

| DBF Type         | Java Type       | Example               |
| ---------------- | --------------- | --------------------- |
| `C` (Character)  | `String`        | `"Hello"`             |
| `N` (Numeric)    | `BigDecimal`    | `12345.67`            |
| `F` (Float)      | `BigDecimal`    | `3.14`                |
| `D` (Date)       | `LocalDate`     | `2024-05-21`          |
| `T` (DateTime)   | `LocalDateTime` | `2024-05-21T14:20:00` |
| `L` (Logical)    | `Boolean`       | `true` / `false`      |
| `I` (Integer)    | `Integer`       | `42`                  |
| `B` (Double)     | `BigDecimal`    | `3.1415926`           |
| `M` (Memo/Blob)* | `byte[]`        | *(reserved)*          |

---

## 🧾 License

This project is licensed under the **Apache-2.0 License**

---

## 🤝 Contributing

Pull requests are welcome!
If you encounter a file that doesn’t parse correctly, please open an issue and attach:

* A short description (origin software, e.g. Visual FoxPro / Clipper)
* The first 32 bytes of the header (in hex)
* The full stack trace if applicable

---

## 🔗 Links

* 📦 [Maven Central](https://search.maven.org/artifact/io.github.zazalng/DBFReader)
* 🌐 [GitHub Repository](https://github.com/Zazalng/DBFReader)
* 💬 Author: [@zazalng](https://github.com/Zazalng)

---

### ❤️ Built for clarity, reliability, and open data preservation.