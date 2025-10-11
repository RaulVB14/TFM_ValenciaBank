package com.valenciaBank.valenciaBank.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "CRYPTO")
@IdClass(CryptoId.class)
public class Crypto {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "CLOSE")
    private Double close;

    @Column(name = "HIGH")
    private Double high;

    @Column(name = "LOW")
    private Double low;

    @Column(name = "OPEN")
    private Double open;

    @Column(name = "VOLUME")
    private Double volume;


    public Crypto() {
    }

    public Crypto(String name,LocalDate date, Double close, Double high, Double low, Double open, Double volume) {
        this.name = name;
        this.date = date;
        this.close = close;
        this.high = high;
        this.low = low;
        this.open = open;
        this.volume = volume;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Crypto{" +
                "date=" + date +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", open=" + open +
                ", volume=" + volume +
                '}';
    }
}
