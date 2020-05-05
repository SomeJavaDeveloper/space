package com.space.service;

import com.space.exceptions.ShipBadRequestException;
import com.space.exceptions.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService{

    private final ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Page<Ship> getAllShips(Specification<Ship> shipSpecification, Pageable pageable) {
        return shipRepository.findAll(shipSpecification, pageable);
    }

    @Override
    public List<Ship> getAllShips(Specification<Ship> shipSpecification) {
        return shipRepository.findAll(shipSpecification);
    }

    @Override
    public Ship createShip(Ship ship) {
        validateParams(ship);
        ship.setRating(countRating(ship));
        return shipRepository.save(ship);
    }

    @Override
    public Ship updateShip(Long id, Ship ship) {

        Ship reworkedShip = shipRepository.findById(id).get();

        if (ship.getName() != null) reworkedShip.setName(ship.getName());
        if (ship.getPlanet() != null) reworkedShip.setPlanet(ship.getPlanet());
        if (ship.getShipType() != null) reworkedShip.setShipType(ship.getShipType());
        if (ship.getProdDate() != null) reworkedShip.setProdDate(ship.getProdDate());
        if (ship.getSpeed() != null) reworkedShip.setSpeed(ship.getSpeed());
        if (ship.getUsed() != null) reworkedShip.setUsed(ship.getUsed());
        if (ship.getCrewSize() != null) reworkedShip.setCrewSize(ship.getCrewSize());

        validateParams(reworkedShip);
        reworkedShip.setRating(countRating(reworkedShip));

        return shipRepository.saveAndFlush(reworkedShip);
    }

    @Override
    public void deleteShip(Long id) {
        checkId(id.toString());
        shipRepository.deleteById(id);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public Long checkId(String id) {
        Long id1;

        if (id == null || id.isEmpty() || id.equals("0")) throw new ShipBadRequestException();

        try {
            id1 = Long.parseLong(id);
        } catch (NumberFormatException e){
            throw new ShipBadRequestException();
        }

        if (shipRepository.findById(Long.parseLong(id)).orElse(null) == null) throw new ShipNotFoundException();
        return id1;
    }


    private Double countRating(Ship ship){
        Calendar c = new GregorianCalendar();
        c.setTime(ship.getProdDate());
        int y = c.get(Calendar.YEAR);

        double k = ship.getUsed() ? 0.5 : 1;

        BigDecimal r = BigDecimal.valueOf((80 * ship.getSpeed() * k) / (3019 - y + 1));
        return r.setScale(2, RoundingMode.HALF_UP).doubleValue();

    }

    private void validateParams(Ship ship)  {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = sdf.parse("2799-12-31");
            date2 = sdf.parse("3020-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ship.getUsed() == null) ship.setUsed(false);

        if ((ship.getName() == null || (ship.getName().length() < 1 || ship.getName().length() > 50)) ||
           (ship.getPlanet() == null || (ship.getPlanet().length() < 1 || ship.getPlanet().length() > 50)) ||
           (ship.getProdDate() == null || (ship.getProdDate().before(date1) || ship.getProdDate().after(date2))) ||
           (ship.getSpeed() == null || (ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99)) ||
           (ship.getCrewSize() == null || (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999))) throw new ShipBadRequestException();

    }

    @Override
    public Specification<Ship> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        return (root, query, cb) -> planet == null ? null : cb.like(root.get("planet"), "%" + planet + "%");
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, cb) -> shipType == null ? null : cb.equal(root.get("shipType"), shipType);
    }

    @Override
    public Specification<Ship> filterByDate(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) return null;
            if (before == null) return cb.greaterThanOrEqualTo(root.get("prodDate"), new Date(after));
            if (after == null) return cb.lessThanOrEqualTo(root.get("prodDate"), new Date(before));

            return cb.between(root.get("prodDate"), new Date(after), new Date(before));
        };

    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return (root, query, cb) -> {
            if (isUsed == null) return null;
            if (isUsed) return cb.isTrue(root.get("isUsed"));

            else return cb.isFalse(root.get("isUsed"));
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("speed"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("speed"), min);

            return cb.between(root.get("speed"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("crewSize"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("crewSize"), min);

            return cb.between(root.get("crewSize"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByRating(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("rating"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("rating"), min);

            return cb.between(root.get("rating"), min, max);
        };
    }
}
