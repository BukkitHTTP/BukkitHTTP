package nano.http.d2.database.csv;

import java.lang.reflect.Field;

/*
 * @author huzpsb
 * @description 将一个成员全部为私有的类转换为CSV表格的一行
 * */

public class Reflectior<T> {
    private final Field[] allFields;

    public Reflectior(Class<?> clazz) {
        try {
            allFields = clazz.getDeclaredFields();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String serl(T o) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            for (Field f : allFields) {
                if (f.getName().equals("serialVersionUID")) {
                    continue;
                }
                f.setAccessible(true);
                sb.append(String.valueOf(f.get(o)).replaceAll("\"", "^"));
                sb.append("\",\"");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage() + "\n";
        }
    }

    public String title(Localizer l) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            for (Field f : allFields) {
                if (f.getName().equals("serialVersionUID")) {
                    continue;
                }
                sb.append(l.transform(f.getName()));
                sb.append("\",\"");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage() + "\n";
        }
    }
}
