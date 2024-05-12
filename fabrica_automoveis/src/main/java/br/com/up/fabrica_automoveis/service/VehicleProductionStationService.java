package br.com.up.fabrica_automoveis.service;

import br.com.up.fabrica_automoveis.model.Employee;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Data
@Service
public class VehicleProductionStationService implements Runnable{
  private Logger logger = LoggerFactory.getLogger(VehicleProductionStationService.class);
  private List<Employee> employees = new ArrayList<>();
  private UUID id;
  private ConveyorBelt conveyorBelt;
  private InventoryService inventoryService;

  private ExecutorService employeeAtAssemblyStationPool = Executors.newFixedThreadPool(5);
  private CountDownLatch threadLatch = new CountDownLatch(5);  // CountDownLatch para contar as threads concluídas
  private CompletableFuture<Void> future;

  private Semaphore tools1 = new Semaphore(1);
  private Semaphore tools2 = new Semaphore(1);
  private Semaphore tools3 = new Semaphore(1);
  private Semaphore tools4 = new Semaphore(1);
  private Semaphore tools5 = new Semaphore(1);

  {
    id = UUID.randomUUID();
    employees.add(new Employee(id,"funcionário 1", tools1, tools2));
    employees.add(new Employee(id,"funcionário 2", tools2, tools3));
    employees.add(new Employee(id,"funcionário 3", tools3, tools4));
    employees.add(new Employee(id,"funcionário 4", tools4, tools5));
    employees.add(new Employee(id,"funcionário 5", tools5, tools1));
  }

  public VehicleProductionStationService(ConveyorBelt conveyorBelt, InventoryService inventoryService){
    this.conveyorBelt = conveyorBelt;
    this.inventoryService = inventoryService;
  }

  @Override
  public void run() {
    inventoryService.requestParts();
    logger.info("A mesa com ID: {} recebeu 5 peças com sucesso", id);

    for (int i = 0; i < 5; i++) {
      var employee = employees.get(i);
      employeeAtAssemblyStationPool.execute(() -> {
        employee.run();
        conveyorBelt.addItems(id, employee.getEmployeeName());
        threadLatch.countDown(); // Marca a conclusão de uma thread
      });
    }

    try {
      threadLatch.await(); // Aguarda a conclusão de todas as threads
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Erro ao aguardar a conclusão das threads: {}", e.getMessage());
    }

    logger.info("A mesa com ID: {} produziu 5 carros com sucesso", id);
  }
}
