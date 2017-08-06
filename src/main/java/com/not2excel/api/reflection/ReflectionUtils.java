package com.not2excel.api.reflection;

import java.lang.reflect.Field;

/**
 * @author Richmond Steele
 * @since 12/17/13 All rights Reserved Please read included LICENSE file
 */
public class ReflectionUtils {

    public static Object getField(final Object object, final String field)
    throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Class<?> c = object.getClass();
        final Field objectField = c.getDeclaredField(field);
        objectField.setAccessible(true);
        final Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }
}
