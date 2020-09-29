/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import tools.CargaDatos;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Miguerubsk
 */
public class Algoritmos implements Runnable {
    
    private Random aleatorio;
    private CargaDatos archivo;
    private StringBuilder log;
    private CountDownLatch cdl;
    private long[] sol;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla) {
        this.archivo = archivo;
        this.cdl = cdl;
        aleatorio = new Random(semilla);
        log = new StringBuilder();
        sol = new long[archivo.getTamSolucion()];
    }
    
    @Override
    public void run() {
        //Inicicialización
        log.append("El valor de la primera solución es X");
        long tiempoInicial = System.currentTimeMillis();
        
        //Ejecución
        
        log.append("Iteración Y \nCoste mejor X\nSe acepta solución generada con coste XXXXX");
        long tiempoFinal = System.currentTimeMillis();
        
        //Finalización
        log.append("\nEl costo final es X\n Duración: " + (tiempoFinal - tiempoInicial)/1000 + " segundos");
        cdl.countDown();
    }
    
    public String getLog(){
        return log.toString();
    }
    
    private double Coste(){
        double coste = 0.0;
        
        return coste;
    }
    
    static void Greedy(int tam, int[][] matrizDistancias, int sol[]){
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }
    
    static void BusquedaLocal(){
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }
    
    static void BusquedaTabu(){
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }
    
}
