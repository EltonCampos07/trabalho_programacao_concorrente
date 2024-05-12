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

    conveyorBelt.getSoldCars().clear();

    if (numberOfCarsToProduce > 40) {
      throw new IllegalArgumentException("Não é possível processar a requisição. A quantidade solicitada é maior que a capacidade de estoque!");
    }

    if ((inventoryService.getInventory().size() + conveyorBelt.getConveyorBelt().size()) < numberOfCarsToProduce) {
      throw new IllegalArgumentException("Não há quantidade de peças em estoque para atender à solicitação");
    }

    if(conveyorBelt.getQuantityOfItemsOnConveyorBelt() > numberOfCarsToProduce) {
      conveyorBelt.removeItems(numberOfCarsToProduce);
    }

    logger.info("Sua requisição será processada...");

    var threadsNeeded = Math.ceil((double) Math.abs(conveyorBelt.getQuantityOfItemsOnConveyorBelt() - numberOfCarsToProduce) / 5);
    CountDownLatch latch = new CountDownLatch((int) threadsNeeded);
    System.out.println(latch + " valor do latch");

    for (int i = 0; i < threadsNeeded; i++) {
      var table = vehicleProductionStations.get(i);
      table.setFuture(CompletableFuture.runAsync(() -> {
        table.run();
        latch.countDown(); // Marca a conclusão de uma thread
      }));
    }

    try {
      latch.await(); // Aguarda até que todas as threads terminem
      conveyorBelt.removeItems(numberOfCarsToProduce);
      logger.info("Quantidade de carros disponíveis na esteira após processar todas as threads: {}", conveyorBelt.getQuantityOfItemsOnConveyorBelt());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Erro ao esperar pela conclusão das threads: {}", e.getMessage());
    }

    return conveyorBelt.getSoldCars().stream().toList();
  }
}