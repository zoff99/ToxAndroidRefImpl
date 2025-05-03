package com.zoffcc.applications.sorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Table
{
    String value() default "";

    String[] constraints() default {};

    Index[] indexes() default {};

    String schemaClassName() default "";

    String relationClassName() default "";

    String updaterClassName() default "";

    String deleterClassName() default "";

    String selectorClassName() default "";

    String associationConditionClassName() default "";
}
