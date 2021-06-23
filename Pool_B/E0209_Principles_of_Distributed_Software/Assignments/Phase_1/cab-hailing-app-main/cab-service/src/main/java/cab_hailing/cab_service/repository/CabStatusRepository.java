package cab_hailing.cab_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cab_hailing.cab_service.model.CabStatus;

public interface CabStatusRepository extends JpaRepository<CabStatus, Long>{

}
