package ma.youcode.covoiturage.repository;

import ma.youcode.covoiturage.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByNumeroPermisAndIdNot(String numeroPermis, Long id);

    List<Conducteur> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nom,
            String email
    );
}