package com.hotel.api.mapper;

import com.hotel.api.dto.request.AddressRequest;
import com.hotel.api.dto.request.ArrivalTimeRequest;
import com.hotel.api.dto.request.ContactsRequest;
import com.hotel.api.dto.request.HotelCreateRequest;
import com.hotel.api.dto.response.AddressResponse;
import com.hotel.api.dto.response.ArrivalTimeResponse;
import com.hotel.api.dto.response.ContactsResponse;
import com.hotel.api.dto.response.HotelBriefResponse;
import com.hotel.api.dto.response.HotelDetailResponse;
import com.hotel.api.entity.Address;
import com.hotel.api.entity.Amenity;
import com.hotel.api.entity.ArrivalTime;
import com.hotel.api.entity.Contacts;
import com.hotel.api.entity.Hotel;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class HotelMapper {

    public Hotel toEntity(HotelCreateRequest req) {
        return Hotel.builder()
                .name(req.getName())
                .description(req.getDescription())
                .brand(req.getBrand())
                .address(toAddress(req.getAddress()))
                .contacts(toContacts(req.getContacts()))
                .arrivalTime(toArrivalTime(req.getArrivalTime()))
                .build();
    }

    public HotelBriefResponse toBriefResponse(Hotel hotel) {
        return HotelBriefResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress() != null ? hotel.getAddress().toShortString() : null)
                .phone(hotel.getContacts() != null ? hotel.getContacts().getPhone() : null)
                .build();
    }

    public HotelDetailResponse toDetailResponse(Hotel hotel) {
        List<String> amenityNames = hotel.getAmenities().stream()
                .map(Amenity::getName)
                .sorted(Comparator.naturalOrder())
                .toList();

        return HotelDetailResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .brand(hotel.getBrand())
                .address(toAddressResponse(hotel.getAddress()))
                .contacts(toContactsResponse(hotel.getContacts()))
                .arrivalTime(toArrivalTimeResponse(hotel.getArrivalTime()))
                .amenities(amenityNames)
                .build();
    }

    private Address toAddress(AddressRequest req) {
        if (req == null) return null;
        return Address.builder()
                .houseNumber(req.getHouseNumber())
                .street(req.getStreet())
                .city(req.getCity())
                .country(req.getCountry())
                .postCode(req.getPostCode())
                .build();
    }

    private Contacts toContacts(ContactsRequest req) {
        if (req == null) return null;
        return Contacts.builder()
                .phone(req.getPhone())
                .email(req.getEmail())
                .build();
    }

    private ArrivalTime toArrivalTime(ArrivalTimeRequest req) {
        if (req == null) return null;
        return ArrivalTime.builder()
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) return null;
        return AddressResponse.builder()
                .houseNumber(address.getHouseNumber())
                .street(address.getStreet())
                .city(address.getCity())
                .country(address.getCountry())
                .postCode(address.getPostCode())
                .build();
    }

    private ContactsResponse toContactsResponse(Contacts contacts) {
        if (contacts == null) return null;
        return ContactsResponse.builder()
                .phone(contacts.getPhone())
                .email(contacts.getEmail())
                .build();
    }

    private ArrivalTimeResponse toArrivalTimeResponse(ArrivalTime arrivalTime) {
        if (arrivalTime == null) return null;
        return ArrivalTimeResponse.builder()
                .checkIn(arrivalTime.getCheckIn())
                .checkOut(arrivalTime.getCheckOut())
                .build();
    }
}
