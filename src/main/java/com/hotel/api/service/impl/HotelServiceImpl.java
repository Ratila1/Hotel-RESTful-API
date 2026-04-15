package com.hotel.api.service.impl;

import com.hotel.api.dto.request.HotelCreateRequest;
import com.hotel.api.dto.response.HotelBriefResponse;
import com.hotel.api.dto.response.HotelDetailResponse;
import com.hotel.api.entity.Amenity;
import com.hotel.api.entity.Hotel;
import com.hotel.api.exception.HotelNotFoundException;
import com.hotel.api.exception.InvalidHistogramParamException;
import com.hotel.api.mapper.HotelMapper;
import com.hotel.api.repository.AmenityRepository;
import com.hotel.api.repository.HotelRepository;
import com.hotel.api.service.HotelService;
import com.hotel.api.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelMapper hotelMapper;

    @Override
    public List<HotelBriefResponse> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toBriefResponse)
                .toList();
    }

    @Override
    public HotelDetailResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        return hotelMapper.toDetailResponse(hotel);
    }

    @Override
    public List<HotelBriefResponse> searchHotels(String name, String brand, String city,
                                                  String country, List<String> amenities) {
        Specification<Hotel> spec = Specification
                .where(HotelSpecification.hasName(name))
                .and(HotelSpecification.hasBrand(brand))
                .and(HotelSpecification.hasCity(city))
                .and(HotelSpecification.hasCountry(country))
                .and(HotelSpecification.hasAnyAmenity(amenities));

        return hotelRepository.findAll(spec).stream()
                .map(hotelMapper::toBriefResponse)
                .toList();
    }

    @Override
    @Transactional
    public HotelBriefResponse createHotel(HotelCreateRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toBriefResponse(saved);
    }

    @Override
    @Transactional
    public void addAmenities(Long hotelId, List<String> amenityNames) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        for (String name : amenityNames) {
            Amenity amenity = amenityRepository.findByName(name)
                    .orElseGet(() -> amenityRepository.save(Amenity.builder().name(name).build()));
            hotel.getAmenities().add(amenity);
        }

        hotelRepository.save(hotel);
    }

    @Override
    public Map<String, Long> getHistogram(String param) {
        List<Object[]> rows = switch (param.toLowerCase()) {
            case "brand"     -> hotelRepository.countByBrand();
            case "city"      -> hotelRepository.countByCity();
            case "country"   -> hotelRepository.countByCountry();
            case "amenities" -> hotelRepository.countByAmenity();
            default          -> throw new InvalidHistogramParamException(param);
        };

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
