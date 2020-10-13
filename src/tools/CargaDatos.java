/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

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
    private String NombreFichero;
    
    public CargaDatos(String fichero){
        NombreFichero = fichero;
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
            
            for (int i = 0; i < TamMatriz; i++) {
                for (int j = 0; j < TamMatriz; j++) {
                    Matriz[i][j] = 0;
                }
            }
            
            while((linea = b.readLine()) != null){
                String[] split = linea.split(" ");
                Matriz[Integer.parseInt(split[0])][Integer.parseInt(split[1])] = Double.parseDouble(split[2]);
                Matriz[Integer.parseInt(split[1])][Integer.parseInt(split[0])] = Double.parseDouble(split[2]);
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

    public String getNombreFichero() {
        return NombreFichero;
    }
    
}
