package br.com.up.fabrica_automoveis.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Data
public class ConveyorBelt {
  private final ArrayBlockingQueue<Integer> conveyorBelt = new ArrayBlockingQueue<>(40);
  private final Semaphore semaphore = new Semaphore(1);
  private final Logger logger = LoggerFactory.getLogger(ConveyorBelt.class);
  private final AtomicInteger threadsCompleted = new AtomicInteger(0);


  public void addItems(int numberOfCars){
    try {
      semaphore.acquire();
      for (int i = 0; i < numberOfCars; i++) {
        conveyorBelt.add(i);
      }
      Thread.sleep(5000);
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
        conveyorBelt.poll();
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
}
