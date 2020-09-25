/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Miguerubsk
 */
public class GuardarArchivos {
    
    private static FileWriter fichero = null;
    private static PrintWriter pw = null;
    
    public static void GuardarArchivo(String ruta, String texto){
        try{
            fichero = new FileWriter(ruta);
            pw = new PrintWriter(fichero);
            
            pw.write(texto);
            
        }catch (IOException e){
            System.out.println(e);
        } finally {
            try{
                if (fichero != null){
                    fichero.close();
                }
            }catch (IOException e2){
                System.out.println(e2);
            }
        }
    }
    
}
