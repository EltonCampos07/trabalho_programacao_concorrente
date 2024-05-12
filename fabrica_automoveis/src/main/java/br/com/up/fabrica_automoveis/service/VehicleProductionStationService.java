package br.com.up.fabrica_automoveis.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

@Data
@Service
public class VehicleProductionStationService implements Runnable{
  private Logger logger = LoggerFactory.getLogger(VehicleProductionStationService.class);
  private HashMap<Integer, String> employees = new LinkedHashMap<>();
  private UUID id;
  private ConveyorBelt conveyorBelt;
  private InventoryService inventoryService;

  {
    id = UUID.randomUUID();
    employees.put(1, "funcionário 1");
    employees.put(2, "funcionário 2");
    employees.put(3, "funcionário 3");
    employees.put(4, "funcionário 4");
    employees.put(5, "funcionário 5");
  }

  public VehicleProductionStationService(ConveyorBelt conveyorBelt, InventoryService inventoryService){
    this.conveyorBelt = conveyorBelt;
    this.inventoryService = inventoryService;
  }

  @Override
  public void run() {
    System.out.println(inventoryService);
    inventoryService.requestParts();
    logger.info("A mesa com ID: {} recebeu 5 peças com sucesso", id);
    conveyorBelt.addItems(5);
    logger.info("A mesa com ID: {} produziu 5 carros com sucesso", id);
  }
}
