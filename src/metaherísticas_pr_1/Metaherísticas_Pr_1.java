/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import tools.*;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static tools.GuardarArchivos.GuardarArchivo;

/**
 *
 * @author Miguerubsk
 */
public class Metaherísticas_Pr_1 {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // TODO code application logic here
        Configurador config = new Configurador("config.txt");
//        CargaDatos Datos = new CargaDatos(config.getFicheros().get(0));
        ArrayList<CargaDatos> Datos = new ArrayList<>();
        for (int i = 0; i < config.getFicheros().size(); i++) {
            Datos.add(new CargaDatos(config.getFicheros().get(i)));
        }

        ExecutorService ejecutor = Executors.newCachedThreadPool();

        for (int i = 0; i < config.getAlgoritmos().size(); i++) {
            for (int j = 0; j < Datos.size(); j++) {
                try {
                    ArrayList<Algoritmos> m = new ArrayList();
                    ArrayList<Future<Vector<Integer>>> futures = new ArrayList<>();
                    CountDownLatch cdl = new CountDownLatch(1);
                    for (int k = 0; k < 1; k++) {
                        Algoritmos meta = new Algoritmos(Datos.get(j), cdl, config.getSemillas().get(k), config.getAlgoritmos().get(i), config);
                        m.add(meta);
                        Future<Vector<Integer>> ejecucion = ejecutor.submit(meta);
                        futures.add(ejecucion);
                    }
                    cdl.await();
                    for (int k = 0; k < m.size(); k++) {
                        GuardarArchivo("log/" + config.getAlgoritmos().get(i) + "_" + Datos.get(j) + config.getSemillas().get(k) + ".txt", futures.get(k).get().toString());
                    }

                } catch (UnsupportedOperationException ex) {
                    Logger.getLogger(Metaherísticas_Pr_1.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Metaherísticas_Pr_1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        ejecutor.shutdown();
        
        System.out.println("TERMINADO");
    }

}
