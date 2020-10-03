package tools;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Miguerubsk
 */
public class Timer {
    private long startime, stoptime;
    
    public void Start(){
        startime = System.currentTimeMillis();
    }
    
    public void Stop(){
        stoptime = System.currentTimeMillis();
    }
    
    private long Time(){
        return stoptime - startime;
    }
    
    public long TiempoMilisegundos(){
        return Time();
    }
    
    public long TiempoSegundos(){
        return Time()/1000;
    }
    
    public long TiempoMinutos(){
        return Time()/60000;
    }
    
    public long TiempoHoras(){
        return Time()/3600000;
    }
}
