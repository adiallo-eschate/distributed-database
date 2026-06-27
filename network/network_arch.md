# Network Layer Architecture

## Purpose

Handles message transport between nodes using sockets.

It is a dumb transport layer and is NOT aware of Raft rules.

---

## Core Classes

### Client
Sends RPC requests.

Responsibilities:
- serialize Packet → String
- send message over socket
- receive response
- optionally forward result to RaftNode (current workaround)

---

### Server
Receives RPC requests.

Responsibilities:
- accept socket connections
- read message string
- route request to handler
- return response string

---

## Message Flow

Client.connect()
  → socket send
  → Server.handleClient()
  → parse string
  → route handler
  → generate response
  → send response back
  → Client reads response
  → (optional) forward to Raft

---

## Message Format (Current)

TYPE:nodeId:seqNum:term:leaderId:prevLogIndex:prevLogTerm:commitIndex

Optional DISCOVER suffix:
-nodeId:ip:port,nodeId:ip:port

---

## Routing Logic

Server uses string matching:
- contains("DISCOVER")
- contains("REQUESTVOTE")
- contains("APPENDENTRIES")

---

## Handlers

### DISCOVER
- updates membership list

### REQUESTVOTE
- processes vote request
- returns vote decision

### APPENDENTRIES
- placeholder

---

## Limitations

- string-based protocol (fragile)
- no schema validation
- contains() routing risks false matches
- client must parse responses manually
- Raft logic partially coupled via response forwarding hack

---

## Relationship to Raft

Network layer:
- transports messages
- knows nothing about consensus

Raft layer:
- decides consensus state
- ignores transport details

---

## System Model

Client → Server → Handler → Response → Client → Raft (optional)