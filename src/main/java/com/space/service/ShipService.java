package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ShipService {

    Page<Ship> getAllShips(Specification<Ship> shipSpecification, Pageable pageable);

    List<Ship> getAllShips(Specification<Ship> shipSpecification);

    Ship createShip(Ship ship);
    Ship updateShip(Long id, Ship ship);
    void deleteShip(Long id);
    Ship getShip(Long id);

    Specification<Ship> filterByPlanet(String planet);

    Specification<Ship> filterByName(String name);

    Specification<Ship> filterByShipType(ShipType shipType);

    Specification<Ship> filterByDate(Long after, Long before);

    Specification<Ship> filterByUsage(Boolean isUsed);

    Specification<Ship> filterBySpeed(Double min, Double max);

    Specification<Ship> filterByCrewSize(Integer min, Integer max);

    Specification<Ship> filterByRating(Double min, Double max);

    Long checkId(String id);
}
