package com.hotel.api.repository;

import com.hotel.api.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    @Query("SELECT h.brand, COUNT(h) FROM Hotel h WHERE h.brand IS NOT NULL GROUP BY h.brand")
    List<Object[]> countByBrand();

    @Query("SELECT h.address.city, COUNT(h) FROM Hotel h WHERE h.address.city IS NOT NULL GROUP BY h.address.city")
    List<Object[]> countByCity();

    @Query("SELECT h.address.country, COUNT(h) FROM Hotel h WHERE h.address.country IS NOT NULL GROUP BY h.address.country")
    List<Object[]> countByCountry();

    @Query("SELECT a.name, COUNT(DISTINCT h.id) FROM Hotel h JOIN h.amenities a GROUP BY a.name")
    List<Object[]> countByAmenity();
}
