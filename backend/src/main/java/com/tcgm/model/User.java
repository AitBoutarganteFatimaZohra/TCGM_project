
package com.tcgm.model;

import com.tcgm.model.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATION : ROLES
    // =========================================================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // =========================================================
    // RELATIONS AVEC SITE
    // =========================================================

    @OneToMany(mappedBy = "chefProjet")
    @Builder.Default
    private List<Site> sitesAsChefProjet = new ArrayList<>();

    @OneToMany(mappedBy = "magasinier")
    @Builder.Default
    private List<Site> sitesAsMagasinier = new ArrayList<>();

    @OneToMany(mappedBy = "agentSaisie")
    @Builder.Default
    private List<Site> sitesAsAgentSaisie = new ArrayList<>();

    @OneToMany(mappedBy = "chefChantier")
    @Builder.Default
    private List<Site> sitesAsChefChantier = new ArrayList<>();

    // =========================================================
    // RELATIONS AVEC DOSSIER POINTAGE
    // =========================================================

    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private List<DossierPointage> dossiersCreated = new ArrayList<>();

    @OneToMany(mappedBy = "validatedBy")
    @Builder.Default
    private List<DossierPointage> dossiersValidated = new ArrayList<>();

    // =========================================================
    // RELATION AVEC JOURNAL
    // =========================================================

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<JournalOperation> journalOperations = new ArrayList<>();

    // =========================================================
    // JPA CALLBACKS
    // =========================================================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // SPRING SECURITY
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : roles) {

            authorities.add(
                new SimpleGrantedAuthority(
                    "ROLE_" + role.getName().name()
                )
            );

            for (Permission permission : role.getPermissions()) {

                authorities.add(
                    new SimpleGrantedAuthority(
                        permission.getName()
                    )
                );
            }
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
