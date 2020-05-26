package sample;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MyDataType {
    private SimpleStringProperty property ;
    private SimpleIntegerProperty value ;
    public MyDataType(String property, Integer value) {
        this.property = new SimpleStringProperty(property) ;
        this.value = new SimpleIntegerProperty(value) ;
    }

    public Integer getValue() {

        return value.get();

    }
    public String getProperty() {
        return property.get();
    }

}