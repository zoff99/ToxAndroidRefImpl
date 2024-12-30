package com.zoffcc.applications.sorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface OnConflict
{
    int NONE = 0;
    int ROLLBACK = 1;
    int ABORT = 2;
    int FAIL = 3;
    int IGNORE = 4;
    int REPLACE = 5;
}
