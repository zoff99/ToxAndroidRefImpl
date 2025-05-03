package com.zoffcc.applications.sorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface PrimaryKey
{
    boolean autoincrement() default false;

    boolean auto() default true;

    @OnConflict int onConflict() default 0;
}