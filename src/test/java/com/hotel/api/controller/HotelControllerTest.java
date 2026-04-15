package com.hotel.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.api.dto.request.AddressRequest;
import com.hotel.api.dto.request.ArrivalTimeRequest;
import com.hotel.api.dto.request.ContactsRequest;
import com.hotel.api.dto.request.HotelCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerTest {

    private static final String BASE = "/property-view";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM hotel_amenities");
        jdbcTemplate.execute("DELETE FROM amenities");
        jdbcTemplate.execute("DELETE FROM hotels");
    }

    // -------------------------------------------------------------------------
    // GET /hotels - empty list initially
    // -------------------------------------------------------------------------

    @Test
    void getAllHotels_returnsEmptyList() throws Exception {
        mockMvc.perform(get(BASE + "/hotels"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // POST /hotels - creates a hotel and returns brief response
    // -------------------------------------------------------------------------

    @Test
    void createHotel_returnsCreatedWithBriefResponse() throws Exception {
        String body = objectMapper.writeValueAsString(buildCreateRequest());

        mockMvc.perform(post(BASE + "/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("DoubleTree by Hilton Minsk"))
                .andExpect(jsonPath("$.address").value("9 Pobediteley Avenue, Minsk, 220004, Belarus"))
                .andExpect(jsonPath("$.phone").value("+375 29 309-80-00"));
    }

    @Test
    void createHotel_missingRequiredField_returns400() throws Exception {
        HotelCreateRequest req = buildCreateRequest();
        req.setName(null);

        mockMvc.perform(post(BASE + "/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /hotels/{id} - detail
    // -------------------------------------------------------------------------

    @Test
    void getHotelById_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE + "/hotels/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHotelById_returnsDetail() throws Exception {
        long id = createHotelAndGetId();

        mockMvc.perform(get(BASE + "/hotels/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.brand").value("Hilton"))
                .andExpect(jsonPath("$.address.city").value("Minsk"))
                .andExpect(jsonPath("$.contacts.phone").value("+375 29 309-80-00"))
                .andExpect(jsonPath("$.arrivalTime.checkIn").value("14:00"));
    }

    // -------------------------------------------------------------------------
    // POST /hotels/{id}/amenities
    // -------------------------------------------------------------------------

    @Test
    void addAmenities_hotelNotFound_returns404() throws Exception {
        mockMvc.perform(post(BASE + "/hotels/999/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Free WiFi\"]"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAmenities_success_amenitiesAppearsInDetail() throws Exception {
        long id = createHotelAndGetId();

        mockMvc.perform(post(BASE + "/hotels/" + id + "/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Free WiFi\", \"Free parking\"]"))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/hotels/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amenities", hasSize(2)))
                .andExpect(jsonPath("$.amenities", containsInAnyOrder("Free WiFi", "Free parking")));
    }

    // -------------------------------------------------------------------------
    // GET /search
    // -------------------------------------------------------------------------

    @Test
    void search_byCity_returnsMatchingHotels() throws Exception {
        createHotelAndGetId();

        mockMvc.perform(get(BASE + "/search").param("city", "Minsk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("DoubleTree by Hilton Minsk"));
    }

    @Test
    void search_noMatch_returnsEmptyList() throws Exception {
        createHotelAndGetId();

        mockMvc.perform(get(BASE + "/search").param("city", "Berlin"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // GET /histogram/{param}
    // -------------------------------------------------------------------------

    @Test
    void histogram_byCity_returnsGroupedCounts() throws Exception {
        createHotelAndGetId();

        mockMvc.perform(get(BASE + "/histogram/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Minsk").value(1));
    }

    @Test
    void histogram_byAmenities_returnsGroupedCounts() throws Exception {
        long id = createHotelAndGetId();
        mockMvc.perform(post(BASE + "/hotels/" + id + "/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"Free WiFi\"]"));

        mockMvc.perform(get(BASE + "/histogram/amenities"))
                .andExpect(status().isOk());
    }

    @Test
    void histogram_invalidParam_returns400() throws Exception {
        mockMvc.perform(get(BASE + "/histogram/unknown"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private long createHotelAndGetId() throws Exception {
        String body = objectMapper.writeValueAsString(buildCreateRequest());
        MvcResult result = mockMvc.perform(post(BASE + "/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private HotelCreateRequest buildCreateRequest() {
        AddressRequest address = new AddressRequest();
        address.setHouseNumber(9);
        address.setStreet("Pobediteley Avenue");
        address.setCity("Minsk");
        address.setCountry("Belarus");
        address.setPostCode("220004");

        ContactsRequest contacts = new ContactsRequest();
        contacts.setPhone("+375 29 309-80-00");
        contacts.setEmail("doubletreeminsk.info@hilton.com");

        ArrivalTimeRequest arrivalTime = new ArrivalTimeRequest();
        arrivalTime.setCheckIn("14:00");
        arrivalTime.setCheckOut("12:00");

        HotelCreateRequest req = new HotelCreateRequest();
        req.setName("DoubleTree by Hilton Minsk");
        req.setDescription("The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms ...");
        req.setBrand("Hilton");
        req.setAddress(address);
        req.setContacts(contacts);
        req.setArrivalTime(arrivalTime);
        return req;
    }
}
