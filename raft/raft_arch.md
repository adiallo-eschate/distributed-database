# Raft Layer Architecture (Thin Slice)

## Purpose
Implements Raft leader election only:
- leader election
- RequestVote RPC
- term management
- majority voting

No log replication yet.

---

## Core Responsibility

The Raft layer is a per-node state machine that decides:
- who is leader
- when elections start
- how votes are granted
- how terms update

It does NOT handle networking directly.

---

## Main Class: RaftNode

### State
- currentTerm
- votedFor
- state: FOLLOWER | CANDIDATE | LEADER
- leaderId

### Election State
- votesReceived (Set)
- lastHeartbeat timestamp

---

## State Transitions

### FOLLOWER → CANDIDATE
Triggered by election timeout.

Actions:
- increment term
- vote for self
- request votes from peers

---

### CANDIDATE → LEADER
Condition:
- majority votes received

Actions:
- become leader
- start leadership duties (future: heartbeats)

---

### ANY → FOLLOWER
Triggered when:
- higher term is seen

Actions:
- update term
- reset votedFor

---

## RPC Handled

### RequestVote

Input:
- candidateId
- term

Rules:
- reject if term < currentTerm
- accept if not voted or votedFor == candidateId
- update votedFor

Output:
- vote granted or denied

---

## Timing

- election timeout checks run periodically (~500ms)
- triggers election if no heartbeat received

---

## Not Included

- AppendEntries
- log replication
- persistence
- cluster reconfiguration

---

## Integration

Raft is invoked by server layer:
- handleRequestVote
- handleVoteResponse (from network layer)