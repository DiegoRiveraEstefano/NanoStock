package cl.diego.NanoStock.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.portfolio.model.CreatorProfile;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {

    Optional<CreatorProfile> findFirstByOrderByIdAsc();
}
