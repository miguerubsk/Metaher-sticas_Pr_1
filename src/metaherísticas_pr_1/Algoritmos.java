/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.ArrayList;
import java.util.Arrays;
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
    private ArrayList<Integer> sol;
    private String algoritmo;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo) {
        this.archivo = archivo;
        this.cdl = cdl;
        aleatorio = new Random(semilla);
        log = new StringBuilder();
        sol = new ArrayList(archivo.getTamSolucion());
        this.algoritmo = algoritmo;
    }

    @Override
    public void run() {
        switch (algoritmo) {
            case ("Greedy"):
                Greedy(archivo, archivo.getTamMatriz(), archivo.getTamSolucion(), sol);
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): greedy" + sol.toString());
                break;
            case ("Búsqueda_Local"):
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): Búsqueda_Local");
                break;
            case ("Búsqueda_Tabú"):
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): Búsqueda_Tabú");
                break;
        }
        cdl.countDown();
    }

    public String getLog() {
        return log.toString();
    }

    private double Coste() {
        double coste = 0.0;

        return coste;
    }

    private void Greedy(CargaDatos archivo, int numDatos, int numSoluciones, ArrayList<Integer> s) {
        double mayordist = 0.0;
        ArrayList<Integer> M = new ArrayList();
        Boolean[] marcados = new Boolean[numDatos];
        Arrays.fill(marcados, Boolean.FALSE);
        
        Integer punto = aleatorio.nextInt(numDatos-1);
        marcados[punto] = true;
        s.add(punto);
        
        for (int i = 1; i < numSoluciones; i++) {
            double d = 0.0;
            for (int j = 0; j < numDatos; j++) {
                if (!marcados[j]) {
                    for (int k = 0; k < i; k++) {
//                        d += dist[j][s[k]];
                        d+=archivo.getMatriz()[j][s.get(k)];
                    }
                    if (d > mayordist) {
                        mayordist = d;
                        punto = j;
                    }
                    d = 0.0;
                }

            }
            marcados[punto] = true;
            s.add(punto);
            mayordist = 0.0;
        }
    }

    static void BusquedaLocal() {
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }

    static void BusquedaTabu() {
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }

}
