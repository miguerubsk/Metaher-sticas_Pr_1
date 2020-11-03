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
                    if (!solucion.contains(i)) {
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
    
    private double BusquedaTabu() {
        
        Integer evaluacion = 0;
        Integer contadorReinicializacion = 0;
        Integer posicion;
        double costeF;
        double costeMejorSolucion;
        double costeSolucionActual;
        double costeSolucionParcial;
        Vector<Integer> solucionParcial = null;
        Vector<Integer> mejorSolucion = null;
        Vector<Integer> solucionActual = null;
        
        generarSolucionAleatoria(archivo.getTamSolucion(), archivo.getTamMatriz());
        mejorSolucion = solucion;
        solucionActual = mejorSolucion;
        
        Vector<Boolean> marcados = new Vector<>();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            marcados.add(Boolean.FALSE);
        }
        
        costeMejorSolucion = coste(archivo.getMatriz(), archivo.getTamSolucion(), mejorSolucion);
        costeSolucionActual = costeMejorSolucion;
        
        ConcurrentLinkedQueue<Integer> listaTabu = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < config.getTenencia(); i++) {
            listaTabu.offer(-1);
        }
        
        Vector<Integer> memoriaLargoPlazo = new Vector<>();
        for (int i = 0; i < archivo.getTamMatriz(); i++) {
            memoriaLargoPlazo.add(0);
        }
        try {
            while (evaluacion < 50000) {
//            System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): " + evaluacion);

//                if (evaluacion == 1034) {
//                    System.out.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu()");
//                }
                int numVecinos = 10;
                
                posicion = menorAporte(archivo.getTamSolucion(), archivo.getMatriz(), solucionActual);
                
                marcados.clear();
                for (int i = 0; i < archivo.getTamMatriz(); i++) {
                    marcados.add(Boolean.FALSE);
                }
                
                int elementoAnterior = solucionActual.get(posicion);
                costeSolucionParcial = 0;
                
                while (numVecinos > 0) {
                    int vecino;
                    do {
                        vecino = aleatorio.nextInt(archivo.getTamMatriz());
                    } while (marcados.get(vecino));
                    marcados.set(vecino, Boolean.TRUE);
                    if (!solucionActual.contains(vecino)) {
                        
//                        boolean tabu = false;
//                        for (Integer integer : listaTabu) {
//                            if (integer == vecino) {
//                                tabu = true;
//                                break;
//                            }
//                        }
                        
                        if (!listaTabu.contains(vecino)) {
                            
                            costeF = factorizacion(archivo.getMatriz(), archivo.getTamSolucion(), costeSolucionActual, elementoAnterior, vecino, solucionActual);
                            numVecinos--;
                            if (costeSolucionParcial < costeF) {
                                
                                costeSolucionParcial = costeF;
                                solucionParcial = solucion;
                                intercambia(solucionParcial, posicion, vecino);
                            }
                        }
                    }
                }
                
                solucionActual = solucionParcial;
                costeSolucionActual = costeSolucionParcial;
                evaluacion++;
                
                listaTabu.offer(elementoAnterior);
                listaTabu.remove();
                
                actualizarMemoriaLargoPlazo(memoriaLargoPlazo, solucionActual);
                
                if (costeSolucionActual > costeMejorSolucion) {
                    costeMejorSolucion = costeSolucionActual;
                    mejorSolucion = solucionActual;
                    
                    contadorReinicializacion = 0;
                } else {
                    contadorReinicializacion++;
                }
                
                if (contadorReinicializacion == 100) {
                    contadorReinicializacion = 0;
                    
                    solucionActual = reiniciar(memoriaLargoPlazo);
                    
                    costeSolucionActual = coste(archivo.getMatriz(), archivo.getTamSolucion(), solucionActual);
                    
                    if (costeSolucionActual > costeMejorSolucion) {
                        costeMejorSolucion = costeSolucionActual;
                        mejorSolucion = solucionActual;
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
        } catch (Exception e) {
            System.err.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): " + e.toString() + ". Iteracion: " + evaluacion + ". Contador reinicializacion: " + contadorReinicializacion);
        }
        
        return coste(archivo.getMatriz(), archivo.getTamSolucion());
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
    
    private double coste(double[][] matriz, Integer tamañoSolucion, Vector<Integer> vector) {
        double coste = 0.0;
        
        for (int i = 0; i < tamañoSolucion; i++) {
            for (int j = i + 1; j < tamañoSolucion; j++) {
                coste += matriz[vector.get(j)][vector.get(i)];
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

//    private boolean solucionContiene(Integer cual) {
//        for (int i = 0; i < solucion.size(); i++) {
//            if (cual == solucion.get(i)) {
//
//                return true;
//            }
//        }
//
//        return false;
//    }
//    private boolean solucionContiene(Vector<Integer> vector, Integer cual) {
//        for (int i = 0; i < vector.size(); i++) {
//            if (cual == vector.get(i)) {
//                
//                return true;
//            }
//        }
//        
//        return false;
//    }
    
//    private boolean vectorContiene(Vector<Integer> vector, Integer cual) {
//        for (int i = 0; i < vector.size(); i++) {
//            if (cual == vector.get(i)) {
//                
//                return true;
//            }
//        }
//        
//        return false;
//    }
    
    private void intercambia(int i, int j) {
        solucion.setElementAt(j, i);
    }
    
    private void intercambia(Vector<Integer> vector, int i, int j) {
//        System.err.println(vector);
        if (!vector.contains(j)) {
            vector.set(i, j);
        }
//        System.err.println(vector);
    }
    
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

//    private Integer menorAporte(int m, double[][] dist) {
//        double peso = 0.0;
//        Integer posMenor = 0;
//        double menor = 999999999;
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < m; j++) {
//                if (solucion.get(i) != solucion.get(j)) {
//                    peso += dist[solucion.get(i)][solucion.get(j)];
//                }
//            }
//
//            if (peso < menor) {
//                menor = peso;
//                posMenor = i;
//            }
//            peso = 0.0;
//        }
//
//        return posMenor;
//    }
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
    
    private Vector<Integer> calculaVectorMasFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {
        
        Vector<Integer> masFrecuentes = new Vector<>();
        Vector<Integer> memoriaLargoPlazoBis = memoriaLargoPlazo;
        
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
    
    private Vector<Integer> calculaVectorMenosFrecuentes(Vector<Integer> memoriaLargoPlazo, int tamañoMatriz, int tamañoSolucion) {
        
        Vector<Integer> menosFrecuentes = new Vector<>();
        Vector<Integer> memoriaLargoPlazoBis = memoriaLargoPlazo;
        
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
    
    private void actualizarMemoriaLargoPlazo(Vector<Integer> memoriaLargoPlazo, Vector<Integer> vector) {
        for (int i = 0; i < archivo.getTamSolucion(); i++) {
            memoriaLargoPlazo.set(vector.get(i), memoriaLargoPlazo.get(vector.get(i)) + 1);
        }
    }
}
