package org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
	Optional<Driver> findByEmail(String email);
	Optional<Driver> findByLicenceNumber(String licenceNumber);
	boolean existsByEmail(String email);
	boolean existsByLicenceNumber(String licenceNumber);
}
