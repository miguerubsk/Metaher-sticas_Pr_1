/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaherísticas_pr_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Miguerubsk
 */
public class CargaDatos {
    private int TamMatriz, TamSolucion;
    private double Matriz[][];
    
    public CargaDatos(String fichero){
        FileReader f = null;
        String linea;
        try {
            f = new FileReader(fichero);
            BufferedReader b = new BufferedReader(f);
            
            if((linea = b.readLine()) != null){
                String[] split = linea.split(" ");
                TamMatriz = Integer.parseInt(split[0]);
                TamSolucion = Integer.parseInt(split[1]);
                Matriz = new double[TamMatriz][TamMatriz];
            }
            
            while((linea = b.readLine()) != null){
                String[] split = linea.split(" ");
                Matriz[Integer.parseInt(split[0])][Integer.parseInt(split[1])] = Double.parseDouble(split[2]);
            }
            
        } catch (IOException e) {
            System.out.println(e);
        }
        
    }

    public int getTamMatriz() {
        return TamMatriz;
    }

    public int getTamSolucion() {
        return TamSolucion;
    }

    public double[][] getMatriz() {
        return Matriz;
    }
}
