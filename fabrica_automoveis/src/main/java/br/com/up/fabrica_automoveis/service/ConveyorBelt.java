package br.com.up.fabrica_automoveis.service;

import br.com.up.fabrica_automoveis.model.CarModel;
import br.com.up.fabrica_automoveis.model.ManufacturingData;
import br.com.up.fabrica_automoveis.model.VehicleColors;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
@Data
public class ConveyorBelt {
  private Semaphore mutex = new Semaphore(1);
  private Semaphore full = new Semaphore(0);
  private Semaphore empty = new Semaphore(40);
  private ArrayList<ManufacturingData> conveyorBelt = new ArrayList<>(40); // Tamanho fixo de 40
  private int head = 0; // Ponteiro para o início do buffer
  private int tail = 0; // Ponteiro para o final do buffer
  private final Logger logger = LoggerFactory.getLogger(ConveyorBelt.class);

  public ConveyorBelt() {
    // Inicialize o ArrayList com elementos nulos
    for (int i = 0; i < 40; i++) {
      conveyorBelt.add(null);
    }
  }

  public void addItems(UUID IDVehicleProductionStation, String employee) {
    try {
      empty.acquire();
      mutex.acquire();

      var carData = ManufacturingData.builder()
          .employeeName(employee)
          .carModel(chooseCarModel())
          .manufacturingTime(Instant.now())
          .productionStationNumber(IDVehicleProductionStation)
          .vehicleColor(chooseCarColor())
          .build();

      conveyorBelt.set(tail, carData); // Substitui o elemento na posição tail
      tail = (tail + 1) % 40; // Atualiza a posição do tail de forma cíclica

      mutex.release();
      full.release();
    } catch (InterruptedException ex) {
      logger.error("Erro ao adicionar os carros na esteira: {}", ex.getMessage());
    }
  }

  public ManufacturingData removeItems() {
    ManufacturingData car = null;
    try {
      full.acquire();
      mutex.acquire();

      car = conveyorBelt.get(head); // Obtém o elemento na posição head
      conveyorBelt.set(head, null); // Remove o elemento na posição head
      head = (head + 1) % 40; // Atualiza a posição do head de forma cíclica

      mutex.release();
      empty.release();
    } catch (InterruptedException ex) {
      logger.error("Erro ao remover os carros na esteira: {}", ex.getMessage());
    }
    return car;
  }

  public int getQuantityOfItemsOnConveyorBelt() {
    int count = 0;
    try {
      mutex.acquire();
      for (ManufacturingData car : conveyorBelt) {
        if (car != null) count++;
      }
      mutex.release();
    } catch (InterruptedException ex) {
      logger.error("Erro ao obter a quantidade de carros na esteira: {}", ex.getMessage());
    }
    return count;
  }

  public VehicleColors chooseCarColor() {
    var random = new Random();
    var corNumber = random.nextInt(3) + 1;
    return VehicleColors.fromValue(corNumber);
  }

  public CarModel chooseCarModel() {
    var random = new Random();
    var corNumber = random.nextInt(2) + 1;
    return CarModel.fromValue(corNumber);
  }


























/*  private final ArrayBlockingQueue<ManufacturingData> conveyorBelt = new ArrayBlockingQueue<>(40);
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
      System.out.println(conveyorBelt.size() + " tamanho do conveyor");
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
  }*/
}
