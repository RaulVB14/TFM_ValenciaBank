package com.valenciaBank.valenciaBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.valenciaBank.valenciaBank"}) // Asegúrate de que este paquete se está escaneando
public class ValenciaBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValenciaBankApplication.class, args);
	}
}