package com.graficases.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion Spring Boot.
 * Aqui se inicia el contexto de Spring y se levantan todos los beans.
 */
@SpringBootApplication
public class BackendApplication {

	/**
	 * Metodo main estandar. Delegamos a SpringApplication para arrancar el servidor embebido
	 * y el ciclo de vida de la app.
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
