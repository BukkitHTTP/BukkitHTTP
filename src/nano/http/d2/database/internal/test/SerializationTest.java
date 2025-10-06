package nano.http.d2.database.internal.test;

import nano.http.d2.database.internal.SerlBridge;
import nano.http.d2.database.internal.SerlBridgeResult;
import nano.http.d2.json.NanoJSON;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationTest {
    public static void main(String[] args) {
        System.out.println("Starting serialization test...");

        // 1. 创建一个复杂的测试对象
        ComplexObject original = new ComplexObject();
        original.byteValue = 123;
        original.shortValue = 12345;
        original.intValue = 1234567890;
        original.longValue = 1234567890123456789L;
        original.floatValue = 123.456f;
        original.doubleValue = 12345.6789;
        original.boolValue = true;
        original.charValue = 'Z';
        original.stringValue = "Hello, Serialization!";
        original.simpleObject = new SimpleObject(99, "Nested Object");
        original.intArray = new int[]{1, 2, 3, 4, 5};
        original.stringArray = new String[]{"a", "b", "c"};
        original.stringList = new LinkedList<>(Arrays.asList("List1", "List2"));
        original.objectMap = new ConcurrentHashMap<>();
        original.objectMap.put("key1", new SimpleObject(1, "MapValue1"));
        original.objectMap.put("key2", new SimpleObject(2, "MapValue2"));
        original.enumValue = TestEnum.B;

        System.out.println("Original object: " + original);
        SerlBridge serlBridge = new SerlBridge("LET ME IN");


        try {
            // 2. 序列化对象
            byte[] data = serlBridge.serialize(original);
            System.out.println("Serialization successful. Data length: " + data.length + " bytes.");
            // 3. 反序列化对象
            SerlBridgeResult deserializedObject = serlBridge.deserialize(data);
            // 4. 验证结果
            if (deserializedObject.obj instanceof ComplexObject deserialized) {
                if (original.equals(deserialized)) {
                    System.out.println("\nSUCCESS");
                } else {
                    System.err.println("\nFAILURE XXXXXXXX");
                }
            } else {
                System.err.println("\nFAILURE: Deserialized object is not of type ComplexObject.");
            }

        } catch (IOException e) {
            System.err.println("\nAn error occurred during serialization/deserialization test.");
            e.printStackTrace();
        }

        System.out.println(new NanoJSON(new SimpleObject()));
    }
}
