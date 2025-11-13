Minimal DBF reader focused on reading and accessing DBF data.

This implementation aims to be conservative and extensible, focusing on core DBF file
features like reading the header, field descriptors, and all data records as a snapshot.
It also supports up to 60 charsets possible for text encoding.

Key Features:
Read header and field descriptors.
Read all records as a snapshot at construction and on reload().

Limitations / Notes:
Memo fields (.dbt/.fpt/.dbt-like memo) are not parsed here (placeholder).
Visual FoxPro-specific binary types, timestamps, and some Level 7 features are partially unsupported.
