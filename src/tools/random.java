/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

/**
 *
 * @author Miguerubsk
 */



public class random {
    long Seed = 0L;
    static final int MASK = 2147483647;
    static final int PRIME = 65539;
    static final double SCALE = 0.4656612875e-9;
    
    
    /*@brief Inicializa la semilla al valor x. Solo debe llamarse a esta funcion una vez en todo el programa */
    public void Set_random(long x){
        Seed = (long) x;
    }
    
    /*@brief Devuelve el valor actual de la semilla */
    public long Get_random(){
        return Seed;
    }
    
    /*@brief Genera un numero aleatorio real en el intervalo [0,1](incluyendo el 0 pero sin incluir el 1) */
    public float Rand(){
        return (float) (( Seed = ( (Seed * PRIME) & MASK) ) * SCALE );
    }
    
    /*@brief Genera un numero aleatorio entero en {low,...,high}
    * @param low
    * @param high
    */
    public int Randint(int low, int high){
        return (int) (low + (high-(low)+1) * Rand());
    }
    
    /*@brief Genera un numero aleatorio real en el intervalo [low,...,high](incluyendo 'low' pero sin incluir 'high')
    * @param low 
    * @param high
    */
    public float Randfloat(float low, float high){
        return (low + (high-(low))*Rand());
    }
    
}
