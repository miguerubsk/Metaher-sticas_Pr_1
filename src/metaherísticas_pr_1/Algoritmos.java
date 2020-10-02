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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Miguerubsk
 */
public class Algoritmos implements Callable<HashSet<Integer>> {

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
                Greedy();
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): greedy" + sol.toString());
                break;
            case ("Búsqueda_Local"):
                BusquedaLocal();
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): Búsqueda_Local" + sol.toString());
                break;
            case ("Búsqueda_Tabú"):
                BusquedaTabu();
                System.out.println("metaherísticas_pr_1.Algoritmos.run(): Búsqueda_Tabú" + sol.toString());
                break;
        }
        cdl.countDown();

        return sol;
    }

    public String getLog() {
        return log.toString();
    }

    void GenerarSolucionAleatoria() {
        while (sol.size() < archivo.getTamSolucion()) {
            sol.add(aleatorio.nextInt() * 500);
        }
    }

    private double CostePunto(int punto) {
        double distancia = 0.0;
        Iterator i = sol.iterator();
        while (i.hasNext()) {
            distancia += archivo.getMatriz()[punto][(int) i.next()];
        }
        return distancia;
    }
    
    private double CosteSolución(){
        double distancia = 0.0;
        for (Iterator i = sol.iterator(); i.hasNext(); i.next()) {
            for (Iterator j = sol.iterator(); j.hasNext(); j.next()) {
                distancia += archivo.getMatriz()[(int) j.next()][(int) i.next()];
            }
        }
        return distancia;
    }

    private void Greedy() {
        double mayordist = 0.0;
        ArrayList<Integer> M = new ArrayList();
        Boolean[] marcados = new Boolean[archivo.getTamMatriz()];
        Arrays.fill(marcados, Boolean.FALSE);

        Integer punto = aleatorio.nextInt(archivo.getTamMatriz() - 1);
        marcados[punto] = true;
        sol.add(punto);

        for (int i = 1; i < archivo.getTamSolucion(); i++) {
            double d = 0.0;
            for (int j = 0; j < archivo.getTamMatriz(); j++) {
                if (!marcados[j]) {
                    d = CostePunto(j);
                    if (d > mayordist) {
                        mayordist = d;
                        punto = j;
                    }
                    d = 0.0;
                }

            }
            marcados[punto] = true;
            sol.add(punto);
            mayordist = 0.0;
        }
    }

    private void BusquedaLocal() {
        GenerarSolucionAleatoria();
        throw new UnsupportedOperationException("No soportado.");
    }

    private void BusquedaTabu() {
        //TODO
        throw new UnsupportedOperationException("No soportado.");
    }

}
