package br.com.up.fabrica_automoveis.controller;

import br.com.up.fabrica_automoveis.model.ManufacturingData;
import br.com.up.fabrica_automoveis.service.VehicleFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manufacture_cars")
public class VehicleFactoryController {

  @Autowired
  private VehicleFactoryService vehicleFactoryService;

  @GetMapping("/{carNumberRequested}")
  public ResponseEntity<List<ManufacturingData>> getCars(@PathVariable Integer carNumberRequested){
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(vehicleFactoryService
            .startVehicleManufacturing(carNumberRequested));
  }
}
