package io.simforce.bytezard.coordinator.api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @FileDescription  用于刷新token，所有类上使用该注解的都会刷新token，
 *  仅仅作用域controller
 * @Create 2021-11-18 10:23
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshToken {
}
