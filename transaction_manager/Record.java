

class Record {

    int key;
    String value;

    Record(int key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{key=" + key + ", value=" + value + "}";
    }
}