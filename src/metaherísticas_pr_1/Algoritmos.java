/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.Arrays;
import java.util.Random;
import tools.CargaDatos;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import tools.Configurador;
//import tools.random;

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
//    private Timer tiempo;
    private long semilla;
    Vector<Double> aportes;
    Vector<Boolean> marcados;
    Integer contadorMarcados;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo, Configurador config) {
        this.archivo = archivo;
        this.config = config;
        this.cdl = cdl;
        this.aportes = new Vector<>();
        this.marcados = new Vector<>();
        this.semilla = semilla;
        aleatorio = new Random(semilla);
//        aleatorio.Set_random(semilla);
        log = new StringBuilder();
        sol = new Vector<Integer>(archivo.getTamSolucion());
        this.algoritmo = algoritmo;
    }

    @Override
    public Vector<Integer> call() throws Exception {
        double coste;
        long start, stop;
        switch (algoritmo) {
            case ("Greedy"):
                start = System.currentTimeMillis();
                coste = Greedy();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): greedy" + sol.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                        + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                break;

            case ("Búsqueda_Local"):
                start = System.currentTimeMillis();
                coste = BusquedaLocal();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): busqueda local" + sol.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste + "\n\n");

                break;

            case ("Búsqueda_Tabú"):
                start = System.currentTimeMillis();
                coste = BusquedaTabu();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): busqueda local" + sol.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste + "\n\n");
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

        Integer punto = aleatorio.nextInt(archivo.getTamMatriz());
        marcados[punto] = true;

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

            mayordist = 0.0;
        }

        return costeSolucion();
    }

    private double BusquedaLocal() {

        generarSolucionAleatoria();
        double costeActual = costeSolucion();
        double C;
        int evalua = 0;
        int pos;

        boolean mejora = Boolean.TRUE;

        while (evalua < config.getEvaluaciones() && mejora) {
            mejora = Boolean.FALSE;
            for (int i = 0; i < archivo.getTamSolucion(); i++) {
                marcados.add(Boolean.FALSE);
            }
            actualizarCostes(sol);

            for (int k = 0; k < archivo.getTamSolucion(); k++) {
                pos = obtenerPosicionAporteMenor();

                for (int i = 0; i < archivo.getTamMatriz(); i++) {
                    if (!sol.contains(i)) {
                        C = FactorCoste(costeActual, sol.get(pos), i);
                        evalua++;
                        if (costeActual < C) {
                            sol.set(pos, i);
                            costeActual = C;
                            mejora = Boolean.TRUE;
                        }
                    }

                }
                if (mejora) {
                    break;
                }
            }
            marcados.clear();
            aportes.clear();
        }

        return costeSolucion();
    }

    private double BusquedaTabu() {
        generarSolucionAleatoria();

        ConcurrentLinkedQueue<Integer> memC = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < config.getTenencia(); i++) {
            memC.add(-1);
        }

        Integer[] memL = new Integer[archivo.getTamMatriz()];
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memL[i] = 0;
            if (sol.contains(i)) {
                memL[i]++;
            }
        }

        Integer iteracion = 0;
//        Integer anterior = 0;
        Integer posAporteMenor = 0;
        Integer numVecinos = 10;
//        double costeAnterior;
//        boolean mejora = true;
        contadorMarcados = 0;
        Vector<Integer> SolucionActual = sol, SolucionParcial = null;
        int contadorReinicio = 0;

        while (iteracion < config.getEvaluaciones()) {
//            System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu() " + iteracion + " " + contadorReinicio);
            Vector<Integer> soluciones = new Vector<Integer>();
            double costeActual = 0.0;
            actualizarCostes(SolucionActual);
            posAporteMenor = obtenerPosicionAporteMenor();
            int elemento = 0;
//            costeAnterior = costeSolucion();
//            eliminarPuntoSolucion(posAporteMenor);
            memC.poll();
            memC.offer(SolucionActual.get(posAporteMenor));

//            SolucionParcial = SolucionActual;
            generarSoluciones(numVecinos, soluciones, SolucionActual, memC);

            for (int i = 0; i < numVecinos; i++) {
                SolucionActual.set(posAporteMenor, soluciones.get(i));
                if (costeActual < coste(SolucionActual)) {
                    costeActual = coste(SolucionActual);
                    elemento = soluciones.get(i);
                }
            }
            SolucionActual.set(posAporteMenor, elemento);
            iteracion++;

            if (costeSolucion() < coste(SolucionActual)) {
                sol = SolucionActual;
                contadorReinicio = 0;
            } else {
                contadorReinicio++;
            }

            if (contadorReinicio == 100) {
                reinicioBTabu(memC, memL, SolucionActual, contadorReinicio);
                contadorReinicio = 0;
            }

        }

        return costeSolucion();
    }

    //Funciones auxiliares
