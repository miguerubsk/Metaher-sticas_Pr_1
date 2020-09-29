/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import tools.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        Configurador config = new Configurador(args[0]);
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
                    CountDownLatch cdl = new CountDownLatch(config.getSemillas().size());
                    switch (config.getAlgoritmos().get(i)) {
                        case ("Greedy"):
                            for (int k = 0; k < config.getSemillas().size(); k++) {
                                Algoritmos meta = new Algoritmos(Datos.get(j), cdl, config.getSemillas().get(k));
                                m.add(meta);
                                ejecutor.execute(meta);
                            }
                            cdl.await();
                            for (int k = 0; k < m.size(); k++) {
                                GuardarArchivo("log/" + config.getAlgoritmos().get(i) + "_" + Datos.get(j) + config.getSemillas().get(k) + ".txt", m.get(k).getLog());
                            }
                            break;
                        case ("Búqueda_Local"):
                            for (int k = 0; k < config.getSemillas().size(); k++) {
                                Algoritmos meta = new Algoritmos(Datos.get(j), cdl, config.getSemillas().get(k));
                                m.add(meta);
                                ejecutor.execute(meta);
                            }
                            cdl.await();
                            for (int k = 0; k < m.size(); k++) {
                                GuardarArchivo("log/" + config.getAlgoritmos().get(i) + "_" + Datos.get(j) + config.getSemillas().get(k) + ".txt", m.get(k).getLog());
                            }
                            break;
                        case ("Búsqueda_Tabú"):
                            for (int k = 0; k < config.getSemillas().size(); k++) {
                                Algoritmos meta = new Algoritmos(Datos.get(j), cdl, config.getSemillas().get(k));
                                m.add(meta);
                                ejecutor.execute(meta);
                            }
                            cdl.await();
                            for (int k = 0; k < m.size(); k++) {
                                GuardarArchivo("log/" + config.getAlgoritmos().get(i) + "_" + Datos.get(j) + config.getSemillas().get(k) + ".txt", m.get(k).getLog());
                            }
                            break;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Metaherísticas_Pr_1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        ejecutor.shutdown();
        System.out.println("TERMINADO");
    }

}
