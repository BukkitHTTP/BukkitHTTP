package nano.http.d2.database.internal.test;

import nano.http.d2.database.internal.SerlClz;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SerlClz
public class ComplexObject {
    public Void theVoid;
    public byte byteValue;
    public Short shortValue;
    public int intValue;
    public Long longValue;
    public float floatValue;
    public double doubleValue;
    public boolean boolValue;
    public char charValue;

    public String stringValue;
    public SimpleObject simpleObject;
    public int[] intArray;
    public String[] stringArray;
    public List<String> stringList;
    public Map<String, SimpleObject> objectMap;
    public TestEnum enumValue;

    // 反序列化需要默认构造函数
    public ComplexObject() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexObject that = (ComplexObject) o;
        return byteValue == that.byteValue && Objects.equals(shortValue, that.shortValue) && intValue == that.intValue && Objects.equals(longValue, that.longValue) && Float.compare(that.floatValue, floatValue) == 0 && Double.compare(that.doubleValue, doubleValue) == 0 && boolValue == that.boolValue && charValue == that.charValue && Objects.equals(stringValue, that.stringValue) && Objects.equals(simpleObject, that.simpleObject) && Arrays.equals(intArray, that.intArray) && Arrays.equals(stringArray, that.stringArray) && Objects.equals(stringList, that.stringList) && Objects.equals(objectMap, that.objectMap) && enumValue == that.enumValue;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(byteValue, shortValue, intValue, longValue, floatValue, doubleValue, boolValue, charValue, stringValue, simpleObject, stringList, objectMap, enumValue);
        result = 31 * result + Arrays.hashCode(intArray);
        result = 31 * result + Arrays.hashCode(stringArray);
        return result;
    }

    @Override
    public String toString() {
        return "ComplexObject{" +
                "byteValue=" + byteValue +
                ", shortValue=" + shortValue +
                ", intValue=" + intValue +
                ", longValue=" + longValue +
                ", floatValue=" + floatValue +
                ", doubleValue=" + doubleValue +
                ", boolValue=" + boolValue +
                ", charValue=" + charValue +
                ", stringValue='" + stringValue + '\'' +
                ", simpleObject=" + simpleObject +
                ", intArray=" + Arrays.toString(intArray) +
                ", stringArray=" + Arrays.toString(stringArray) +
                ", stringList=" + stringList +
                ", objectMap=" + objectMap +
                ", enumValue=" + enumValue +
                '}';
    }
}