import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


class TransactionManager {

    private DB db;
    private Logger logger;
    private LockManager lockManager;

    TransactionManager(DB db, Logger logger, LockManager lockManager) {
        this.db = db;
        this.logger = logger;
        this.lockManager = lockManager;
    }

    public String read(Transaction tx, int key) {

        if (tx.workspace.containsKey(key)) {
            return tx.workspace.get(key);
        }

        Record record = db.read(key);

        return record == null ? null : record.value;
    }

    public void write(Transaction tx, int key, String newValue) throws Exception {

        lockManager.acquire(tx, key);

        Record oldRecord = db.read(key);

        String oldValue = oldRecord == null ? null : oldRecord.value;

        logger.log(new LogRecord(tx.id, key, oldValue, newValue));

        tx.workspace.put(key, newValue);
    }

    public void commit(Transaction tx) {

        for (Map.Entry<Integer, String> entry : tx.workspace.entrySet()) {
            db.write(entry.getKey(), entry.getValue());
        }

        tx.status = Status.COMMITTED;

        releaseLocks(tx);

        System.out.println("TX " + tx.id + " COMMITTED");
    }

    public void rollback(Transaction tx) {

        tx.workspace.clear();

        tx.status = Status.ABORTED;

        releaseLocks(tx);

        System.out.println("TX " + tx.id + " ABORTED");
    }

    private void releaseLocks(Transaction tx) {

        for (ReentrantLock lock : tx.heldLocks) {
            lock.unlock();
        }

        tx.heldLocks.clear();
    }

    public void recover() {

        System.out.println("\nRECOVERY...");

        for (LogRecord log : logger.getLogs()) {

            Record current = db.read(log.key);

            if (current == null || !Objects.equals(current.value, log.newValue)) {

                db.write(log.key, log.oldValue);

                System.out.println("Undo tx " + log.txId);
            }
        }
    }
}
