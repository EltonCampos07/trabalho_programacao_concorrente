package br.com.up.fabrica_automoveis.service;

import br.com.up.fabrica_automoveis.model.CarModel;
import br.com.up.fabrica_automoveis.model.ManufacturingData;
import br.com.up.fabrica_automoveis.model.VehicleColors;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

@Service
@Data
public class ConveyorBelt {
  private final ArrayBlockingQueue<ManufacturingData> conveyorBelt = new ArrayBlockingQueue<>(40);
  private final ArrayBlockingQueue<ManufacturingData> soldCars = new ArrayBlockingQueue<>(40);
  private final Semaphore semaphore = new Semaphore(1);
  private final Logger logger = LoggerFactory.getLogger(ConveyorBelt.class);

  public void addItems(UUID IDVehicleProductionStation, String employee){
    try {
      semaphore.acquire();
      var carData = ManufacturingData.builder()
          .employeeName(employee)
          .carModel(chooseCarModel())
          .manufacturingTime(Instant.now())
          .productionStationNumber(IDVehicleProductionStation)
          .vehicleColor(chooseCarColor())
          .build();
      conveyorBelt.add(carData);
      Thread.sleep(2500);
    } catch (InterruptedException ex){
      logger.error("Erro ao adicionar carros à esteira: {}", ex.getMessage());
    } finally {
      semaphore.release();
      logger.info("Quantidade de carros disponíveis na esteira: {}", conveyorBelt.size());
    }
  }

  public void removeItems(int numberOfCars) {
    try {
      semaphore.acquire();
      for (int i = 0; i < numberOfCars; i++) {
        soldCars.add(conveyorBelt.poll());
      }
    } catch (InterruptedException ex){
      logger.error("Erro ao remover carros da esteira: {}", ex.getMessage());
    } finally {
      semaphore.release();
    }
  }

  public int getQuantityOfItemsOnConveyorBelt() {
    return conveyorBelt.size();
  }

  public VehicleColors chooseCarColor(){
    var random = new Random();
    var corNumber = random.nextInt(3) + 1;
    return VehicleColors.fromValue(corNumber);
  }

  public CarModel chooseCarModel(){
    var random = new Random();
    var corNumber = random.nextInt(2) + 1;
    return CarModel.fromValue(corNumber);
  }
}
