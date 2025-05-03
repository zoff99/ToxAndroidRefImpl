package com.zoffcc.applications.sorm;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Column
{
    String value() default "";

    boolean indexed() default false;

    boolean unique() default false;

    @OnConflict int uniqueOnConflict() default 0;

    Column.ForeignKeyAction onDelete() default Column.ForeignKeyAction.CASCADE;

    Column.ForeignKeyAction onUpdate() default Column.ForeignKeyAction.CASCADE;

    String defaultExpr() default "";

    Column.Collate collate() default Column.Collate.BINARY;

    String storageType() default "";

    @Column.Helpers long helpers() default 1L;

    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    public @interface Helpers
    {
        long NONE = 0L;
        long AUTO = 1L;
        long CONDITION_EQ = 2L;
        long CONDITION_NOT_EQ = 4L;
        long CONDITION_IS_NULL = 8L;
        long CONDITION_IS_NOT_NULL = 16L;
        long CONDITION_IN = 32L;
        long CONDITION_NOT_IN = 64L;
        long CONDITION_GLOB = 128L;
        long CONDITION_NOT_GLOB = 256L;
        long CONDITION_LIKE = 512L;
        long CONDITION_NOT_LIKE = 1024L;
        long CONDITION_LT = 2048L;
        long CONDITION_LE = 4096L;
        long CONDITION_GT = 8192L;
        long CONDITION_GE = 16384L;
        long CONDITION_BETWEEN = 32768L;
        long CONDITIONS = 65534L;
        long ORDER_IN_ASC = 65536L;
        long ORDER_IN_DESC = 131072L;
        long ORDERS = 196608L;
        long PLUCK = 262144L;
        long MIN = 524288L;
        long MAX = 1048576L;
        long SUM = 2097152L;
        long AVG = 4194304L;
        long AGGREGATORS = 7864320L;
        long ALL = 8388606L;
    }

    public static enum ForeignKeyAction
    {
        NO_ACTION, RESTRICT, SET_NULL, SET_DEFAULT, CASCADE;

        private ForeignKeyAction()
        {
        }
    }

    public static enum Collate
    {
        BINARY, NOCASE, RTRIM;

        private Collate()
        {
        }
    }
}
