package com.bigobooks.purchases_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.bigobooks.util.exception.UnauthorizedException;

/**
 * Acceso a los datos del usuario autenticado que viajan en el token JWT.
 */
@Component
public class AuthenticatedUser {

    private static final String WAREHOUSE_ID_CLAIM = "warehouseId";
    private static final String WAREHOUSE_ID_CLAIM_SNAKE_CASE = "warehouse_id";

    /**
     * Obtiene el almacen asignado al usuario desde el token. Se aceptan los
     * claims {@code warehouseId} y {@code warehouse_id} para mantener
     * compatibilidad con emisores existentes.
     */
    public Long requireWarehouseId() {
        Jwt jwt = requireJwt();
        Long warehouseId = asLong(jwt.getClaim(WAREHOUSE_ID_CLAIM));
        if (warehouseId == null) {
            warehouseId = asLong(jwt.getClaim(WAREHOUSE_ID_CLAIM_SNAKE_CASE));
        }
        if (warehouseId == null) {
            throw new UnauthorizedException(
                    "El token no contiene el identificador de almacen (warehouseId) del usuario");
        }
        return warehouseId;
    }

    private Jwt requireJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No hay un usuario autenticado");
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new UnauthorizedException("El usuario autenticado no tiene un token JWT");
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ex) {
                throw new UnauthorizedException("El claim warehouseId del token no es un numero valido");
            }
        }
        throw new UnauthorizedException("El claim warehouseId del token no es valido");
    }
}
