package io.simforce.bytezard.coordinator.api.aop;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.simforce.bytezard.coordinator.api.entity.ResultMap;
import io.simforce.bytezard.coordinator.api.enums.BytezardApiException;
import io.simforce.bytezard.coordinator.api.enums.Status;
import io.simforce.bytezard.coordinator.utils.TokenManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(Integer.MAX_VALUE)
public class RefreshTokenAop {

    @Resource
    private TokenManager tokenManager;

    /**
     * 所有类上使用RefreshToken注解的都需要走该切片
     */
    @Pointcut("@within(RefreshToken)")
    public void pointCut() {

    }

    /**
     * 在切片内取出方法的参数，找到request，然后执行切片，将执行结果封装在ResponseEntity内部
     *
     * @param point
     * @return
     */
    @Around(value = "pointCut()")
    public Object doAroundReturningAdvice(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        BindingResult bindingResult = null;
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                bindingResult = (BindingResult) arg;
            }
        }
        // check bindingResult
        if (!Objects.isNull(bindingResult)) {
            Object checkResult = checkParam(bindingResult, request);
            if (!Objects.isNull(checkResult)) {
                return checkResult;
            }
        }

        Object result = null;
        try {
            result = point.proceed(args);
        } catch (Throwable throwable) {
            log.info(throwable.getMessage(), throwable);

            if (throwable instanceof BytezardApiException) {
                BytezardApiException exception = (BytezardApiException) throwable;
                return ResponseEntity.ok(new ResultMap(tokenManager)
                        .failAndRefreshToken(request)
                        .fail(exception.getStatus().getCode())
                        .message(exception.getMessage()));
            }

            return ResponseEntity.ok(new ResultMap(tokenManager).failAndRefreshToken(request).payload(throwable.toString()));
        }

        return ResponseEntity.ok(new ResultMap(tokenManager).successAndRefreshToken(request).payload(result));
    }

    protected String getValidErrorMessage(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        fieldErrors.forEach(fieldError -> {
            //日志打印不符合校验的字段名和错误提示
            sb.append(fieldError.getDefaultMessage()).append(System.lineSeparator());
        });

        return sb.toString();
    }

    protected Object checkParam(BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult != null && bindingResult.hasErrors()) {
            return ResponseEntity.ok(new ResultMap(tokenManager)
                    .failAndRefreshToken(request)
                    .fail(Status.PARAM_CHECK_ERROR.getCode())
                    .message(getValidErrorMessage(bindingResult)));
        }

        return null;
    }
}
