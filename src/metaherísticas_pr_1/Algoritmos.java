/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import tools.CargaDatos;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Miguerubsk
 */
public class Algoritmos implements Callable<ArrayList<Integer>> {

    private Random aleatorio;
    private CargaDatos archivo;
    private StringBuilder log;
    private CountDownLatch cdl;
    private HashSet<Integer> sol;
    private String algoritmo;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo) {
        this.archivo = archivo;
        this.cdl = cdl;
        aleatorio = new Random(semilla);
        log = new StringBuilder();
        sol = new HashSet(archivo.getTamSolucion());
        this.algoritmo = algoritmo;
    }

    @Override
    public HashSet<Integer> call() throws Exception {
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

        return sol;
    }

    public String getLog() {
        return log.toString();
    }

    private double Coste(double distancias[][], int punto, ArrayList<Integer> sol) {
        double distancia = 0.0;
        for (int i = 0; i < sol.size(); i++) {
            distancia += distancias[punto][sol.get(i)];
        }
        return distancia;
    }

    private void Greedy(CargaDatos archivo, int numDatos, int numSoluciones, HashSet<Integer> s) {
        double mayordist = 0.0;
        ArrayList<Integer> M = new ArrayList();
        Boolean[] marcados = new Boolean[numDatos];
        Arrays.fill(marcados, Boolean.FALSE);

        Integer punto = aleatorio.nextInt(numDatos - 1);
        marcados[punto] = true;
        s.add(punto);

        for (int i = 1; i < numSoluciones; i++) {
            double d = 0.0;
            for (int j = 0; j < numDatos; j++) {
                if (!marcados[j]) {
//                    for (int k = 0; k < i; k++) {
//                        d += archivo.getMatriz()[j][s.get(k)];
//                    }

                    d = Coste(archivo.getMatriz(), j, s);
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
    
    private HashSet GenerarAleatoria(){
        while(sol.size() < 50){
            sol.add(aleatorio.nextInt()*500);
        }
        return sol;
    }

}
