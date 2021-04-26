package com.application.hotspotapplication.requests.hotspots.Location;

import com.application.hotspotapplication.exceptions.ApiRequestException;
import com.application.hotspotapplication.utils.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationService {

  @Autowired
  private LocationDAO dao;
  @Value("${api.key}")
  private String positionStackApiKey;
  private final RestTemplate restTemplate;

  public LocationService(RestTemplateBuilder restTemplateBuilder){
    this.restTemplate = restTemplateBuilder.build();
  }

  @SneakyThrows
  public Location createHotspotLocation(String streetAddress, String areaName, String cityName, int postalCode){
    String addressQuery = streetAddress + ", " + areaName + ", " + cityName + ", " + postalCode;
    try {
      Location curLocation = getHotspotLocation(addressQuery).get(Constants.TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

      if (locationExists(curLocation.getLatitude(), curLocation.getLongitude())) {
        return dao.findByLatitudeAndLongitude(curLocation.getLatitude(), curLocation.getLongitude()).get();
      }

      if (curLocation.getConfidence() >= 0.5) { //@TODO find a better more accurate way of determining if location is correct.
        curLocation.setStreetAddress(streetAddress);
        curLocation.setCity(cityName);
        curLocation.setNeighbourhood(areaName);
        curLocation.setPostalCode(postalCode);
        dao.save(curLocation);
      }
      return curLocation;
    }catch (Exception e){
      throw new ApiRequestException("Could not add specified location", HttpStatus.BAD_REQUEST);
    }
  }
  public boolean locationExists(Double latitude, Double longitude){
    return dao.findByLatitudeAndLongitude(latitude, longitude).isPresent();
  }

  public List<Location> getHotspotLocationByNeighbourhood(String neighbourhood) {return dao.findByNeighbourhood(neighbourhood);}

  public List<Location> getHotspotLocationsByRegion(String region){
    return dao.findByRegion(region);
  }

  public List<Location> getAllHotspotLocations(){
    return dao.findAll();
  }

  @SneakyThrows
  @Async
  private CompletableFuture<Location> getHotspotLocation(String addressQuery){
    String requestURL = Constants.POSSITIONSTACK_API_URL_FORWARD + positionStackApiKey + "&query=" + addressQuery + "&country=ZA";
    ResponseEntity<LocationData> responseEntity =  restTemplate.getForEntity(requestURL, LocationData.class);

    if(responseEntity.getStatusCode() == HttpStatus.OK) {
      Location curLocation = getConfidentLocation(responseEntity.getBody().getData());
      return CompletableFuture.completedFuture(curLocation);
    }
    throw new ApiRequestException("Cannot find location", HttpStatus.BAD_REQUEST);
  }

  public Optional<Location> findLocationById(Long id){
    return dao.findById(id);
  }


  private Location getConfidentLocation(List<Location> locations){
    return locations.stream().max(Comparator.comparing(Location::getConfidence)).get();
  }

}
