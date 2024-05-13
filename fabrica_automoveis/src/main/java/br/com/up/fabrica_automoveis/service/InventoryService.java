package br.com.up.fabrica_automoveis.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

@Service
@Data
public class InventoryService {
  private final Logger logger = LoggerFactory.getLogger(InventoryService.class);
  private final Semaphore semaphore = new Semaphore(1);
  private final ArrayBlockingQueue<Integer> inventory = new ArrayBlockingQueue<>(500);
  private static final Integer MAXIMUM_QUANTITY_OF_PARTS_PER_REQUISITION = 5;

  public InventoryService(){
    for (int i = 0; i < 500; i++) {
      inventory.add(i);
    }
  }

  public void requestParts(){
    try {
      semaphore.acquire();
      for(int i = 0; i < MAXIMUM_QUANTITY_OF_PARTS_PER_REQUISITION; i++){
        inventory.poll();
      }
      logger.info("Aguardando o tempo de coleta das peças ...");
      //Thread.sleep(1000);
      semaphore.release();
    } catch(InterruptedException ex){
      logger.error("Erro ao requisitar as peças ao inventário: {}", ex.getMessage());
    } finally {
      logger.info("Tamanho atual do estoque: {}", inventory.size());
    }
  }
}
