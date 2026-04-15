package com.hotel.api.service;

import com.hotel.api.dto.request.HotelCreateRequest;
import com.hotel.api.dto.response.HotelBriefResponse;
import com.hotel.api.dto.response.HotelDetailResponse;

import java.util.List;
import java.util.Map;

public interface HotelService {

    List<HotelBriefResponse> getAllHotels();

    HotelDetailResponse getHotelById(Long id);

    List<HotelBriefResponse> searchHotels(String name, String brand, String city, String country, List<String> amenities);

    HotelBriefResponse createHotel(HotelCreateRequest request);

    void addAmenities(Long hotelId, List<String> amenityNames);

    Map<String, Long> getHistogram(String param);
}
