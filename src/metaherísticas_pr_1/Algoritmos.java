/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.Arrays;
import static java.util.Collections.list;
import java.util.LinkedList;
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

    private String algoritmo;
    private CargaDatos archivo;
    private CountDownLatch cdl;
    private Configurador config;
    private Long semilla;
    private Random aleatorio;

    private Vector<Integer> solucion;

    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo, Configurador config) {
        this.archivo = archivo;
        this.cdl = cdl;
        this.semilla = semilla;
        this.algoritmo = algoritmo;
        this.config = config;
        this.aleatorio = new Random(semilla);

        this.solucion = new Vector<Integer>();
    }

    @Override
    public Vector<Integer> call() throws Exception {
        double coste = 0.0;
        long start, stop;
        switch (algoritmo) {
            case ("Greedy"):
                start = System.currentTimeMillis();
//                coste = Greedy();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): greedy" + solucion.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                        + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                break;

            case ("Búsqueda_Local"):
                start = System.currentTimeMillis();
//                coste = BusquedaLocal();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): busqueda local" + solucion.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste + "\n\n");

                break;

            case ("Búsqueda_Tabú"):
                start = System.currentTimeMillis();
                coste = BusquedaTabu();
                stop = System.currentTimeMillis();
                System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                        + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): busqueda tabu" + solucion.toString()
                        + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste + "\n\n");
                break;
        }
        cdl.countDown();

        return solucion;
    }

    //Algoritmos
    private double Greedy() {
        double mayordist = 0.0;

        Boolean[] marcados = new Boolean[archivo.getTamMatriz()];
        Arrays.fill(marcados, Boolean.FALSE);

        Integer punto = aleatorio.nextInt(archivo.getTamMatriz());
        marcados[punto] = true;

        solucion.add(punto);

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
            solucion.add(punto);

            mayordist = 0.0;
        }

        return coste(archivo.getMatriz(), archivo.getTamSolucion());
    }

    private double BusquedaLocal() {
        Integer tamañoSolucion = archivo.getTamSolucion();
        double[][] matriz = archivo.getMatriz();
        Integer tamañoMatriz = archivo.getTamMatriz();
        Integer numIteraciones = config.getEvaluaciones();

        generarSolucionAleatoria(tamañoSolucion, tamañoMatriz);
        double CosteActual = coste(matriz, tamañoSolucion);
        double NuevoCoste;

        Vector<Double> aportes = new Vector<>();
        Vector<Boolean> seleccionados = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            aportes.add(0.0);
            seleccionados.add(false);
        }

        Integer iteracion = 0;
        Integer posicion;
        boolean mejora = true;

        while (iteracion < numIteraciones && mejora) {

            mejora = false;

            actualizarVectorAportes(matriz, aportes, tamañoSolucion);

            for (int k = 0; k < tamañoSolucion && iteracion < numIteraciones; k++) {

                posicion = obtenerPosicionAporteMenor(aportes, seleccionados);

                for (int i = 0; i < tamañoMatriz && !mejora && iteracion < numIteraciones; i++) {
                    if (!solucionContiene(i)) {
                        NuevoCoste = factorizacion(matriz, tamañoSolucion, CosteActual, solucion.get(posicion), i);

//                        System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaLocal(): " + evaluacion + " / " + numEvaluaciones);
                        if (NuevoCoste > CosteActual) {
                            intercambia(posicion, i);

                            CosteActual = NuevoCoste;
                            mejora = true;
                        }

                        iteracion++;
                    }
                }

                if (mejora) {

                    break;
                }
            }

            seleccionados.clear();
            aportes.clear();

            for (int i = 0; i < tamañoSolucion; i++) {
                aportes.add(0.0);
                seleccionados.add(false);
            }
        }

        return CosteActual;
    }

    //Funciones auxiliares
    private double costePuntoEnSolucion(Integer punto) {
        double distancia = 0.0;

        for (int i = 0; i < solucion.size(); i++) {
            distancia += archivo.getMatriz()[punto][solucion.get(i)];
        }

        return distancia;
    }

    private double coste(double[][] matriz, Integer tamañoSolucion) {
        double coste = 0.0;

        for (int i = 0; i < tamañoSolucion; i++) {
            for (int j = i + 1; j < tamañoSolucion; j++) {
                coste += matriz[solucion.get(j)][solucion.get(i)];
            }
        }

        return coste;
    }

    private double factorizacion(double[][] matriz, int tamañoSolucion, double costeActual, int ele1, int ele2) {
        double costeResta = 0.0;
        double costeSuma = 0.0;

        for (int k = 0; k < tamañoSolucion; k++) {

            if (solucion.get(k) != ele1) {
                costeResta += matriz[ele1][solucion.get(k)];
            }

            if (solucion.get(k) != ele1) {
                costeSuma += matriz[ele2][solucion.get(k)];
            }
        }

        return costeActual - costeResta + costeSuma;
    }

    private void actualizarVectorAportes(double[][] matriz, Vector<Double> aportes, Integer tamañoSolucion) {

        for (int i = 0; i < tamañoSolucion; i++) {
            double aporte = 0.0;

            for (int j = 0; j < tamañoSolucion; j++) {
                if (solucion.get(i) != solucion.get(j)) {
                    aporte += matriz[solucion.get(i)][solucion.get(j)];
                }
            }

            aportes.setElementAt(aporte, i);
        }
    }

    private Integer obtenerPosicionAporteMenor(Vector<Double> aportes, Vector<Boolean> marcados) {
        Integer posicionAporteMenor = 0;
        double menor = 999999999;

        for (int i = 0; i < aportes.size(); i++) {
            if ((!marcados.get(i) && aportes.get(i) < menor)) {
                menor = aportes.get(i);
                posicionAporteMenor = i;
            }
        }

        marcados.setElementAt(Boolean.TRUE, posicionAporteMenor);

        return posicionAporteMenor;
    }

    private boolean solucionContiene(Integer cual) {
        for (int i = 0; i < solucion.size(); i++) {
            if (cual == solucion.get(i)) {

                return true;
            }
        }

        return false;
    }

    private boolean vectorContiene(Vector<Integer> vector, Integer cual) {
        for (int i = 0; i < vector.size(); i++) {
            if (cual == vector.get(i)) {

                return true;
            }
        }

        return false;
    }

    private void intercambia(int i, int j) {
        solucion.setElementAt(j, i);
    }

    private void intercambia(Vector<Integer> vector, int i, int j) {
        vector.set(i, j);
    }

    private void generarSolucionAleatoria(int tamañoSolucion, int tamañoMatriz) {
        Integer generados = 0;

        while (generados < tamañoSolucion) {
            Integer elemento = aleatorio.nextInt(tamañoMatriz - 1);
            if (!solucionContiene(elemento)) {
                solucion.add(elemento);
                generados++;
            }
        }
    }

    private Integer menorAporte(int m, double[][] dist) {
        double peso = 0.0;
        Integer posMenor = 0;
        double menor = 999999999;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (solucion.get(i) != solucion.get(j)) {
                    peso += dist[solucion.get(i)][solucion.get(j)];
                }
            }

            if (peso < menor) {
                menor = peso;
                posMenor = i;
            }
            peso = 0.0;
        }

        return posMenor;
    }

    private Vector<Integer> calculaVectorMasFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {

        Vector<Integer> masFrecuentes = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            Integer mayor = -1;
            Integer posicion = null;

            for (int j = 0; j < tamañoMatriz; j++) {

                if (memoriaLargoPlazo.get(j) > mayor && !vectorContiene(masFrecuentes, j)) {
                    mayor = memoriaLargoPlazo.get(j);
                    posicion = j;
                }
            }

            masFrecuentes.add(posicion);
            memoriaLargoPlazo.set(posicion, 0);
        }

        return masFrecuentes;
    }

    private Vector<Integer> calculaVectorMenosFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {

        Vector<Integer> menosFrecuentes = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            Integer menor = 999999999;
            Integer posicion = null;

            for (int j = 0; j < tamañoMatriz; j++) {

                if (memoriaLargoPlazo.get(j) < menor && !vectorContiene(menosFrecuentes, j)) {
                    menor = memoriaLargoPlazo.get(j);
                    posicion = j;
                }
            }

            menosFrecuentes.add(posicion);
            memoriaLargoPlazo.set(posicion, 0);
        }

        return menosFrecuentes;
    }

    private double BusquedaTabu() {

        Integer evaluacion = 0;
        Integer contadorReinicializacion = 0;
        Integer posicion;
        double costeF;
        double costeMejorSolucion;
        double costeSolucionActual;
        double costeParcial;
        Vector<Integer> Parcial = null;
        Vector<Integer> mejorSolucion = null;

        generarSolucionAleatoria(archivo.getTamSolucion(), archivo.getTamMatriz());

        Vector<Boolean> marcados = new Vector<>();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            marcados.add(Boolean.FALSE);
        }

        costeMejorSolucion = coste(archivo.getMatriz(), archivo.getTamSolucion());
        costeSolucionActual = costeMejorSolucion;

        ConcurrentLinkedQueue<Integer> listaTabu = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < config.getTenencia(); i++) {
            listaTabu.offer(-1);
        }

        Vector<Integer> memoriaLargoPlazo = new Vector<>();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memoriaLargoPlazo.add(i);
        }

        while (evaluacion < config.getEvaluaciones()) {
//            System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): " + evaluacion );

            if (evaluacion == 49999) {
                System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): " + evaluacion);
            }

            int numVecinos = 10;
            
            posicion = menorAporte(archivo.getTamSolucion(), archivo.getMatriz());
            
            marcados.clear();
            for (int i = 0; i < archivo.getTamMatriz(); i++) {
                marcados.add(Boolean.FALSE);
            }

            int elementoAnterior = solucion.get(posicion);
            costeParcial = 0;

            while (numVecinos > 0) {
                int vecino;
                do {
                    vecino = aleatorio.nextInt(archivo.getTamMatriz() - 1);
                } while (marcados.get(vecino));
                marcados.set(vecino, Boolean.TRUE);
                if (!solucionContiene(vecino)) {

                    boolean tabu = false;
                    for (Integer integer : listaTabu) {
                        if (integer == vecino) {
                            tabu = true;
                            break;
                        }
                    }

                    if (!tabu) {

                        costeF = factorizacion(archivo.getMatriz(), archivo.getTamSolucion(), costeSolucionActual, elementoAnterior, vecino);
                        numVecinos--;
                        if (costeParcial < costeF) {

                            costeParcial = costeF;
                            Parcial = solucion;
                            intercambia(Parcial, posicion, vecino);
                        }
                    }

                }
            }

            solucion = Parcial;
            costeSolucionActual = costeParcial;
            evaluacion++;

            listaTabu.offer(elementoAnterior);
            listaTabu.remove();

            for (int i = 0; i < archivo.getTamSolucion(); i++) {
                memoriaLargoPlazo.set(solucion.get(i), memoriaLargoPlazo.get(solucion.get(i)) + 1);
            }

            if (costeSolucionActual > costeMejorSolucion) {
                costeMejorSolucion = costeSolucionActual;
                mejorSolucion = solucion;

                contadorReinicializacion = 0;
            } else {
                contadorReinicializacion++;
            }

            if (contadorReinicializacion == 100) {
                contadorReinicializacion = 0;

                solucion = reiniciar(memoriaLargoPlazo);

                costeSolucionActual = coste(archivo.getMatriz(), archivo.getTamSolucion());

                if (costeSolucionActual > costeMejorSolucion) {
                    costeMejorSolucion = costeSolucionActual;
                    mejorSolucion = solucion;
                }

                memoriaLargoPlazo.clear();
                for (int i = 0; i < archivo.getTamMatriz(); i++) {
                    memoriaLargoPlazo.add(0);
                }
                           
                listaTabu.clear();
                for (int i = 0; i < config.getTenencia(); i++) {
                    listaTabu.add(-1);
                }

                evaluacion++;
            }

        }
        solucion = mejorSolucion;
        return coste(archivo.getMatriz(), archivo.getTamSolucion());
    }

    private Vector<Integer> reiniciar(Vector<Integer> memoriaLargoPlazo) {
        double tirada = aleatorio.nextInt(2);

        if (tirada <= 0.5) {
            
            return calculaVectorMasFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        } else {
            
            return calculaVectorMenosFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        }
    }
}
