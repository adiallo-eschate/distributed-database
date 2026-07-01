import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


class Logger {

    private List<LogRecord> wal = Collections.synchronizedList(new ArrayList<>());

    public void log(LogRecord record) {

        wal.add(record);

        System.out.println(
            "[LOG] tx=" + record.txId +
            " key=" + record.key +
            " old=" + record.oldValue +
            " new=" + record.newValue
        );
    }

    public List<LogRecord> getLogs() {
        return wal;
    }
}
