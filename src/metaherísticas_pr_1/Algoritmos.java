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
import tools.GuardarLog;

/**
 *
 * @author Miguerubsk
 */
public class Algoritmos implements Callable<Vector<Integer>> {

    private String algoritmo; //Algoritmo que se va a ejecutar
    private CargaDatos archivo; //Archivo de datos
    private CountDownLatch cdl; //MEcanismo de control para la ejecucion de los hilos
    private Configurador config; //Archivo de configuracion
    private Long semilla; //Semilla para inicializacion
    private Random aleatorio; //Generador de aleatorios
    private GuardarLog log; //Clase para generar los logs

    private Vector<Integer> solucion; //Vector que contiene la solucion final que se devolvera
    private Integer tamañoSolucion; //Tamaño de la solucion
    private Integer tamañoMatriz; //Tamaño de la matriz
    private double[][] matriz; //matriz de distancias

    /**
     * @brief Constructor de la clase algoritmos que inicializa las variables
     * necesarias para el correcto funcionamiento
     * @param archivo contiene los datos que se han cargado para la ejecucion de
     * este algoritmo
     * @param cdl mecanismo de control para que los algoritmos esperen a la
     * ejecucion con todas sus semillas de un mismo archivo
     * @param semilla semilla para inicializacion
     * @param algoritmo algoritmo que se va a usar
     * @param config contiene informacion de configuracion para la ejecucion de
     * los algoritmos
     */
    public Algoritmos(CargaDatos archivo, CountDownLatch cdl, Long semilla, String algoritmo, Configurador config) {
        this.archivo = archivo;
        this.cdl = cdl;
        this.semilla = semilla;
        this.algoritmo = algoritmo;
        this.config = config;
        this.aleatorio = new Random(semilla);

        String ruta = algoritmo + "_" + archivo.getNombreFichero() + "_" + semilla;
        String info = "[EJECUCION INICIADA]\n"
                + "Archivo: " + archivo.getNombreFichero()
                + "\nSemilla: " + semilla
                + "\nAlgoritmo: " + algoritmo
                + "\nTamaño matriz/TamañoSolucion: " + archivo.getTamMatriz() + "|" + archivo.getTamSolucion();

        this.log = new GuardarLog(ruta, info, algoritmo);

        this.solucion = new Vector<Integer>();
        this.tamañoSolucion = archivo.getTamSolucion();
        this.tamañoMatriz = archivo.getTamMatriz();
        this.matriz = archivo.getMatriz();
    }

    /**
     * @brief Funcion que ejecuta el hilo y el algoritmo seleccionado, y muestra
     * informacion de la ejecucion al terminar
     * @return vector con la solucion que ha dado el algoritmo
     */
    @Override
    public Vector<Integer> call() {
        double coste = 0.0;
        long start = 0, stop = 0;
        try {
            start = System.currentTimeMillis();
            switch (algoritmo) {
                case ("Greedy"):
                    coste = Greedy();

                    break;

                case ("Búsqueda_Local"):
                    coste = BusquedaLocal();

                    break;

                case ("Búsqueda_Tabú"):
                    coste = BusquedaTabu();

                    break;
            }

        } catch (Exception e) {
            System.err.println("metaherísticas_pr_1.Algoritmos.call(): excepcion capturada: " + e.toString());
        } finally {
            stop = System.currentTimeMillis();

            Collections.sort(solucion);

            String info = "[EJECUCION TERMINADA]\n"
                    + "Archivo: " + archivo.getNombreFichero()
                    + "\nSemilla: " + semilla
                    + "\nAlgoritmo: " + algoritmo
                    + "\nSolucion: " + solucion.toString()
                    + "\nCoste Solución: " + coste
                    + "\nTiempo: " + ((stop - start)) + " ms"
                    + "\nTamaño matriz/TamañoSolucion: " + tamañoMatriz + "|" + tamañoSolucion + "\n\n";

            log.escribirFinal(info);
            System.out.println(info);

            cdl.countDown();
        }

        return solucion;
    }

