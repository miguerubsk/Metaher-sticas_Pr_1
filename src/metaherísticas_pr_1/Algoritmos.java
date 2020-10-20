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
import java.util.concurrent.CountDownLatch;
import tools.Configurador;
//import tools.Random;
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
    int contadorMarcados;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo, Configurador config) {
        this.archivo = archivo;
        this.config = config;
        this.cdl = cdl;
        this.aportes = new Vector<>();
        this.marcados = new Vector<>();
        this.semilla = semilla;
        aleatorio = new Random();
//        aleatorio.Set_random(semilla);
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

//        Integer punto = aleatorio.Randint(0, archivo.getTamMatriz() - 1);
        Integer punto = aleatorio.nextInt(archivo.getTamMatriz());
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
        Integer iteracion = 0;

        generarSolucionAleatoria();
        double costeActual = costeSolucion();

        Integer anterior = 0;
        Integer posAporteMenor = 0;
        double costeAnterior = 0;
        boolean mejora = true;
        contadorMarcados = 0;
        actualizarCostes();
        while (iteracion < config.getEvaluaciones() && mejora && contadorMarcados < sol.size()) {
//            System.out.println("iteracion: " + iteracion + "\n contadorMarcados:" + contadorMarcados);
            mejora = false;

            posAporteMenor = posicionAporteMenor();
            anterior = sol.get(posAporteMenor);
            costeAnterior = aportes.get(posAporteMenor);

            sol.removeElementAt(posAporteMenor);
            aportes.removeElementAt(posAporteMenor);

            for (int i = 0; i < archivo.getTamMatriz() && iteracion < config.getEvaluaciones(); i++) {
                if (!sol.contains(i)) {
                    if (costeAnterior < costePuntoEnSolucion(i)) {
                        sol.insertElementAt(i, posAporteMenor);
                        aportes.insertElementAt(costePuntoEnSolucion(i), posAporteMenor);
                        mejora = true;
                        iteracion++;
                        if (iteracion > 50000) {
                            System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaLocal()");
                        }
                        break;
                    }
                }

                iteracion++;
            }
            if (iteracion > 50000) {
                System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaLocal()");
            }
            if (mejora) {
                desmarcarElementos();
            }

            if (sol.size() < archivo.getTamSolucion()) {
                sol.insertElementAt(anterior, posAporteMenor);
                aportes.insertElementAt(costeAnterior, posAporteMenor);
                marcados.removeElementAt(posAporteMenor);
                marcados.insertElementAt(Boolean.TRUE, posAporteMenor);
                contadorMarcados++;
                mejora = true;
            }
        }

        return costeSolucion();
    }

    private double BusquedaTabu() {
        //TODO
        generarSolucionAleatoria();
        throw new UnsupportedOperationException("No soportado.");
//        return CosteSolución();
    }

    //Funciones auxiliares
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
        return pos;
    }

    private int posicionAporteMenor2() {
        int pos = 0;
        double menor = 999999999;
        for (int i = 0; i < aportes.size(); ++i) {
            if (aportes.get(i) < menor) {
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
}
