package de.muv1n.muvbot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME = "requestStartTime";
    private static final String HEALTHCHECK_URI = "/api/healthcheck";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HEALTHCHECK_URI.equals(request.getRequestURI())) {
            return true;
        }

        logger.info("Incoming request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (HEALTHCHECK_URI.equals(request.getRequestURI())) {
            return;
        }

        long duration = System.currentTimeMillis() - (long) request.getAttribute(START_TIME);
        logger.info("{} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        logger.info("Response: {}", response.toString());
    }
}