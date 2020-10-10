/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import tools.CargaDatos;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import tools.Configurador;
import tools.Timer;

/**
 *
 * @author Miguerubsk
 */
public class Algoritmos implements Callable<Vector<Integer>> {

    private Random aleatorio;
    private CargaDatos archivo;
    private Configurador config;
    private StringBuilder log;
    private CountDownLatch cdl;
    private Vector<Integer> sol;
    private String algoritmo;
    private Timer tiempo;
    private long semilla;
    Vector<Double> aportes;
    Vector<Boolean> marcados;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo, Configurador config) {
        this.archivo = archivo;
        this.config = config;
        this.cdl = cdl;
        this.aportes = new Vector<>();
        this.marcados = new Vector<>();
        this.semilla = semilla;
        aleatorio = new Random(semilla);
        log = new StringBuilder();
        sol = new Vector<Integer>(archivo.getTamSolucion());
        this.algoritmo = algoritmo;
    }

    @Override
    public Vector<Integer> call() throws Exception {
        double coste;
        long start, stop;
//        System.out.println(archivo.getMatriz().toString());
        switch (algoritmo) {
            case ("Greedy"):
//                Tiempo.Start();
                start = System.currentTimeMillis();
                coste = Greedy();
//                Tiempo.Stop();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): greedy" + sol.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                        + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                
                break;
            case ("Búsqueda_Local"):
//                Tiempo.Start();
                start = System.currentTimeMillis();
                coste = BusquedaLocal();
//                Tiempo.Stop();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): busqueda local" + sol.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste + "\n\n");
                
                break;
            case ("Búsqueda_Tabú"):
//                BusquedaTabu();
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): Búsqueda_Tabú" + sol.toString());
                
                break;
        }
        cdl.countDown();

        return sol;
    }

    //Algoritmos
    private double Greedy() {
        double mayordist = 0.0;
        
        Boolean[] marcados = new Boolean[archivo.getTamMatriz()];
        Arrays.fill(marcados, Boolean.FALSE);

        Integer punto = aleatorio.nextInt(archivo.getTamMatriz() - 1);
        marcados[punto] = true;
        
        int contador = 1;
        sol.add(punto);

        for (int i = 1; i < archivo.getTamSolucion(); i++) {
            double d = 0.0;
            for (int j = 0; j < archivo.getTamMatriz(); j++) {
                if (!marcados[j]) {
                    d = costePuntoEnSolucion(j);
                    if (d > mayordist) {
                        mayordist = d;
                        punto = j;
                    }
                    d = 0.0;
                }
            }
            marcados[punto] = true;
            sol.add(punto);
            
            contador++;
            
            mayordist = 0.0;
        }
        
        return costeSolucion();
    }

    private double BusquedaLocal() {
        return 0;
    }

    private double BusquedaTabu() {
        //TODO
        generarSolucionAleatoria();
        throw new UnsupportedOperationException("No soportado.");
//        return CosteSolución();
    }

    //Funciones auxiliares
    private void generarSolucionAleatoria() {
        for (int i = 0; i < sol.size(); i++) {
            sol.add(aleatorio.nextInt(archivo.getTamMatriz()));
        }
    }

    private double costePuntoEnSolucion(int punto) {
        double distancia = 0.0;
        for (int i = 0; i < sol.size(); i++) {
            distancia += archivo.getMatriz()[punto][sol.get(i)];
        }

        return distancia;
    }

    private boolean estaEnLaSolucion(int ele) {
        for (Integer integer : sol) {
            if (integer == ele) {
                return true;
            }
        }
        return false;
    }

    private double costeSolucion() {
        double distancia = 0.0;

        for (int i = 0; i < sol.size(); i++) {
            for (int j = i + 1; j < sol.size(); j++) {
                distancia += archivo.getMatriz()[sol.get(i)][sol.get(j)];
            }

        }

        return distancia;
    }

    private int posicionAporteMenor() {
        int pos = 0;
        double menor = 999999999;
        for (int i = 0; i < aportes.size(); ++i) {
            if (!(marcados.get(i)) && aportes.get(i) < menor) {
                menor = aportes.get(i);
                pos = i;
            }
        }
        marcados.insertElementAt(true, pos);
        return pos;
    }

    public String getLog() {
        return log.toString();
    }
}
