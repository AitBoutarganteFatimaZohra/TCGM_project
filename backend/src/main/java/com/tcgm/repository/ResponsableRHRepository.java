package com.tcgm.repository;

import com.tcgm.model.ResponsableRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponsableRHRepository extends JpaRepository<ResponsableRH, Long> {

    Optional<ResponsableRH> findByEmail(String email);

    @Query("SELECT r FROM ResponsableRH r WHERE r.departement = :departement")
    List<ResponsableRH> findByDepartement(@Param("departement") String departement);

    @Query("SELECT r FROM ResponsableRH r WHERE r.fonction = :fonction")
    List<ResponsableRH> findByFonction(@Param("fonction") String fonction);
}