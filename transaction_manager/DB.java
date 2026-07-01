import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


class DB {

    private final ConcurrentHashMap<Integer, Record> storage = new ConcurrentHashMap<>();

    public Record read(int key) {
        return storage.get(key);
    }

    public void write(int key, String value) {
        storage.put(key, new Record(key, value));
    }

    public void print() {
        System.out.println("\n===== DATABASE =====");

        for (Record r : storage.values()) {
            System.out.println(r);
        }

        System.out.println("====================");
    }
}