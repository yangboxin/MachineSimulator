package panel2.panel1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CacheEntry {
    private final StringProperty tag;
    private final StringProperty value;

    public CacheEntry(String tag, String value) {
        this.tag = new SimpleStringProperty(tag);
        this.value = new SimpleStringProperty(value);
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag.set(tag);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
