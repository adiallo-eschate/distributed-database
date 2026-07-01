import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


class LockManager {

    private final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();

    public void acquire(Transaction tx, int key) throws Exception {

        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

        boolean acquired = lock.tryLock(2, TimeUnit.SECONDS);

        if (!acquired) {
            throw new Exception("Could not acquire lock");
        }

        tx.heldLocks.add(lock);
    }
}





