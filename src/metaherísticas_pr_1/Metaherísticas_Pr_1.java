/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Miguerubsk
 */
public class Metaherísticas_Pr_1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Configurador config = new Configurador(args[0]);
        CargaDatos Datos = new CargaDatos(config.getFicheros().get(0));
        System.out.println(config.getSemillas());
        System.out.println(config.getFicheros());
        System.out.println(config.getAlgoritmos());
        System.out.println(Datos.getTamSolucion());
        System.out.println(Datos.getTamMatriz());

        ExecutorService ejecutor = Executors.newCachedThreadPool();

        for (int i = 0; i < config.getAlgoritmos().size(); i++) {
            for (int j = 0; j < config.getFicheros().size(); j++) {
                CountDownLatch cdl = new CountDownLatch(config.getSemillas().size());
                switch (config.getAlgoritmos().get(i)) {
                    case ("Greedy"):
                        ArrayList<Algoritmos> m = new ArrayList();
                        for (int k = 0; k < config.getSemillas().size(); k++) {
                            Algoritmos meta = new Algoritmos(Datos, cdl, config.getSemillas().get(k));
                            m.add(meta);
                            ejecutor.execute(meta);
                        }
                }
            }

        }
    }

}
