package br.com.up.fabrica_automoveis.service;

import br.com.up.fabrica_automoveis.model.ManufacturingData;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Service
public class VehicleFactoryService {
  private ExecutorService vehicleProductionStationsPool = Executors.newFixedThreadPool(4);
  private List<VehicleProductionStationService> vehicleProductionStations = new ArrayList<>();
  private AtomicInteger threadsCompleted = new AtomicInteger(0);
  private List<ManufacturingData> response = new ArrayList<>();

  @Autowired
  private ConveyorBelt conveyorBelt;

  @Autowired
  private InventoryService inventoryService;

  private Logger logger = LoggerFactory.getLogger(VehicleFactoryService.class);
  private List<ManufacturingData> carsList = new ArrayList<>();

  @PostConstruct
  public void initializeVehicleProductionStations() {
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));

    logger.info("Inicializadas {} estações de produção de veículos", vehicleProductionStations.size());
  }

  public List<ManufacturingData> startVehicleManufacturing(Integer numberOfCarsToProduce) {

    if (numberOfCarsToProduce > 40) {
      throw new IllegalArgumentException("Não é possível processar a requisição. A quantidade solicitada é maior que a capacidade de estoque!");
    }

    if ((inventoryService.getInventory().size() + conveyorBelt.getQuantityOfItemsOnConveyorBelt()) < numberOfCarsToProduce) {
      throw new IllegalArgumentException("Não há quantidade de peças em estoque para atender à solicitação");
    }

    if(conveyorBelt.getQuantityOfItemsOnConveyorBelt() > numberOfCarsToProduce) {
      return getTheCarsSold(numberOfCarsToProduce);
    }

    logger.info("Sua requisição será processada...");

    var threadsNeeded = Math.ceil((double) Math.abs(conveyorBelt.getQuantityOfItemsOnConveyorBelt() - numberOfCarsToProduce) / 5);
    var threadCurrent = 0;
    var latch = new CountDownLatch((int) threadsNeeded);

    for (int i = 0; i < threadsNeeded; i++) {
      if(threadCurrent >= 4) threadCurrent = 0;
      var table = vehicleProductionStations.get(threadCurrent);
      table.setFuture(CompletableFuture.runAsync(() -> {
        table.run();
        latch.countDown(); // Marca a conclusão de uma thread
      }));
      threadCurrent++;
    }

    try {
      latch.await(); // Aguarda até que todas as threads terminem
      logger.info("Preparando os carros solicitados");
      response = getTheCarsSold(numberOfCarsToProduce);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Erro ao esperar pela conclusão das threads: {}", e.getMessage());
    }

    return response;
  }

  public List<ManufacturingData> getTheCarsSold(Integer numberOfCarsToProduce){
    var soldCars = new ArrayList<ManufacturingData>();
    for(int i = 0; i < numberOfCarsToProduce; i++){ soldCars.add(conveyorBelt.removeItems());}
    logger.info("Quantidade de carros disponíveis na esteira após processar todas as threads: {}", conveyorBelt.getQuantityOfItemsOnConveyorBelt());
    return soldCars;
  }
}