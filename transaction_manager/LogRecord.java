

class LogRecord {

    int txId;
    int key;

    String oldValue;
    String newValue;

    LogRecord(int txId, int key, String oldValue, String newValue) {
        this.txId = txId;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
