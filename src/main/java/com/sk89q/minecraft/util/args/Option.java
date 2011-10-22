package com.sk89q.minecraft.util.args;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sk89q.minecraft.util.args.spi.Setter;

@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface Option {
    String[] aliases();
        
    boolean required() default false;
    
    String usage() default "";
    
    Class<? extends Setter> setter() default Setter.class;
}
