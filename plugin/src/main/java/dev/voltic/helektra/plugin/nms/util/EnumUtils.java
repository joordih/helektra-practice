package dev.voltic.helektra.plugin.nms.util;

public class EnumUtils {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <E extends Enum<E>> E getEnumConstant(Class<?> enumClass, String name) {
        return (E) Enum.valueOf((Class) enumClass.asSubclass(Enum.class), name);
    }
}