    //Algoritmos
    /**
     * @brief Función que ejecuta el algoritmo Greedy
     * @return coste de la solucion
     */
    private double Greedy() {
        double mayordist = 0.0;

        //Creamos y rellenamos el vector de marcados
        Boolean[] marcados = new Boolean[tamañoMatriz];
        Arrays.fill(marcados, Boolean.FALSE);

        //Generamos el primer elemento de forma aleatoria
        Integer punto = aleatorio.nextInt(tamañoMatriz);
        marcados[punto] = true;

        //Agregamos ese elemento a la solucion
        solucion.add(punto);

        log.escribir("Iteracion: 0\n" + solucion.toString() + "\nCoste: 0");

        /*Para cada elemento de la solucion,buscamos el que mas aporta a 
        nuestra solucion actual en cada iteracion y lo añadimos a la solucion*/
        for (int i = 1; i < tamañoSolucion; i++) {
            double d = 0.0;

            for (int j = 0; j < tamañoMatriz; j++) {
                if (!marcados[j]) {
                    d = costePuntoEnSolucion(j); //Calculamos el coste del punto respecto de la solucion

                    //Si es mayor, nos quedamos con el
                    if (d > mayordist) {
                        mayordist = d;
                        punto = j;
                    }

                    d = 0.0;
                }
            }

            //Marcamos el punto como añadido y lo añadimos a la solucion
            marcados[punto] = true;
            solucion.add(punto);

            log.escribir("Iteracion: " + i + "\n" + "Solucion actual: " + solucion.toString() + "\nCoste: " + coste(matriz, solucion.size()));

            mayordist = 0.0;
        }

        return coste(archivo.getMatriz(), archivo.getTamSolucion());
    }

