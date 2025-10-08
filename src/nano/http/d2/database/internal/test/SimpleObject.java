package nano.http.d2.database.internal.test;

import nano.http.d2.database.internal.SerlClz;

import java.util.Objects;

@SerlClz
public class SimpleObject {
    public int intValue;
    public String stringValue;

    // 反序列化需要默认构造函数
    public SimpleObject() {
    }

    public SimpleObject(int intValue, String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleObject that = (SimpleObject) o;
        return intValue == that.intValue && Objects.equals(stringValue, that.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue, stringValue);
    }

    @Override
    public String toString() {
        return "SimpleObject{" + "intValue=" + intValue + ", stringValue='" + stringValue + '\'' + '}';
    }
}