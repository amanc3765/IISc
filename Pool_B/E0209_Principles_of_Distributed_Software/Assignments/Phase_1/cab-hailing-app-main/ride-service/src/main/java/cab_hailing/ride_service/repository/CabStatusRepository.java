package cab_hailing.ride_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import cab_hailing.ride_service.model.CabStatus;

public interface CabStatusRepository extends JpaRepository<CabStatus, Long> {

	public List<CabStatus> findTop3ByCurrPosGreaterThanEqualAndMajorStateAndMinorStateOrderByCurrPosAsc(Long currPos,
			String majorState, String minorState);

	public List<CabStatus> findTop3ByCurrPosLessThanAndMajorStateAndMinorStateOrderByCurrPosDesc(Long currPos,
			String majorState, String minorState);
	
	public List<CabStatus> findAllByMinorState(String minorState);
	
	public List<CabStatus> findAllByMajorState(String majorState);
	
}
