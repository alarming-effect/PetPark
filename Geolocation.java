package pet.park.entity;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
public class Geolocation {
 private BigDecimal latitude;
 private BigDecimal longitude;
 
 public Geolocation(Geolocation geolocation) {
 this.latitude = geolocation.latitude;
 this.longitude = geolocation.longitude;
 }
}//end
