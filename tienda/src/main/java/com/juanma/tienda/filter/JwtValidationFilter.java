package com.juanma.tienda.filter;

import com.juanma.tienda.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtValidationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isPublicRoute(request)) {
            // Si trae token opcional en ruta pública, podemos extraer atributos
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.validateToken(token)) {
                    request.setAttribute("username", jwtService.getUsername(token));
                    request.setAttribute("userId", jwtService.getUserId(token));
                    request.setAttribute("rolId", jwtService.getRolId(token));
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Acceso no autorizado: token no proporcionado o formato inválido");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            sendUnauthorizedResponse(response, "Acceso no autorizado: token inválido o expirado");
            return;
        }

        request.setAttribute("username", jwtService.getUsername(token));
        request.setAttribute("userId", jwtService.getUserId(token));
        request.setAttribute("rolId", jwtService.getRolId(token));

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRoute(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Rutas de autenticación públicas (/auth/**)
        if (servletPath.startsWith("/auth") || uri.contains("/auth")) {
            return true;
        }

        // Catálogo público: GET /products y GET /products/{id}
        if ("GET".equalsIgnoreCase(method)) {
            String path = servletPath;
            if (path == null || path.isEmpty()) {
                String contextPath = request.getContextPath();
                if (contextPath != null && uri.startsWith(contextPath)) {
                    path = uri.substring(contextPath.length());
                } else {
                    path = uri;
                }
            }

            if ("/products".equals(path) || "/products/".equals(path)) {
                return true;
            }

            // GET /products/{id} con id numérico
            if (path.matches("^/products/\\d+/?$")) {
                return true;
            }
        }

        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = "{\"message\":\"" + message + "\",\"data\":null}";
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
