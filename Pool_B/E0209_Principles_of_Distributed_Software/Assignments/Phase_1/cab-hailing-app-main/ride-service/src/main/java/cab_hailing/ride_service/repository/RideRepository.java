package cab_hailing.ride_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cab_hailing.ride_service.model.CabStatus;
import cab_hailing.ride_service.model.Ride;

public interface RideRepository extends JpaRepository<Ride, Long>{
	public Ride findTopByCabStatusAndRideState(CabStatus cabStatus, String rideState) ;
}
