package br.com.up.fabrica_automoveis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturingData {
  private String employeeName;
  private UUID productionStationNumber;
  private VehicleColors vehicleColor;
  private CarModel carModel;
  private Instant manufacturingTime;
}
