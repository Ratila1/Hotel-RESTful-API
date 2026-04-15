package com.hotel.api.specification;

import com.hotel.api.entity.Amenity;
import com.hotel.api.entity.Hotel;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class HotelSpecification {

    private HotelSpecification() {}

    public static Specification<Hotel> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Hotel> hasBrand(String brand) {
        return (root, query, cb) ->
                brand == null ? null : cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%");
    }

    public static Specification<Hotel> hasCity(String city) {
        return (root, query, cb) ->
                city == null ? null : cb.like(cb.lower(root.get("address").get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Hotel> hasCountry(String country) {
        return (root, query, cb) ->
                country == null ? null : cb.like(cb.lower(root.get("address").get("country")), "%" + country.toLowerCase() + "%");
    }

    public static Specification<Hotel> hasAnyAmenity(List<String> amenities) {
        return (root, query, cb) -> {
            if (amenities == null || amenities.isEmpty()) {
                return null;
            }
            Join<Hotel, Amenity> amenityJoin = root.join("amenities");
            query.distinct(true);
            return amenityJoin.get("name").in(amenities);
        };
    }
}
