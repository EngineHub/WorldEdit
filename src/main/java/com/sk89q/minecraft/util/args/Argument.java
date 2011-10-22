package com.sk89q.minecraft.util.args;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sk89q.minecraft.util.args.spi.Setter;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;


@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface Argument {    
    String usage() default "";
    
    boolean required() default false;
    
    Class<? extends Setter> setter() default Setter.class;
}
