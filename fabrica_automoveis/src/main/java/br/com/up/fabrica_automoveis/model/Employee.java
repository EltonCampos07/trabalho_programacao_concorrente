package br.com.up.fabrica_automoveis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Semaphore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee implements Runnable{

  private UUID idProductionStationService;
  private String employeeName;
  private Semaphore tools1;
  private Semaphore tools2;
  private static Logger logger = LoggerFactory.getLogger(Employee.class);

  @Override
  public void run() {
    organizeTools();
    getTheLeftTool();
    getTheRightTool();
    manufactureTheCar();
    releaseLeftTool();
    releaseRightTool();
  }

  private void organizeTools() {
    try {
      logger.info("O {} da mesa: {} começou organizar as ferramentas para inicializar a fabricação", employeeName, idProductionStationService);
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu organizar a ferramenta: {}", employeeName, idProductionStationService, ex.getMessage());
    }
  }

  private void getTheLeftTool() {
    try {
      logger.info("O {} da mesa: {} pegou a ferramenta à sua esquerda", employeeName, idProductionStationService);
      tools1.acquire();
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu pegar a ferramenta à esquerda: {}", idProductionStationService, employeeName, ex.getMessage());
    }
  }

  private void getTheRightTool() {
    try {
      logger.info("O {} da mesa: {} pegou a ferramenta à sua direita", employeeName, idProductionStationService);
      tools2.acquire();
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu pegar a ferramenta à direita: {}", idProductionStationService, employeeName, ex.getMessage());
    }
  }

  private void manufactureTheCar() {
    try {
      logger.info("O {} da mesa: {} começou fabricar o carro", employeeName, idProductionStationService);
      Thread.sleep(2500);
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu fabricar o carro: {}", idProductionStationService, employeeName, ex.getMessage());
    }
  }

  private void releaseLeftTool() {
    try {
      logger.info("O {} da mesa: {} devolveu a ferramenta à esquerda", employeeName, idProductionStationService);
      tools1.release();
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu devolver a ferramenta à esquerda: {}", idProductionStationService, employeeName, ex.getMessage());
    }
  }

  private void releaseRightTool() {
    try {
      logger.info("O {} da mesa: {} devolveu a ferramenta à direita", employeeName, idProductionStationService);
      tools2.release();
    } catch (Exception ex) {
      logger.error("O {} da mesa: {} não conseguiu devolver a ferramenta à direita: {}", idProductionStationService, employeeName, ex.getMessage());
    }
  }
}
