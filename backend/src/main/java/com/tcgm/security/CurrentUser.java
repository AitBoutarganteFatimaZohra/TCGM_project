package com.tcgm.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Annotation pour injecter l'utilisateur actuellement authentifié dans les contrôleurs.
 * Utilisation: @CurrentUser UserPrincipal user
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}