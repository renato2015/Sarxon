/*
 * DetectaWebcam.java
 *
 * Created on 01/04/2016, 15:04:01
 */
package com.sarxon;

import com.github.sarxos.webcam.Webcam;

/**
 *
 * @author Renato Borges Cardoso
 */
public class DetectaWebcam
{
    public static void main (String[] args)
    {

        Webcam webcam = Webcam.getDefault();
        if (webcam != null)
        {
            System.out.println("Webcam: " + webcam.getName());
        }
        else
        {
            System.out.println("Não foi detectada nenhuma webcam.");
        }
    }

}
