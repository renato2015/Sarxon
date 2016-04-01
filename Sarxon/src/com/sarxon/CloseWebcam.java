/*
 * CloseWebcam.java
 *
 * Created on 01/04/2016, 16:01:07
 */

package com.sarxon;

import com.github.sarxos.webcam.Webcam;

/**
 *
 * @author Tekna <informatica@teknamed.com.br>
 */
public class CloseWebcam
{
    public static void main(String[]args){
        Webcam webcam = Webcam.getDefault();
        if(webcam != null){
            webcam.close();
        }
    }
}
