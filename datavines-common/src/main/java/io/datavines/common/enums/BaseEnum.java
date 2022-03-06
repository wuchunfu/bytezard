package io.datavines.common.enums;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public interface BaseEnum {

    String DEFAULT_CODE_NAME = "code";

    String DEFAULT_DESCRIPTION_NAME = "description";

    /**
     * get code
     * @return String
     */
    public default Integer getCode() {
        Field field = getDeclaredField(this.getClass(), DEFAULT_CODE_NAME);
        if (field == null){
            return null;
        }

        try {
            field.setAccessible(true);
            return Integer.parseInt(field.get(this).toString());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get description
     * @return String
     */
    default String getDescription() {
        Field field = getDeclaredField(this.getClass(), DEFAULT_DESCRIPTION_NAME);
        if (field == null){
            return null;
        }

        try {
            field.setAccessible(true);
            return field.get(this).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T valueOfEnum(Class<T> enumClass, Integer code) {
        if (code == null){
            throw  new IllegalArgumentException("BaseEnum value should not be null");
        }

        if (enumClass.isAssignableFrom(BaseEnum.class)){
            throw new IllegalArgumentException("illegal BaseEnum type");
        }

        T[] enums = enumClass.getEnumConstants();
        for (T t: enums) {
            BaseEnum baseEnum = (BaseEnum)t;
            if (baseEnum.getCode().equals(code)){
                return (T) baseEnum;
            }

        }
        throw new IllegalArgumentException("cannot parse integer: " + code + " to " + enumClass.getName());
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T valueOfEnum(Class<T> enumClass, String description) {
        if (StringUtils.isEmpty(description)){
            throw  new IllegalArgumentException("BaseEnum value should not be null");
        }

        if (enumClass.isAssignableFrom(BaseEnum.class)){
            throw new IllegalArgumentException("illegal BaseEnum type");
        }

        T[] enums = enumClass.getEnumConstants();
        for (T t: enums) {
            BaseEnum baseEnum = (BaseEnum)t;
            if (baseEnum.getDescription().equals(description)){
                return (T) baseEnum;
            }

        }

        throw new IllegalArgumentException("cannot parse integer: " + description + " to " + enumClass.getName());
    }

    static Field getDeclaredField(Object object, String filedName) {

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                //Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

}
