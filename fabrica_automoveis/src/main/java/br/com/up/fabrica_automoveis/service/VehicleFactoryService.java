package br.com.up.fabrica_automoveis.service;

import br.com.up.fabrica_automoveis.model.ManufacturingData;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Service
public class VehicleFactoryService {
  private ExecutorService vehicleProductionStationsPool = Executors.newFixedThreadPool(4);
  private List<VehicleProductionStationService> vehicleProductionStations = new ArrayList<>();

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


  public List<ManufacturingData> startVehicleManufacturing (Integer numberOfCarsToProduce) {
    conveyorBelt.getSoldCars().clear();

    if(numberOfCarsToProduce > 40){
      throw new IllegalArgumentException("Não é possível processar a requisição. A quantidade solicitado a maior que capacidade de estoque!");
    }

    if((inventoryService.getInventory().size() + conveyorBelt.getConveyorBelt().size()) < numberOfCarsToProduce) {
      throw new IllegalArgumentException("Não há quantidade de peças em estoque para atender a solicitação");
    }

    logger.info("Sua requisição será processada...");

    if(conveyorBelt.getQuantityOfItemsOnConveyorBelt() > numberOfCarsToProduce) {
      conveyorBelt.removeItems(numberOfCarsToProduce);
    }

    var currentThread = 0;
    var threadsNeeded  = Math.ceil((double) Math.abs(conveyorBelt.getQuantityOfItemsOnConveyorBelt() - numberOfCarsToProduce) / 5);
    CountDownLatch latch = new CountDownLatch((int) threadsNeeded);

    for(int i = 0; i < threadsNeeded ; i++){
      if(currentThread > 3) currentThread = 0;
      var table = vehicleProductionStations.get(currentThread);
      logger.info("A mesa com ID: {} está solicitando recurso para fabricar os carros", table.getId());
      vehicleProductionStationsPool.execute(() -> {
        table.run();
        latch.countDown();
      });
      currentThread += 1;
    }

    try {
      latch.await(); // Aguarda até que o latch chegue a zero
      conveyorBelt.removeItems(numberOfCarsToProduce);
      logger.info("Quantidade de carros disponíveis na esteira após processar todas as threads: {}", conveyorBelt.getQuantityOfItemsOnConveyorBelt());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Erro ao esperar pela conclusão das threads: {}", e.getMessage());
    }

    return conveyorBelt.getSoldCars().stream().toList();
  }

}
