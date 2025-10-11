package com.valenciaBank.valenciaBank.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class CryptoId implements Serializable {
    private String name;
    private LocalDate date;

    public CryptoId() {}

    public CryptoId(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CryptoId cryptoId = (CryptoId) o;
        return Objects.equals(name, cryptoId.name) && Objects.equals(date, cryptoId.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date);
    }
}

