# Learning LSM Storage Engine Architecture

## Purpose

Build a single-node embedded key-value storage engine to understand fundamental storage-system concepts:

* Write paths
* Write-Ahead Logging (WAL)
* MemTables
* SSTables
* Compaction
* Crash recovery

---

## Design Priorities

1. Understand implementation
2. Simplicity
3. Readability
4. Performance

---

## Non-Goals

The following features are intentionally excluded:

* SQL support
* Transactions
* Replication
* Distributed systems
* Compression
* Multiple writers
* Secondary indexes
* Snapshots

---

# System Context

```text
+-------------------+
| Application       |
+-------------------+
          |
          v
+-------------------+
| KV API            |
| read()            |
| write()           |
| delete()          |
+-------------------+
          |
          v
+-------------------+
| LSM Engine        |
+-------------------+
          |
          v
+-------------------+
| File System       |
+-------------------+
          |
          v
+-------------------+
| Disk              |
+-------------------+
```

---

# Public API

The storage engine exposes a minimal synchronous interface.

```cpp
write(key,value)

read(key)

delete(key)
```

## API Behavior

| Operation  | Description         |
| ---------- | ------------------- |
| write(k,v) | Insert/update value |
| read(k)    | Retrieve value      |
| delete(k)  | Mark key as deleted |

Characteristics:

* synchronous operations
* single writer
* no batching
* no snapshots

---

# High-Level Architecture

```text
                    +-------------+
                    | Client API  |
                    +-------------+
                           |
             +-------------+------------+
             |                          |
             v                          v

      +-------------+           +--------------+
      | Write Path  |           | Read Path    |
      +-------------+           +--------------+
             |                          |
             v                          v

      +-------------+           +--------------+
      | WAL         |           | MemTable     |
      | MemTable    |           | SSTables     |
      +-------------+           +--------------+
             |
             v

      +--------------+
      | Flush        |
      +--------------+
             |
             v

      +--------------+
      | SSTables     |
      +--------------+
             |
             v

      +--------------+
      | Compaction   |
      +--------------+
```

---

# Component Design

## Write-Ahead Log (WAL)

### Purpose

Provide durability before in-memory updates.

### Implementation

* append-only text file
* one operation per line
* replayed during recovery

### Record Format

```text
[opType] [key] [value]
```

Examples:

```text
1 user1 john
1 user2 alice
2 user1
```

Operation Types:

| Value | Meaning |
| ----- | ------- |
| 1     | PUT     |
| 2     | DELETE  |

### Write Flow

```text
Append WAL
    ↓
fsync()
    ↓
Continue
```

### Tradeoffs

Pros:

* human-readable
* easy debugging
* easy recovery logic

Cons:

* larger than binary format
* slower parsing
* higher storage overhead

---

## MemTable

### Purpose

Store recent writes in memory.

### Structure

```text
Red-Black Tree
```

Properties:

* sorted keys
* O(log n) insertion
* O(log n) lookup

### Flush Trigger

```text
MemTable size >= 1000 entries
```

### Flush Flow

```text
MemTable Full
        ↓
Freeze MemTable
        ↓
Write SSTable
        ↓
Clear MemTable
```

---

## SSTables

### Purpose

Store immutable sorted data on disk.

### Structure

```text
+------------------+
| Data Blocks      |
+------------------+
| Index            |
+------------------+
| Bloom Filter     |
+------------------+
```

### Index Format

```text
[key][offset]
```

Example:

```text
alice -> 0
bob -> 128
john -> 256
```

Properties:

* immutable
* sorted
* no compression
* supports Bloom filters

---

## Bloom Filter

### Purpose

Reduce unnecessary SSTable reads.

### Flow

```text
Possible match
      ↓
Read SSTable

No match
      ↓
Skip SSTable
```

Properties:

* false positives possible
* false negatives impossible

---

## Compaction

### Strategy

Tiered compaction

### Purpose

Reduce SSTable count and improve read performance.

### Flow

```text
Too many SSTables
        ↓
Select files
        ↓
Merge sorted entries
        ↓
Resolve duplicate keys
        ↓
Handle delete records
        ↓
Write new SSTable
        ↓
Delete old SSTables
```

Compaction rules:

1. Newest key version wins
2. Delete records remove older values
3. Obsolete records are discarded

Tradeoffs:

Pros:

* simpler implementation
* better write throughput

Cons:

* higher read amplification
* increased temporary disk usage

---

# Data Flows

## Write Path

```text
Client write(k,v)
        ↓
Append WAL
        ↓
fsync WAL
        ↓
Insert into MemTable
        ↓
Return success
```

---

## Read Path

```text
Client read(k)
        ↓
Search MemTable
        ↓
Search Level0 SSTables
        ↓
Search Level1 SSTables
        ↓
Return result
```

Read behavior:

```cpp
if(record.opType == 2)
    return NOT_FOUND;
```

---

## Delete Path

Deletes are represented internally using tombstone records.

Tombstone representation:

```text
opType = 2
value = ignored
```

Example:

```text
2 user1
```

Purpose:

The tombstone prevents deleted values from reappearing from older SSTables.

Delete flow:

```text
delete(key)
        ↓
Append DELETE to WAL
        ↓
Insert tombstone into MemTable
        ↓
Flush tombstone into SSTable
        ↓
Compaction observes tombstone
        ↓
Remove obsolete values
```

---

# Recovery

## Purpose

Restore state after crashes.

Startup flow:

```text
Engine startup
       ↓
Locate WAL
       ↓
Replay WAL entries
       ↓
Rebuild MemTable
       ↓
Serve requests
```

Example replay:

WAL:

```text
1 user1 john
1 user2 alice
2 user1
```

Recovered MemTable:

```text
user1 -> DELETE_MARKER
user2 -> alice
```

---

# Storage Layout

```text
db/

WAL.log

level0/
    sst-1.sst
    sst-2.sst

level1/
    sst-3.sst

level2/
```

---

# Failure Assumptions

## Handled

* process crashes
* application crashes
* machine restart
* partial WAL writes
* disk full
* basic file corruption detection

## Not Handled

* hardware failures
* distributed recovery
* replication failures
* torn disk writes
* Byzantine corruption

---

# Future Improvements

Potential future additions:

* binary WAL format
* manifest file
* configurable block sizes
* background compaction thread
* snapshots
* transactions
* multiple writers
* compression

---

# Learning Goals

Expected understanding after implementation:

* why WAL exists
* why LSM systems optimize writes
* why immutable files simplify storage
* why compaction causes write amplification
* why Bloom filters improve reads
* why tombstones are necessary
* tradeoffs between simplicity and performance

---

# Summary

This engine was built for learning purposes and to understand how 
storage works at the lowest level. The above architecture docs were mostly ai-generated with a lot fixs from me. The code is of course 
the main source of truth.