    /**
     * @brief Función que ejecuta el algoritmo Búsqueda Local del primer mejor
     * @return coste de la solucion
     */
    private double BusquedaLocal() {
        Integer numIteraciones = config.getEvaluaciones();

        //Generamos una solucion inicial aleatoria y calculamos su coste
        generarSolucionAleatoria(tamañoSolucion, tamañoMatriz);
        double costeActual = coste(matriz, tamañoSolucion);
        double nuevoCoste;

        log.escribir("Solucion inicial aleatoria: " + solucion.toString());

        //Creamos e inicializamos los vectores de aportes y seleccionados
        Vector<Double> aportes = new Vector<>();
        Vector<Boolean> seleccionados = new Vector<>();

        for (int i = 0; i < tamañoSolucion; i++) {
            aportes.add(0.0);
            seleccionados.add(false);
        }

        //Creamos las variables necesarias para cada iteracion
        Integer iteracion = 0;
        Integer posicion;
        Integer elementoAnterior;
        Integer elementoNuevo = 0;
        boolean mejora = true;

        /*Realizamos la busqueda hasta que se realizen las iteraciones necesarias
        o no se mejore la solucion*/
        while (iteracion < numIteraciones && mejora) {

            mejora = false;

            //Actualizamos el vector de aportes
            actualizarVectorAportes(matriz, aportes, tamañoSolucion);

            //Recorremos la solucion
            for (int k = 0; k < tamañoSolucion && iteracion < numIteraciones; k++) {

                posicion = obtenerPosicionAporteMenor(aportes, seleccionados); //Obtenemos la posicion de menor aporte
                elementoAnterior = solucion.get(posicion); //Guardamos el elemento que vamos a sustituir

                /*Buscamos un elemento nuevo que sustituya el elemento que vamos a sustituir.
                No debe estar ya en la solucion.*/
                for (int i = 0; i < tamañoMatriz && !mejora && iteracion < numIteraciones; i++) {
                    if (!solucion.contains(i)) {
                        nuevoCoste = factorizacion(matriz, tamañoSolucion, costeActual, solucion.get(posicion), i); //Factorizamos para obtener el nuevo coste

                        /*Si el coste nuevo es mejor que nuestro anterior coste, 
                        nos quedamos con la nueva solucion*/
                        if (nuevoCoste > costeActual) {
                            intercambia(posicion, i);
                            elementoNuevo = i;
                            costeActual = nuevoCoste;

                            mejora = true;
                        }

                        log.escribir("Iteracion: " + iteracion + "\n"
                                + "Elemento sustituido: " + elementoAnterior + "\n"
                                + "Nuevo elemento: " + i + "\n"
                                + "Solucion actual: " + solucion.toString()
                                + "\nCoste: " + coste(matriz, tamañoSolucion));
                        iteracion++;
                    }
                }

                /*Si ha mejorado, no seguimos buscando un nuevo elemento. 
                Nos quedamos con el primer mejor y pasamos a la siguiente iteracion.*/
                if (mejora) {

                    break;
                }
            }

            //Borramos los seleccionados y aportes, y los reinicializamos para la siguiente iteracion
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
     * @return coste de la solucion
     */
    private double BusquedaTabu() {
        //Creamos las variables necesarias para la ejecucion del algoritmo
        Integer evaluacion = 0;
        Integer contadorReinicializacion = 0;
        Integer posicion;
        double costeMejorSolucion;
        double costeSolucionActual;
        double costeSolucionParcial;

        //Creamos las estructuras necesarias para las soluciones y memorias
        Vector<Integer> solucionParcial = new Vector<>(tamañoSolucion);
        Vector<Integer> mejorSolucion = new Vector<>(tamañoSolucion);
        Vector<Integer> solucionActual = new Vector<>(tamañoSolucion);
        ConcurrentLinkedQueue<Integer> listaTabu = new ConcurrentLinkedQueue<>();
        Vector<Integer> memoriaLargoPlazo = new Vector<>();

        //Generamos una solucion inicial aleatoria
        generarSolucionAleatoria(tamañoSolucion, tamañoMatriz);

        //Usamos como mejor solucion y solucion actual la solucion generada
        mejorSolucion = solucion;
        solucionActual = mejorSolucion;

        //Inicializamos el vector de marcados para la seleccion de vecinos
        Vector<Boolean> marcados = new Vector<>();
        for (int i = 0; i < tamañoMatriz; i++) {
            marcados.add(Boolean.FALSE);
        }

        //Calculamos el coste de la solucion que hemos generado
        costeMejorSolucion = coste(matriz, tamañoSolucion, mejorSolucion);
        costeSolucionActual = costeMejorSolucion;

        //Iniciamos la lista tabu y la memoria a largo plazo
        iniciarMemorias(listaTabu, memoriaLargoPlazo);

        try {
            //Ejecutamos el algoritmo hasta que realicemos el numero de evaluaciones establecido
            while (evaluacion < config.getEvaluaciones()) {
                Integer numVecinos = 10;

                //Obtenemos la posicion de menor aporte
                posicion = menorAporte(tamañoSolucion, matriz, solucionActual);

                //Limpiamos el vector de marcados para realizar la seleccion de vecinos
                limpiarMarcados(marcados);

                //GUardamos el elemento que vamos a sustituir
                int elementoAnterior = solucionActual.get(posicion);
                costeSolucionParcial = 0;

                evaluacion++;

                log.escribir("Iteracion: " + evaluacion + "\n"
                        + "Elemento que se va a sustituir: " + elementoAnterior + "\n"
                        + "Lista tabu: " + listaTabu.toString() + "\n"
                        + "Coste actual: " + costeSolucionActual);

                //Generamos y evaluamos los vecinos, obteniendo una nueva solucion con el mejor vecino
                solucionActual = evaluarVecinos(numVecinos, marcados, solucionActual, costeSolucionActual, listaTabu, solucionParcial, costeSolucionParcial, elementoAnterior, posicion);;

                //Calculamos el coste de esta nueva solucion
                costeSolucionActual = coste(matriz, tamañoSolucion, solucionActual);

                //Actualizamos la lista tabu y la memoria a largo plazo
                actualizarMemoriaLargoPlazo(memoriaLargoPlazo, solucionActual);
                actualizarListaTabu(listaTabu, elementoAnterior);

                /*Si la solucion que hemos obtenido tiene un coste mejor 
                que nuestra mejor solucion, la solucion actual la tomamos como mejor solucion.
                Reiniciamos el contador para la reinicializacion.
                Si no mejora el coste, sumamos 1 al contador de reinicializacion.*/
                if (costeSolucionActual > costeMejorSolucion) {
                    costeMejorSolucion = costeSolucionActual;
                    mejorSolucion = solucionActual;

                    contadorReinicializacion = 0;
                } else {

                    contadorReinicializacion++;
                }

                /*Si el contandor de reinicializacion llega a 100, llevamos a cabo
                el reinicio de la busqueda, realizacion una intensificacion o diversificacion
                y obtendremos una nueva solucion desde la que continuar la busqueda.*/
                if (contadorReinicializacion == 100) {

                    solucionActual = reiniciar(memoriaLargoPlazo);

                    costeSolucionActual = coste(matriz, tamañoSolucion, solucionActual);

                    if (costeSolucionActual > costeMejorSolucion) {
                        costeMejorSolucion = costeSolucionActual;
                        mejorSolucion = solucionActual;
                    }

                    //Reiniciamos las memorias
                    reiniciarMemorias(memoriaLargoPlazo, listaTabu);

                    evaluacion++;
                    contadorReinicializacion = 0;
                }
            }

            //Nos guardamos la mejor solucion obtenido en nuestra solucion
            solucion = mejorSolucion;
        } catch (Exception e) {
            System.err.println("metaherísticas_pr_1.Algoritmos.BusquedaTabu(): excepcion capturada: " + e.toString() + ". Iteracion: " + evaluacion + ". Contador reinicializacion: " + contadorReinicializacion);
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
     * @brief Función que calcula el coste de una solucion que se le pase como
     * parametro
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
     * @brief Función que crealiza la factorizacion de la solucion al cambiar un
     * elemento por otro que no este en la solucion
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
     * @brief Función que crealiza la factorizacion de una solucion al cambiar
     * un elemento por otro que no este en la solucion
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
     * @brief Operador de intercambio que cambia un elemento por otro en la
     * solucion
     * @param i indice del elemento que se quiere modificar
     * @param j elemento que se quiere añadir en la posicion i
     */
    private void intercambia(int i, int j) {
        solucion.setElementAt(j, i);
    }

    /**
     * @brief Operador de intercambio que cambia un elemento por otro en una
     * solucion
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
     * @brief Funcion que realiza la intensificacion cuando queremos reiniciar
     * la busqueda tabu. Busca los m elementos mas frecuentes
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
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
     * @brief Funcion que realiza la diversificacion cuando queremos reiniciar
     * la busqueda tabu. Busca los m elementos mas frecuentes
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
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

    /**
     * @brief Función que realiza el reinicio de la busqueda tabu
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
     * @return vector solucion de tamaño m elementos con la nueva solucion
     */
    private Vector<Integer> reiniciar(Vector<Integer> memoriaLargoPlazo) {
        double tirada = aleatorio.nextInt(2);

        if (tirada <= 0.5) {
            log.escribir("======================================================="
                    + "\nReiniciando con intensificacion: \n"
                    + "=======================================================");
            
            return calculaVectorMasFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        } else {
            log.escribir("======================================================="
                    + "\nReiniciando con diversificacion: \n"
                    + "=======================================================");
            
            return calculaVectorMenosFrecuentes(memoriaLargoPlazo, archivo.getTamMatriz(), archivo.getTamSolucion());
        }
    }

    /**
     * @brief Funcion que actualiza la memoria a largo plazo con los elementos
     * de una solucion
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
     * @param vector una solucion
     */
    private void actualizarMemoriaLargoPlazo(Vector<Integer> memoriaLargoPlazo, Vector<Integer> vector) {
        for (int i = 0; i < archivo.getTamSolucion(); i++) {
            memoriaLargoPlazo.set(vector.get(i), memoriaLargoPlazo.get(vector.get(i)) + 1);
        }
    }

    /**
     * @brief Funcion que actualiza la lista tabu, añadiendo el elemento que se
     * ha eliminado de la solucion y eliminando el elemento mas antiguo
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
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
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
     * @brief Funcion que genera un numVecinos y los evalua respecto de una
     * solucion. Busca la mejor solucion entre los vecinos generados.
     * @param numVecinos numero de vecinos a generar
     * @param marcados vector de elementos marcados para no repetir vecinos
     * @param costeF coste de la solucion factorizada
     * @param solucionActual vector con la solucion actual que se esta
     * trabajando
     * @param costeSolucionActual coste de la solucion actual
     * @param listaTabu lista de elementos tabu
     * @param solucionParcial vector con la solucion parcial, incluye el vecino
     * que se va a evaluar
     * @param costeSolucionParcial coste de la solucion parcial
     * @param elementoAnterior elemento que se va a sustituir en la solucion
     * actual
     * @param posicion posicion del elemento anterior
     * @return vector solucion de tamaño m elementos con la mejor solucion de
     * entre los vecinos
     */
    private Vector<Integer> evaluarVecinos(Integer numVecinos, Vector<Boolean> marcados, Vector<Integer> solucionActual,
            double costeSolucionActual, ConcurrentLinkedQueue<Integer> listaTabu,
            Vector<Integer> solucionParcial, double costeSolucionParcial, Integer elementoAnterior, Integer posicion) {
        double costeF = 0.0;
        int mejorVecino = 0;

        while (numVecinos > 0) {
            int vecino;
            do {
                vecino = aleatorio.nextInt(archivo.getTamMatriz());
            } while (marcados.get(vecino));
            marcados.set(vecino, Boolean.TRUE);
            if (!solucionActual.contains(vecino)) {
                if (!listaTabu.contains(vecino)) {

                    costeF = factorizacion(archivo.getMatriz(), archivo.getTamSolucion(), costeSolucionActual, elementoAnterior, vecino, solucionActual);
                    log.escribirNoInfo("    Vecino generado: " + vecino
                            + "\n    Coste con el nuevo vecino: " + costeF);
                    numVecinos--;
                    if (costeSolucionParcial < costeF) {
                        mejorVecino = vecino;
                        costeSolucionParcial = costeF;
                        solucionParcial = solucionActual;

                        intercambia(solucionParcial, posicion, vecino);
                    }
                }
            }
        }

        log.escribirNoInfo("Vecino que nos quedamos: " + mejorVecino
                + "\nNuevo coste: " + costeSolucionParcial
                + "\nSolucion nueva: " + solucionParcial.toString());

        return solucionParcial;
    }

    /**
     * @brief Funcion que reiniciar las memorias de la busqueda tabu
     * @param memoriaLargoPlazo memoria que almacena el numero de veces que se
     * repite un elemento a lo largo de las iteraciones
     * @param listaTabu lista de elementos tabu
     */
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
