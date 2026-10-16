package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.ConfiguracionCarteraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionCarteraJpaRepository extends JpaRepository<ConfiguracionCarteraEntity, Short> {
}
