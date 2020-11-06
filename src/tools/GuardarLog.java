/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Miguerubsk
 */
public class GuardarLog {

    private FileWriter fichero = null;
    private PrintWriter pw = null;
    private String separador = "\n-------------------------------------";

    public GuardarLog(String nombreArchivo, String texto, String algoritmo) {
        try {
            String carpeta = "log/" + algoritmo + "/";
            File directorio = new File(carpeta);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            File file = new File(carpeta + nombreArchivo + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            fichero = new FileWriter(carpeta + nombreArchivo + ".txt");
            pw = new PrintWriter(fichero);

            pw.write(texto + separador);

        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public void escribir(String texto) {
        try {
            fichero.write("\n[INFORMACION]\n" + texto + "\n");
        } catch (Exception e) {
            System.out.println("tools.GuardarLog.escribir()" + e.toString());
        }
    }
    
    public void escribirNoInfo(String texto) {
        try {
            fichero.write("\n" + texto + "\n");
        } catch (Exception e) {
            System.out.println("tools.GuardarLog.escribir()" + e.toString());
        }
    }

    public void escribirFinal(String texto) {
        try {
            fichero.write(separador + "\n" + texto + "\n");
            fichero.close();
        } catch (Exception e) {
            System.out.println("tools.GuardarLog.escribir()" + e.toString());
        }
    }
}