//    private void restablecerSolucionAnterior(Integer anterior, Integer posAporteMenor, double costeAnterior, boolean mejora) {
//        sol.insertElementAt(anterior, posAporteMenor);
//        aportes.insertElementAt(costeAnterior, posAporteMenor);
//        marcados.removeElementAt(posAporteMenor);
//        marcados.insertElementAt(Boolean.TRUE, posAporteMenor);
//        contadorMarcados++;
//        mejora = true;
//    }

    private void reinicioBTabu(ConcurrentLinkedQueue<Integer> memC, Integer[] memL, Vector<Integer> SolucionActual, int contadorReinicio) {
        if (aleatorio.nextInt(2) == 1) {
            SolucionActual.removeAllElements();
            Integer elemento = 0;
            for (int i = 0; i < archivo.getTamSolucion(); i++) {
                int mayor = 0;
                for (int j = 0; j < archivo.getTamMatriz(); j++) {
                    if (memL[j] > mayor && !SolucionActual.contains(j)) {
                        elemento = j;
                    }

                }
                SolucionActual.add(elemento);
            }
        } else {
            SolucionActual.removeAllElements();
            Integer elemento = 0;
            for (int i = 0; i < archivo.getTamSolucion(); i++) {
                int mayor = 999999999;
                for (int j = 0; j < archivo.getTamMatriz(); j++) {
                    if (memL[j] < mayor && !SolucionActual.contains(j)) {
                        elemento = j;
                    }

                }
                SolucionActual.add(elemento);
            }

        }
        memC.clear();

        for (int i = 0; i < config.getTenencia(); i++) {
            memC.add(-1);
        }

        contadorReinicio = 0;
    }

//    private void guardarSolucionAnterior(Integer anterior, double costeAnterior, Integer posAporteMenor) {
//        anterior = sol.get(posAporteMenor);
//        costeAnterior = aportes.get(posAporteMenor);
//    }

//    private void eliminarPuntoSolucion(Integer posAporteMenor) {
//        sol.removeElementAt(posAporteMenor);
//        aportes.removeElementAt(posAporteMenor);
//    }

    private void actualizarCostes(Vector<Integer> SolucionActual) {
        aportes.removeAllElements();
        marcados.removeAllElements();
        for (Integer integer : SolucionActual) {
            aportes.add(costePuntoEnSolucion(integer));
            marcados.add(false);
        }
    }

    private void generarSolucionAleatoria() {
        int contador = 0;
        while(contador < archivo.getTamSolucion()){
            int ele = aleatorio.nextInt(archivo.getTamMatriz());
            if(!sol.contains(ele)){
                sol.add(ele);
                contador++;
            }
                
        }
    }

    private double costePuntoEnSolucion(Integer punto) {
        double distancia = 0.0;
        for (int i = 0; i < sol.size(); i++) {
            distancia += archivo.getMatriz()[punto][sol.get(i)];
        }

        return distancia;
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

    private double coste(Vector<Integer> soli) {
        double distancia = 0.0;

        for (int i = 0; i < soli.size(); i++) {
            for (int j = i + 1; j < soli.size(); j++) {
                distancia += archivo.getMatriz()[soli.get(i)][soli.get(j)];
            }
        }

        return distancia;
    }

    private double FactorCoste(double CosteActual, int i, int j) {
        double costeMenor = 0.0, costeMayor = 0.0;

        for (int k = 0; k < archivo.getTamSolucion(); k++) {
            if (sol.get(k) != i) {
                costeMenor += archivo.getMatriz()[i][sol.get(k)];
            }
            if (sol.get(k) != i) {
                costeMayor += archivo.getMatriz()[j][i];
            }
        }
        return CosteActual - costeMenor + costeMayor;
    }

    private int obtenerPosicionAporteMenor() {
        int pos = 0;
        double menor = 999999999;
        for (int i = 0; i < aportes.size(); ++i) {
            if (!(marcados.get(i)) && aportes.get(i) < menor) {
                menor = aportes.get(i);
                pos = i;
            }
        }
        return pos;
    }

    public String getLog() {
        return log.toString();
    }

    private void generarSoluciones(Integer cuantas, Vector<Integer> soluciones, Vector<Integer> SolucionActual, ConcurrentLinkedQueue memC) {
        while (cuantas > 0) {
            int vecino = aleatorio.nextInt(archivo.getTamMatriz());
            if (!memC.contains(vecino) && !SolucionActual.contains(vecino)) {
                soluciones.add(vecino);
                cuantas--;
            }
        }
    }
}
