package net.techcable.phanatic.ftweaks.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
    public String value();
}
