package br.com.up.fabrica_automoveis.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CarModel {
  SUV(1),
  SEDAN(2);

  private final int value;

  CarModel(int value) {
    this.value = value;
  }

  public static CarModel fromValue(int value) {
    return Arrays.stream(CarModel.values())
        .filter(model -> model.value == value)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Valor inválido: " + value));
  }
}
