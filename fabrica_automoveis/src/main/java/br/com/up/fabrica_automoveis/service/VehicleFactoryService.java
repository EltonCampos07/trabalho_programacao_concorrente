package br.com.up.fabrica_automoveis.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Service
public class VehicleFactoryService {
  private ExecutorService vehicleProductionStationsPool = Executors.newFixedThreadPool(4);
  private List<VehicleProductionStationService> vehicleProductionStations = new ArrayList<>();
  private AtomicInteger threadsCompleted = new AtomicInteger(0);

  @Autowired
  private ConveyorBelt conveyorBelt;

  @Autowired
  private InventoryService inventoryService;

  private Logger logger = LoggerFactory.getLogger(VehicleFactoryService.class);

  @PostConstruct
  public void initializeVehicleProductionStations() {
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));
    vehicleProductionStations.add(new VehicleProductionStationService(conveyorBelt, inventoryService));

    logger.info("Inicializadas {} estações de produção de veículos", vehicleProductionStations.size());
  }


  public Integer startVehicleManufacturing (Integer numberOfCarsToProduce) {

    if(inventoryService.getInventory().isEmpty() || conveyorBelt.getQuantityOfItemsOnConveyorBelt() >= 40){
      throw new IllegalArgumentException("Não é possível processar a requisição. O estoque não há peças disponível!");
    }

    logger.info("Sua requisição será processada...");

    if(conveyorBelt.getQuantityOfItemsOnConveyorBelt() > numberOfCarsToProduce) {
      conveyorBelt.removeItems(numberOfCarsToProduce);
    }

    var currentThread = 0;
    var threadsNeeded  = Math.ceil((double) Math.abs(conveyorBelt.getQuantityOfItemsOnConveyorBelt() - numberOfCarsToProduce) / 5);

    for(int i = 0; i < threadsNeeded ; i++){
      if(currentThread > 3) currentThread = 0;
      var table = vehicleProductionStations.get(currentThread);
      logger.info("A mesa com ID: {} está solicitando recurso para fabricar os carros", table.getId());
      vehicleProductionStationsPool.execute(() -> {
        table.run();
        int completed = threadsCompleted.incrementAndGet();
        if (completed == threadsNeeded) {
          conveyorBelt.removeItems(numberOfCarsToProduce);
          threadsCompleted.set(0);
          logger.info("Quantidade de carros disponíveis na esteira após processar todas as threads: {}", conveyorBelt.getQuantityOfItemsOnConveyorBelt());
        }
      });
      currentThread += 1;
      }

    return numberOfCarsToProduce;
  }
}
