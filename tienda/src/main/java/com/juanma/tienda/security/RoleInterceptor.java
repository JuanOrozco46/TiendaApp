package com.juanma.tienda.security;

import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.exception.ForbiddenException;
import com.juanma.tienda.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Buscar anotación primero en el método, luego a nivel de clase
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            requiresRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }

        if (requiresRole == null) {
            return true;
        }

        Integer userRoleId = (Integer) request.getAttribute("rolId");
        if (userRoleId == null) {
            throw new UnauthorizedException("Acceso no autorizado: token ausente o inválido");
        }

        for (RoleEnum role : requiresRole.value()) {
            if (role.getId().equals(userRoleId)) {
                return true;
            }
        }

        throw new ForbiddenException("Acceso denegado: no cuenta con el rol requerido para esta acción");
    }
}
