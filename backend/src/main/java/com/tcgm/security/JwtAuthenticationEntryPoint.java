package com.tcgm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcgm.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Échec d'authentification: {}", authException.getMessage());
        log.debug("URL de la requête: {}", request.getRequestURI());

        // Construire la réponse d'erreur
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status("UNAUTHORIZED")
            .message("Accès non autorisé. Veuillez vous authentifier.")
            .errorCode("AUTH-001")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .details(authException.getMessage())
            .build();

        // Configurer la réponse
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Écrire la réponse
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}