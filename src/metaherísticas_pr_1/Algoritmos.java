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
//                Tiempo.Stop();
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

        Integer iteracion = 0;
        Integer anterior = 0;
        Integer posAporteMenor = 0;
        double costeAnterior = 0;
        boolean mejora = true;
        contadorMarcados = 0;

        generarSolucionAleatoria();

        actualizarCostes();

        while (iteracion < config.getEvaluaciones() && mejora && contadorMarcados < sol.size()) {
            mejora = false;

            posAporteMenor = obtenerPosicionAporteMenor();

            guardarSolucionAnterior(anterior, costeAnterior, posAporteMenor);
            eliminarPuntoSolucion(posAporteMenor);

            for (int i = 0; i < archivo.getTamMatriz() && iteracion < config.getEvaluaciones(); i++) {
                if (!sol.contains(i)) {
                    if (costeAnterior < costePuntoEnSolucion(i)) {
                        sol.insertElementAt(i, posAporteMenor);
                        aportes.insertElementAt(costePuntoEnSolucion(i), posAporteMenor);
                        mejora = true;
                        iteracion++;

                        break;
                    }
                }

                iteracion++;
            }

            if (mejora) {
                desmarcarElementos();
            }

            if (sol.size() < archivo.getTamSolucion()) {
                restablecerSolucionAnterior(anterior, posAporteMenor, costeAnterior, mejora);
            }
        }

        return costeSolucion();
    }

    private double BusquedaTabu() {
        generarSolucionAleatoria();
        actualizarCostes();

        ConcurrentLinkedQueue<Integer> memC = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < config.getTenencia(); i++) {
            memC.add(-1);
        }

        Integer[] memL = new Integer[archivo.getTamMatriz()];
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memL[i] = 0;
            if(sol.contains(i)){
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
        
        while (iteracion < config.getEvaluaciones()) {
            Vector<Integer> soluciones = null;
            double costeActual = 0.0;
            posAporteMenor = obtenerPosicionAporteMenor();
            int elemento = 0;
//            costeAnterior = costeSolucion();
//            eliminarPuntoSolucion(posAporteMenor);
            memC.offer(sol.get(posAporteMenor));
            memC.poll();
//            SolucionParcial = SolucionActual;
            
            generarSoluciones(numVecinos, soluciones, memC);

            for (int i = 0; i < numVecinos; i++) {
                SolucionActual.set(posAporteMenor, soluciones.get(i));
                if (costeActual < coste(SolucionActual)){
                    costeActual = coste(SolucionActual);
                    elemento = soluciones.get(i);
                }
            }
            SolucionActual.set(posAporteMenor, elemento);
            
            if(costeSolucion() < coste(SolucionActual)){
                sol = SolucionActual;
            }

//            guardarSolucionAnterior(anterior, costeAnterior, posAporteMenor);
//            eliminarPuntoSolucion(posAporteMenor);
            
            

        }

        return costeSolucion();
    }

    //Funciones auxiliares
    private void restablecerSolucionAnterior(Integer anterior, Integer posAporteMenor, double costeAnterior, boolean mejora) {
        sol.insertElementAt(anterior, posAporteMenor);
        aportes.insertElementAt(costeAnterior, posAporteMenor);
        marcados.removeElementAt(posAporteMenor);
        marcados.insertElementAt(Boolean.TRUE, posAporteMenor);
        contadorMarcados++;
        mejora = true;
    }

    private void guardarSolucionAnterior(Integer anterior, double costeAnterior, Integer posAporteMenor) {
        anterior = sol.get(posAporteMenor);
        costeAnterior = aportes.get(posAporteMenor);
    }

    private void eliminarPuntoSolucion(Integer posAporteMenor) {
        sol.removeElementAt(posAporteMenor);
        aportes.removeElementAt(posAporteMenor);
    }

    private void actualizarCostes() {
        aportes.removeAllElements();
        marcados.removeAllElements();
        for (Integer integer : sol) {
            aportes.add(costePuntoEnSolucion(integer));
            marcados.add(false);
        }
    }

    private void generarSolucionAleatoria() {
        for (int i = 0; i < archivo.getTamSolucion(); i++) {
//            sol.add(aleatorio.Randint(0, archivo.getTamMatriz()));
            sol.add(aleatorio.nextInt(archivo.getTamMatriz()));
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

    private double CosteSustituto(Vector<Integer> parcial, Integer punto) {
        double distancia = 0.0;

        for (int i = 0; i < parcial.size(); i++) {
            distancia += archivo.getMatriz()[parcial.get(i)][parcial.get(punto)];
        }

        return distancia;
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

    private void desmarcarElementos() {
        marcados.removeAllElements();
        for (int i = 0; i < sol.size(); i++) {
            marcados.add(i, Boolean.FALSE);
        }

        contadorMarcados = 0;
    }

    private void generarSoluciones(int cuantas, Vector<Integer> soluciones, ConcurrentLinkedQueue memC) {
        soluciones = new Vector<Integer>();
        while (cuantas > 0) {
            int vecino = aleatorio.nextInt(archivo.getTamMatriz());
            if(!memC.contains(vecino) && !sol.contains(vecino)){
                soluciones.add(vecino);
                cuantas--;
            }
        }
    }
}
