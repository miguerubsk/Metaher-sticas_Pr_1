package metaherísticas_pr_1;

import tools.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Miguerubsk
 */
public class Metaherísticas_Pr_1 {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.​util.​concurrent.ExecutionException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        //Cargamos el archivo de configuracion
        Configurador config = new Configurador("config.txt");

        //Cargamos los ficheros de datos
        ArrayList<CargaDatos> Datos = new ArrayList<>();
        for (int i = 0; i < config.getFicheros().size(); i++) {
            Datos.add(new CargaDatos(config.getFicheros().get(i)));
        }

        //Creamos el servicio de ejecucion
        ExecutorService ejecutor = Executors.newCachedThreadPool();

        //Recorremos la lista de algoritmos
        for (int i = 0; i < config.getAlgoritmos().size(); i++) {
            for (int j = 0; j < Datos.size(); j++) {
                try {
                    //Almacena todos los algoritmos que estan en ejecucion
                    ArrayList<Algoritmos> listaAlgoritmos = new ArrayList();
                    //Creamos una lista para almacenar todos los resultados de las ejecuciones de un algoritmo
                    ArrayList<Future<Vector<Integer>>> futures = new ArrayList<>();
                    //Creamos la barrera para esperar a la ejecucion del algoritmo con todas las semillas, para un mismo archivo
                    CountDownLatch cdl = new CountDownLatch(config.getSemillas().size());
                    for (int k = 0; k < config.getSemillas().size(); k++) {
                        //Configuramos el algoritmo para su posterior ejecucion
                        Algoritmos algoritmo = new Algoritmos(Datos.get(j), cdl, config.getSemillas().get(k), config.getAlgoritmos().get(i), config);
                        listaAlgoritmos.add(algoritmo);
                        //Ejecutamos el algoritmo
                        Future<Vector<Integer>> ejecucion = ejecutor.submit(algoritmo);
                        futures.add(ejecucion);
                    }
                    
                    //Esperamos a la ejecucion del algoritmo con todas las semillas
                    cdl.await();
                } catch (Exception ex) {
                    System.err.println("metaherísticas_pr_1.Metaherísticas_Pr_1.main(): excepcion capturada: " + ex.toString());
                }
            }

        }
        ejecutor.shutdown();

        System.out.println("TERMINADO");
    }

}
