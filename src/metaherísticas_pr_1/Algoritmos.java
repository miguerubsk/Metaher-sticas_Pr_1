/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import tools.CargaDatos;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import tools.Configurador;

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
        try {
            double coste = 0.0;
            long start, stop;
            switch (algoritmo) {
                case ("Greedy"):
                    start = System.currentTimeMillis();
                    coste = Greedy();
                    stop = System.currentTimeMillis();
                    System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                            + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): "+ algoritmo + solucion.toString()
                            + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                            + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                    break;

                case ("Búsqueda_Local"):
                    start = System.currentTimeMillis();
                    coste = BusquedaLocal();
                    stop = System.currentTimeMillis();
                    System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                            + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): "+ algoritmo + solucion.toString()
                            + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                            + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                    break;

                case ("Búsqueda_Tabú"):
                    start = System.currentTimeMillis();
                    coste = BusquedaTabu();
                    stop = System.currentTimeMillis();
                    Collections.sort(solucion);
                    System.out.println("Archivo: " + archivo.getNombreFichero() + "\nSemilla: "
                            + semilla + "\nmetaherísticas_pr_1.Algoritmos.run(): "+ algoritmo + solucion.toString()
                            + "\nTiempo: " + ((stop - start)) + " ms" + "\nCoste Solución: " + coste
                            + "\nDatos: " + archivo.getTamMatriz() + ";" + archivo.getTamSolucion() + "\n\n");
                    break;
            }

        } catch (Exception e) {
            System.err.println("metaherísticas_pr_1.Algoritmos.call(): excepcion: " + e.getMessage() + e.toString() + e.getLocalizedMessage());
        } finally {
            cdl.countDown();
        }

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

    /**
     * @brief Función que ejecuta el algoritmo Búsqueda Local
     */
    private double BusquedaLocal() {
        Integer tamañoSolucion = archivo.getTamSolucion();
        double[][] matriz = archivo.getMatriz();
        Integer tamañoMatriz = archivo.getTamMatriz();
        Integer numIteraciones = config.getEvaluaciones();

        generarSolucionAleatoria(tamañoSolucion, tamañoMatriz);
        double costeActual = coste(matriz, tamañoSolucion);
        double nuevoCoste;

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
                    if (!solucion.contains(i)) {
                        nuevoCoste = factorizacion(matriz, tamañoSolucion, costeActual, solucion.get(posicion), i);
                        
                        if (nuevoCoste > costeActual) {
                            intercambia(posicion, i);

                            costeActual = nuevoCoste;
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

        return costeActual;
    }

    /**
     * @brief Función que ejecuta el algoritmo Búsqueda Tabú
     */
    private double BusquedaTabu() {
        Integer tamañoSolucion = archivo.getTamSolucion();
        Integer tamañoMatriz = archivo.getTamMatriz();
        double[][] matriz = archivo.getMatriz();
        
        Integer evaluacion = 0;
        Integer contadorReinicializacion = 0;
        Integer posicion;
        double costeF = 0.0;
        double costeMejorSolucion;
        double costeSolucionActual;
        double costeSolucionParcial;
        
        Vector<Integer> solucionParcial = new Vector<>(tamañoSolucion);
        Vector<Integer> mejorSolucion = new Vector<>(tamañoSolucion);
        Vector<Integer> solucionActual = new Vector<>(tamañoSolucion);
        ConcurrentLinkedQueue<Integer> listaTabu = new ConcurrentLinkedQueue<>();
        Vector<Integer> memoriaLargoPlazo = new Vector<>();

        generarSolucionAleatoria(tamañoSolucion, tamañoMatriz);
        mejorSolucion = solucion;
        solucionActual = mejorSolucion;

        Vector<Boolean> marcados = new Vector<>();
        for (int i = 0; i < tamañoMatriz; i++) {
            marcados.add(Boolean.FALSE);
        }

        costeMejorSolucion = coste(matriz, tamañoSolucion, mejorSolucion);
        costeSolucionActual = costeMejorSolucion;

        iniciarMemorias(listaTabu, memoriaLargoPlazo);
        try {
            while (evaluacion < 50000) {
                Integer numVecinos = 10;

                posicion = menorAporte(tamañoSolucion, matriz, solucionActual);

                limpiarMarcados(marcados);

                int elementoAnterior = solucionActual.get(posicion);
                costeSolucionParcial = 0;

                evaluacion++;
                solucionActual = evaluarVecinos(numVecinos, marcados, costeF, solucionActual, costeSolucionActual, listaTabu, solucionParcial, costeSolucionParcial, elementoAnterior, posicion);;
                costeSolucionActual = coste(matriz, tamañoSolucion, solucionActual);

                actualizarMemoriaLargoPlazo(memoriaLargoPlazo, solucionActual);
                actualizarListaTabu(listaTabu, elementoAnterior);

                if (costeSolucionActual > costeMejorSolucion) {
                    costeMejorSolucion = costeSolucionActual;
                    mejorSolucion = solucionActual;

                    contadorReinicializacion = 0;
                } else {

                    contadorReinicializacion++;
                }

                if (contadorReinicializacion == 100) {

                    solucionActual = reiniciar(memoriaLargoPlazo);

                    costeSolucionActual = coste(matriz, tamañoSolucion, solucionActual);

                    if (costeSolucionActual > costeMejorSolucion) {
                        costeMejorSolucion = costeSolucionActual;
                        mejorSolucion = solucionActual;
                    }

                    reiniciarMemorias(memoriaLargoPlazo, listaTabu);

                    evaluacion++;
                    contadorReinicializacion = 0;
                }
            }
            
            solucion = mejorSolucion;
        } catch (Exception e) {
            System.err.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): " + e.toString() + ". Iteracion: " + evaluacion + ". Contador reinicializacion: " + contadorReinicializacion);
        }

        return coste(matriz, tamañoSolucion);
    }

    //Funciones auxiliares
    
    /**
     * @brief Función que calcula el coste de un punto en una solucion
     * @param punto del que se quiere saber el coste
     * @return Coste de un punto en una solucion
     */
    private double costePuntoEnSolucion(Integer punto) {
        double distancia = 0.0;

        for (int i = 0; i < solucion.size(); i++) {
            distancia += archivo.getMatriz()[punto][solucion.get(i)];
        }

        return distancia;
    }

    /**
     * @brief Función que calcula el coste de la solucion
     * @param matriz matriz de distancias
     * @param tamañoSolucion tamaño de la solucion
     * @return Coste de la solucion
     */
    private double coste(double[][] matriz, Integer tamañoSolucion) {
        double coste = 0.0;

        for (int i = 0; i < tamañoSolucion; i++) {
            for (int j = i + 1; j < tamañoSolucion; j++) {
                coste += matriz[solucion.get(j)][solucion.get(i)];
            }
        }

        return coste;
    }

    /**
     * @brief Función que calcula el coste de una solucion que se le pase como parametro
     * @param matriz matriz de distancias
     * @param tamañoSolucion tamaño de la solucion
     * @param vector una solucion
     * @return Coste de una solucion
     */
    private double coste(double[][] matriz, Integer tamañoSolucion, Vector<Integer> vector) {
        double coste = 0.0;

        for (int i = 0; i < tamañoSolucion; i++) {
            for (int j = i + 1; j < tamañoSolucion; j++) {
                coste += matriz[vector.get(j)][vector.get(i)];
            }
        }

        return coste;
    }
    
    
    /**
     * @brief Función que crealiza la factorizacion de la solucion al cambiar un elemento por otro que no este en la solucion
     * @param matriz matriz de distancias
     * @param tamañoSolucion tamaño de la solucion
     * @param costeActual coste actual de la solucion
     * @param ele1 elemento que sale
     * @param ele2 elemento que entra
     * @return coste de la solucion con el nuevo elemento
     */
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
    
    /**
     * @brief Función que crealiza la factorizacion de una solucion al cambiar un elemento por otro que no este en la solucion
     * @param matriz matriz de distancias
     * @param tamañoSolucion tamaño de la solucion
     * @param costeActual coste actual de la solucion
     * @param ele1 elemento que sale
     * @param ele2 elemento que entra
     * @param vector una solucion
     * @return coste de una solucion con el nuevo elemento
     */
    private double factorizacion(double[][] matriz, int tamañoSolucion, double costeActual, int ele1, int ele2, Vector<Integer> vector) {
        double costeResta = 0.0;
        double costeSuma = 0.0;

        for (int k = 0; k < tamañoSolucion; k++) {

            if (vector.get(k) != ele1) {
                costeResta += matriz[ele1][vector.get(k)];
            }

            if (vector.get(k) != ele1) {
                costeSuma += matriz[ele2][vector.get(k)];
            }
        }

        return costeActual - costeResta + costeSuma;
    }

    /**
     * @brief Actualiza el vector de aportes respecto de la solucion
     * @param matriz matriz de distancias
     * @param aportes vector de aportes de cada elemento individual
     * @param tamañoSolucion tamaño de la solucion
     */
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

    /**
     * @brief Funcion que obtiene la posicion de menor aporte de la solucion
     * @param aportes vector de aportes de cada elemento individual
     * @param marcados vector de marcados
     * @return posicion de menor aporte
     */
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

    /**
     * @brief Operador de intercambio que cambia un elemento por otro en la solucion
     * @param i indice del elemento que se quiere modificar
     * @param j elemento que se quiere añadir en la posicion i
     */
    private void intercambia(int i, int j) {
        solucion.setElementAt(j, i);
    }

    /**
     * @brief Operador de intercambio que cambia un elemento por otro en una solucion
     * @param vector una solucion
     * @param i indice del elemento que se quiere modificar
     * @param j elemento que se quiere añadir en la posicion i
     */
    private void intercambia(Vector<Integer> vector, int i, int j) {
        if (!vector.contains(j)) {
            vector.set(i, j);
        }
    }

    /**
     * @brief Genera una solucion aleatoria
     * @param tamañoSolucion tamaño de la solucion
     * @param tamañoMatriz tamaño de la matriz
     */
    private void generarSolucionAleatoria(int tamañoSolucion, int tamañoMatriz) {
        Integer generados = 0;

        while (generados < tamañoSolucion) {
            Integer elemento = aleatorio.nextInt(tamañoMatriz);
            if (!solucion.contains(elemento)) {
                solucion.add(elemento);
                generados++;
            }
        }
    }

    /**
     * @brief Funcion que obtiene la posicion de menor aporte de la solucion
     * @param m tamaño de la solucion
     * @param dist matriz de distancias
     * @param vector una solucion
     * @return posicion de menor aporte
     */
    private Integer menorAporte(int m, double[][] dist, Vector<Integer> vector) {
        double peso = 0.0;
        Integer posMenor = 0;
        double menor = 999999999;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (vector.get(i) != vector.get(j)) {
                    peso += dist[vector.get(i)][vector.get(j)];
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

    /**
     * @brief Funcion que realiza la intensificacion cuando queremos reiniciar la busqueda tabu. Busca los m elementos mas frecuentes
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se repite un elemento a lo largo de las iteraciones
     * @param tamañoMatriz tamaño de la matriz 
     * @param tamañoSolucion tamaño de la solucion
     * @return vector solucion de tamaño m elementos con los mas frecuentes
     */
    private Vector<Integer> calculaVectorMasFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {

        Vector<Integer> masFrecuentes = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            Integer mayorFrecuencia = -1;
            Integer posicion = null;

            for (int j = 0; j < tamañoMatriz; j++) {
                if (memoriaLargoPlazo.get(j) > mayorFrecuencia && !masFrecuentes.contains(j)) {
                    mayorFrecuencia = memoriaLargoPlazo.get(j);
                    posicion = j;
                }
            }

            masFrecuentes.add(posicion);

        }

        return masFrecuentes;
    }

    /**
     * @brief Funcion que realiza la diversificacion cuando queremos reiniciar la busqueda tabu. Busca los m elementos mas frecuentes
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se repite un elemento a lo largo de las iteraciones
     * @param tamañoMatriz tamaño de la matriz 
     * @param tamañoSolucion tamaño de la solucion
     * @return vector solucion de tamaño m elementos con los menos frecuentes
     */
    private Vector<Integer> calculaVectorMenosFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {

        Vector<Integer> menosFrecuentes = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            Integer menorFrecuencia = 999999999;
            Integer posicion = null;

            for (int j = 0; j < tamañoMatriz; j++) {
                if (memoriaLargoPlazo.get(j) < menorFrecuencia && !menosFrecuentes.contains(j)) {
                    menorFrecuencia = memoriaLargoPlazo.get(j);
                    posicion = j;
                }
            }

            menosFrecuentes.add(posicion);

        }

        return menosFrecuentes;
    }

    private Vector<Integer> reiniciar(Vector<Integer> memoriaLargoPlazo) {
        double tirada = aleatorio.nextInt(2);

        if (tirada <= 0.5) {

            return calculaVectorMasFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        } else {

            return calculaVectorMenosFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        }
    }
    
    /**
     * @brief Funcion que actualiza la memoria a largo plazo con los elementos de una solucion
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se repite un elemento a lo largo de las iteraciones
     * @param vector una solucion
     */
    private void actualizarMemoriaLargoPlazo(Vector<Integer> memoriaLargoPlazo, Vector<Integer> vector) {
        for (int i = 0; i < archivo.getTamSolucion(); i++) {
            memoriaLargoPlazo.set(vector.get(i), memoriaLargoPlazo.get(vector.get(i)) + 1);
        }
    }
    
    /**
     * @brief Funcion que actualiza la lista tabu, añadiendo el elemento que se ha eliminado de la solucion y eliminando el elemento mas antiguo
     * @param listaTabu memoria que almacena los elementos de la lista tabu
     * @param tamañoMatriz tamaño de la matriz
     */
    private void actualizarListaTabu(ConcurrentLinkedQueue<Integer> listaTabu, Integer elementoAnterior) {
        listaTabu.offer(elementoAnterior);
        listaTabu.remove();
    }
    
    /**
     * @brief Inicializa las memorias necesarias para la busqueda tabu
     * @param listaTabu memoria que almacena los elementos de la lista tabu
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se repite un elemento a lo largo de las iteraciones
     */
    private void iniciarMemorias(ConcurrentLinkedQueue<Integer> listaTabu, Vector<Integer> memoriaLargoPlazo) {

        for (int i = 0; i < config.getTenencia(); i++) {
            listaTabu.offer(-1);
        }

        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memoriaLargoPlazo.add(0);
        }
    }
    
    /**
     * @brief Limpia las lista de marcados para la proxima seleccion de vecinos
     * @param marcados vector de elementos marcados que indica su estado
     */
    private void limpiarMarcados(Vector<Boolean> marcados) {
        marcados.clear();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            marcados.add(Boolean.FALSE);
        }
    }

    /**
     * @brief Funcion que genera un numVecinos y los evalua respecto de una solucion. Busca la mejor solucion entre los vecinos generados.
     * @param numVecinos numero de vecinos a generar
     * @param marcados vector de elementos marcados para no repetir vecinos 
     * @param costeF coste de la solucion factorizada
     * @param solucionActual vector con la solucion actual que se esta trabajando
     * @param costeSolucionActual coste de la solucion actual
     * @param listaTabu lista de elementos tabu
     * @param solucionParcial vector con la solucion parcial, incluye el vecino que se va a evaluar 
     * @param costeSolucionParcial coste de la solucion parcial
     * @param elementoAnterior elemento que se va a sustituir en la solucion actual
     * @param posicion posicion del elemento anterior
     * @return vector solucion de tamaño m elementos con la mejor solucion de entre los vecinos
     */
    private Vector<Integer> evaluarVecinos(Integer numVecinos, Vector<Boolean> marcados, double costeF,
            Vector<Integer> solucionActual, double costeSolucionActual, ConcurrentLinkedQueue<Integer> listaTabu,
            Vector<Integer> solucionParcial, double costeSolucionParcial, Integer elementoAnterior, Integer posicion) {
        while (numVecinos > 0) {
            int vecino;
            do {
                vecino = aleatorio.nextInt(archivo.getTamMatriz());
            } while (marcados.get(vecino));
            marcados.set(vecino, Boolean.TRUE);
            if (!solucionActual.contains(vecino)) {
                if (!listaTabu.contains(vecino)) {

                    costeF = factorizacion(archivo.getMatriz(), archivo.getTamSolucion(), costeSolucionActual, elementoAnterior, vecino, solucionActual);
                    numVecinos--;
                    if (costeSolucionParcial < costeF) {

                        costeSolucionParcial = costeF;
                        solucionParcial = solucionActual;
                        intercambia(solucionParcial, posicion, vecino);
                    }
                }
            }
        }

        return solucionParcial;
    }

    private void reiniciarMemorias(Vector<Integer> memoriaLargoPlazo, ConcurrentLinkedQueue<Integer> listaTabu) {
        memoriaLargoPlazo.clear();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memoriaLargoPlazo.add(0);
        }

        listaTabu.clear();
        for (int i = 0; i < config.getTenencia(); i++) {
            listaTabu.add(-1);
        }
    }
}
