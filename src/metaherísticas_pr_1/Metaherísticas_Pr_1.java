/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import tools.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

        for (int i = 0; i < config.getAlgoritmos().size(); i++) {
            for (int j = 0; j < Datos.size(); j++) {
                try {
                    ArrayList<Algoritmos> listaAlgoritmos = new ArrayList();
                    Vector<Vector<Integer>> soluciones = new Vector<>();
                    for (int k = 0; k < config.getSemillas().size(); k++) {
                        Algoritmos meta = new Algoritmos(Datos.get(j), config.getSemillas().get(k), config.getAlgoritmos().get(i), config);
                        soluciones.add(meta.call());
                        listaAlgoritmos.add(meta);
                    }
                    for (int k = 0; k < listaAlgoritmos.size(); k++) {
                        GuardarArchivo("log/" + config.getAlgoritmos().get(i) + "_" + Datos.get(j) + config.getSemillas().get(k) + ".txt", soluciones.get(k).toString());
                    }

                } catch (UnsupportedOperationException ex) {
                    Logger.getLogger(Metaherísticas_Pr_1.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Metaherísticas_Pr_1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        System.out.println("TERMINADO");
    }

}
