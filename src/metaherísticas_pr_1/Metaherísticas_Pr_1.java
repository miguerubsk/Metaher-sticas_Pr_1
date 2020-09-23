/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

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
    }

}
