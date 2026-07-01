import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;




public class Main {

    public static void main(String[] args) {

        DB db = new DB();
        Logger logger = new Logger();
        LockManager lockManager = new LockManager();

        TransactionManager tm = new TransactionManager(db, logger, lockManager);

        db.write(1, "Jon Snow");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {

            Transaction tx = new Transaction(1);

            try {

                tm.write(tx, 1, "King Jon");

                Thread.sleep(1000);

                tm.commit(tx);

            } catch (Exception e) {
                tm.rollback(tx);
            }
        });

        executor.submit(() -> {

            Transaction tx = new Transaction(2);

            try {

                String value = tm.read(tx, 1);

                System.out.println("TX2 read: " + value);

                tm.commit(tx);

            } catch (Exception e) {
                tm.rollback(tx);
            }
        });

        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.print();

        tm.recover();

        db.print();
    }
}