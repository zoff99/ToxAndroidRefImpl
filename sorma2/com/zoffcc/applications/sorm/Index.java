package com.zoffcc.applications.sorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Index
{
    String[] value();

    boolean unique() default false;

    String name() default "";

    @Column.Helpers long helpers() default 1L;
}

