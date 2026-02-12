package com.valenciaBank.valenciaBank.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter - Tests unitarios del filtro de autenticación")
class JwtFilterTest {

    private JwtFilter jwtFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter();
        // Configurar clave JWT para tests
        Jwt jwt = new Jwt();
        jwt.setSecretKey("TestSecretKeyForJWT2026");
    }

    @Test
    @DisplayName("Rechaza petición sin header Authorization")
    void rechazaSinAuthorization() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no proporcionado o inválido");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Rechaza petición sin prefijo Bearer")
    void rechazaSinBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token123");

        jwtFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no proporcionado o inválido");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Rechaza petición con token inválido")
    void rechazaTokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido-123");

        jwtFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado o inválido");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Permite petición con token válido")
    void permiteTokenValido() throws Exception {
        String validToken = Jwt.generateToken("12345678A");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
