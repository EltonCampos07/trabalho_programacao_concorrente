package br.com.up.fabrica_automoveis.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum VehicleColors {
  AZUL(1),
  VERMELHO(2),
  VERDE(3);

  private final int value;

  VehicleColors(int value) {
    this.value = value;
  }

  public static VehicleColors fromValue(int value) {
    return Arrays.stream(VehicleColors.values())
        .filter(color -> color.value == value)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Valor inválido: " + value));
  }

}
