package com.healthfirst.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilitySearchResponse {
    
    private SearchCriteriaDto searchCriteria;
    private int totalResults;
    private List<SearchResultDto> results;
    
    public static class SearchCriteriaDto {
        private LocalDate date;
        private String specialization;
        private String location;
        private String appointmentType;
        private Boolean insuranceAccepted;
        private BigDecimal maxPrice;
        private String timezone;
        
        public SearchCriteriaDto() {}
        
        public SearchCriteriaDto(LocalDate date, String specialization, String location, 
                               String appointmentType, Boolean insuranceAccepted, 
                               BigDecimal maxPrice, String timezone) {
            this.date = date;
            this.specialization = specialization;
            this.location = location;
            this.appointmentType = appointmentType;
            this.insuranceAccepted = insuranceAccepted;
            this.maxPrice = maxPrice;
            this.timezone = timezone;
        }
        
        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public String getSpecialization() {
            return specialization;
        }
        
        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getAppointmentType() {
            return appointmentType;
        }
        
        public void setAppointmentType(String appointmentType) {
            this.appointmentType = appointmentType;
        }
        
        public Boolean getInsuranceAccepted() {
            return insuranceAccepted;
        }
        
        public void setInsuranceAccepted(Boolean insuranceAccepted) {
            this.insuranceAccepted = insuranceAccepted;
        }
        
        public BigDecimal getMaxPrice() {
            return maxPrice;
        }
        
        public void setMaxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
        }
        
        public String getTimezone() {
            return timezone;
        }
        
        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
    }
    
    public static class SearchResultDto {
        private ProviderDto provider;
        private List<AvailableSlotDto> availableSlots;
        
        public SearchResultDto() {}
        
        public SearchResultDto(ProviderDto provider, List<AvailableSlotDto> availableSlots) {
            this.provider = provider;
            this.availableSlots = availableSlots;
        }
        
        // Getters and Setters
        public ProviderDto getProvider() {
            return provider;
        }
        
        public void setProvider(ProviderDto provider) {
            this.provider = provider;
        }
        
        public List<AvailableSlotDto> getAvailableSlots() {
            return availableSlots;
        }
        
        public void setAvailableSlots(List<AvailableSlotDto> availableSlots) {
            this.availableSlots = availableSlots;
        }
    }
    
    public static class ProviderDto {
        private String id;
        private String name;
        private String specialization;
        private Integer yearsOfExperience;
        private Double rating;
        private String clinicAddress;
        
        public ProviderDto() {}
        
        public ProviderDto(String id, String name, String specialization, 
                         Integer yearsOfExperience, Double rating, String clinicAddress) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.yearsOfExperience = yearsOfExperience;
            this.rating = rating;
            this.clinicAddress = clinicAddress;
        }
        
        // Getters and Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getSpecialization() {
            return specialization;
        }
        
        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }
        
        public Integer getYearsOfExperience() {
            return yearsOfExperience;
        }
        
        public void setYearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
        }
        
        public Double getRating() {
            return rating;
        }
        
        public void setRating(Double rating) {
            this.rating = rating;
        }
        
        public String getClinicAddress() {
            return clinicAddress;
        }
        
        public void setClinicAddress(String clinicAddress) {
            this.clinicAddress = clinicAddress;
        }
    }
    
    public static class AvailableSlotDto {
        private String slotId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String appointmentType;
        private LocationDto location;
        private PricingDto pricing;
        private List<String> specialRequirements;
        
        public AvailableSlotDto() {}
        
        public AvailableSlotDto(String slotId, LocalDate date, LocalTime startTime, LocalTime endTime,
                              String appointmentType, LocationDto location, PricingDto pricing,
                              List<String> specialRequirements) {
            this.slotId = slotId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.appointmentType = appointmentType;
            this.location = location;
            this.pricing = pricing;
            this.specialRequirements = specialRequirements;
        }
        
        // Getters and Setters
        public String getSlotId() {
            return slotId;
        }
        
        public void setSlotId(String slotId) {
            this.slotId = slotId;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public LocalTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
        
        public String getAppointmentType() {
            return appointmentType;
        }
        
        public void setAppointmentType(String appointmentType) {
            this.appointmentType = appointmentType;
        }
        
        public LocationDto getLocation() {
            return location;
        }
        
        public void setLocation(LocationDto location) {
            this.location = location;
        }
        
        public PricingDto getPricing() {
            return pricing;
        }
        
        public void setPricing(PricingDto pricing) {
            this.pricing = pricing;
        }
        
        public List<String> getSpecialRequirements() {
            return specialRequirements;
        }
        
        public void setSpecialRequirements(List<String> specialRequirements) {
            this.specialRequirements = specialRequirements;
        }
    }
    
    public static class LocationDto {
        private String type;
        private String address;
        private String roomNumber;
        
        public LocationDto() {}
        
        public LocationDto(String type, String address, String roomNumber) {
            this.type = type;
            this.address = address;
            this.roomNumber = roomNumber;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getRoomNumber() {
            return roomNumber;
        }
        
        public void setRoomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
        }
    }
    
    public static class PricingDto {
        private BigDecimal baseFee;
        private Boolean insuranceAccepted;
        private String currency;
        
        public PricingDto() {}
        
        public PricingDto(BigDecimal baseFee, Boolean insuranceAccepted, String currency) {
            this.baseFee = baseFee;
            this.insuranceAccepted = insuranceAccepted;
            this.currency = currency;
        }
        
        // Getters and Setters
        public BigDecimal getBaseFee() {
            return baseFee;
        }
        
        public void setBaseFee(BigDecimal baseFee) {
            this.baseFee = baseFee;
        }
        
        public Boolean getInsuranceAccepted() {
            return insuranceAccepted;
        }
        
        public void setInsuranceAccepted(Boolean insuranceAccepted) {
            this.insuranceAccepted = insuranceAccepted;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
    
    // Default constructor
    public AvailabilitySearchResponse() {}
    
    // Constructor with required fields
    public AvailabilitySearchResponse(SearchCriteriaDto searchCriteria, int totalResults, List<SearchResultDto> results) {
        this.searchCriteria = searchCriteria;
        this.totalResults = totalResults;
        this.results = results;
    }
    
    // Getters and Setters
    public SearchCriteriaDto getSearchCriteria() {
        return searchCriteria;
    }
    
    public void setSearchCriteria(SearchCriteriaDto searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public List<SearchResultDto> getResults() {
        return results;
    }
    
    public void setResults(List<SearchResultDto> results) {
        this.results = results;
    }
} 