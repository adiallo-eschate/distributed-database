import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;



class Transaction {

    int id;
    Status status = Status.ACTIVE;

    Map<Integer, String> workspace = new HashMap<>();
    List<ReentrantLock> heldLocks = new ArrayList<>();

    Transaction(int id) {
        this.id = id;
    }
}