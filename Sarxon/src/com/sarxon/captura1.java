package com.sarxon;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import de.humatic.dsj.CaptureDeviceControls;
import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 *
 * @author Renato Borges Cardoso
 */
public class captura1 extends JFrame implements Runnable, WebcamListener, WindowListener, UncaughtExceptionHandler, ItemListener, WebcamDiscoveryListener, PropertyChangeListener
{
    private int paramDispositivo = 0;

    static
    {
        File dll = new File("C:\\Home\\Libs\\dsj\\64bit\\dsj.dll");
        Webcam.setHandleTermSignal(true);
        if (dll.exists())
        {
            DSEnvironment.setDLLPath(dll.getAbsolutePath());
        }
    }

    private static final long serialVersionUID = 1L;

    //DSJ
    private DSCapture graph;

    //Sarxon
    private Webcam webcam = null;
    private WebcamPanel panel = null;
    private WebcamPicker picker = null;

    @Override
    public void run ()
    {
        JPanel painelBtn = new JPanel();
        JButton btnCapturar = new JButton();
        JButton btnGravarVideo = new JButton();
        JButton btnControleImagem = new JButton("Ajuste de imagem");
        JButton btnRestart = new JButton("Reiniciar captura");

        btnRestart.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                panel.start();
            }

        });

        btnControleImagem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                abrirControleImagem(paramDispositivo);
            }

        });

        btnCapturar.setText("Capturar");
        btnCapturar.setToolTipText("Click para capturar imagem");
        btnCapturar.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                try
                {
                    File file = new File(String.format("imagem-%d.jpg", System.currentTimeMillis()));
                    ImageIO.write(webcam.getImage(), "JPG", file);
                }
                catch (IOException | HeadlessException e)
                {
                    JOptionPane.showMessageDialog(null, "Erro ao capturar imagem.\n" + e.getMessage());
                }
            }

        });

        btnGravarVideo.setText("Gravar Video");
        btnGravarVideo.setToolTipText("Click para gravar o video");
        btnGravarVideo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                try
                {
                    webcam.close();
                    File file = new File("output.avi" + System.currentTimeMillis());

                    IMediaWriter writer = ToolFactory.makeWriter(file.getName());
                    Dimension size = com.github.sarxos.webcam.WebcamResolution.VGA.getSize();


                    webcam = picker.getSelectedWebcam();
                    webcam.setViewSize(size);
                    webcam.open(true);
//                    writer.addVideoStream(0, 0, size.width, size.height);

                    panel = new WebcamPanel(webcam, false);
                    panel.setFPSDisplayed(true);
                    panel.start();

                    long start = System.currentTimeMillis();

                    for (int i = 0; i < 50; i++)
                    {
                        System.out.println("Capture frame " + i);
                        BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
                        IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);

                        IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
                        frame.setKeyFrame(i == 0);
                        frame.setQuality(0);

                        writer.encodeVideo(0, frame);

                        // 10 FPS
                        Thread.sleep(100);
                    }
                    writer.close();
                }
                catch (WebcamException | InterruptedException e)
                {
                    JOptionPane.showMessageDialog(null, "Erro ao gravar video: " + e.getMessage());
                }
            }

        });

        painelBtn.add(btnCapturar);
        painelBtn.add(btnGravarVideo);
        painelBtn.add(btnRestart);
        painelBtn.add(btnControleImagem);

        Webcam.addDiscoveryListener(this);
        setTitle("Capturando imagem com java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(this);

        picker = new WebcamPicker();
        picker.addItemListener(this);
        webcam = picker.getSelectedWebcam();

        if (webcam == null)
        {
            JOptionPane.showMessageDialog(null, "Nenhum dispositivo de captura encontrado");
            System.exit(1);
        }

        List<Webcam> cameraList = Webcam.getWebcams();

        Webcam webcam = cameraList.get(0);
        webcam.setCustomViewSizes(dimensaoDispositivo());
        webcam.setViewSize(WebcamResolution.HDMI.getSize());
        if(webcam == null){
            webcam.open();
        }


        webcam.addWebcamListener(captura1.this);

        panel = new WebcamPanel(webcam, false);

        add(picker, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(painelBtn, BorderLayout.SOUTH);
        pack();
        setVisible(true);

        Thread t = new Thread()
        {
            @Override
            public void run ()
            {
                panel.start();
            }

        };
        t.setName("example-starter1");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(this);
        t.start();
    }

    public static void main (String[] args)
    {
        SwingUtilities.invokeLater(new captura1());
    }

    /**
     * Metodo que altera o dispositivo de captura no painel
     *
     * @param e Dispositivo escolhido
     */
    @Override
    public void itemStateChanged (ItemEvent e)
    {
        if (e.getItem() != webcam)
        {
            if (webcam != null)
            {
                panel.stop();
                remove(panel);
                webcam.removeWebcamListener(this);
                webcam.close();
                webcam = (Webcam) e.getItem();
                switch (webcam.getName())
                {
                    case "AVerMedia HD 0":
                        webcam.setCustomViewSizes(dimensaoDispositivo());
                        webcam.setViewSize(WebcamResolution.HDMI.getSize());
                        paramDispositivo = 0;
                        break;
                    case "USB Video Camera 1":
                        webcam.setCustomViewSizes(dimensaoDispositivo());
                        webcam.setViewSize(WebcamResolution.VGA.getSize());
                        paramDispositivo = 1;
                        break;
                    case "":
                        webcam.setCustomViewSizes(dimensaoDispositivo());
                        webcam.setViewSize(WebcamResolution.XGA.getSize());
                        paramDispositivo = 2;
                        break;
                }

                webcam.addWebcamListener(this);
                panel = new WebcamPanel(webcam, false);
                add(panel, BorderLayout.CENTER);
                pack();
                Thread t = new Thread()
                {
                    @Override
                    public void run ()
                    {
                        panel.start();
                    }

                };
                t.setName("example-stoper");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(this);
                t.start();
            }
        }
    }

    /**
     * Abrir frame para configurar captura do painel
     *
     * @param paramDispo
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private void abrirControleImagem (int paramDispo)
    {
        JFrame jf = new JFrame("Controle do dispositivo de imagem");
        DSFilterInfo[][] dsi = DSCapture.queryDevices();
        graph = new DSCapture(DSFiltergraph.DD7, dsi[0][paramDispo], false, DSFilterInfo.doNotRender(), this);

        jf.setLayout(new java.awt.GridLayout(0, 1));

        if (graph.getActiveVideoDevice() != null)
        {
            try
            {
                CaptureDeviceControls cdc = graph.getActiveVideoDevice().getControls();
                for (int i = CaptureDeviceControls.BRIGHTNESS; i < CaptureDeviceControls.LT_FINDFACE; i++)
                {
                    try
                    {
                        jf.add(cdc.getController(i, 0, true));
                    }
                    catch (Exception ex)
                    {
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (graph.getActiveAudioDevice() != null)
        {
            try
            {
                CaptureDeviceControls acdc = graph.getActiveAudioDevice().getControls();
                for (int i = CaptureDeviceControls.MASTER_VOL; i < CaptureDeviceControls.TREBLE; i++)
                {
                    try
                    {
                        jf.add(acdc.getController(i, 0, true));
                    }
                    catch (Exception ex)
                    {
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (jf.getContentPane().getComponentCount() > 0)
        {
            jf.pack();
            jf.setVisible(true);
        }
    }

    /**
     * Metodo que seta as resoluções da tela
     *
     * @return Dimension[] com varias resoluções
     */
    private Dimension[] dimensaoDispositivo ()
    {
        Dimension[] nonStandardResolutions = new Dimension[]
        {
            WebcamResolution.PAL.getSize(),
            WebcamResolution.HD720.getSize(),
            new Dimension(2000, 1000),
            new Dimension(1000, 500),
            new Dimension(1920, 1080),
        };
        return nonStandardResolutions;
    }

    @Override
    public void propertyChange (PropertyChangeEvent pce)
    {
        switch (DSJUtils.getEventType(pce))
        {

        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void webcamFound (WebcamDiscoveryEvent event)
    {
        if (picker != null)
        {
            picker.addItem(event.getWebcam());
        }
    }

    @Override
    public void webcamGone (WebcamDiscoveryEvent event)
    {
        if (picker != null)
        {
            picker.removeItem(event.getWebcam());
        }
    }

    @Override
    public void webcamOpen (WebcamEvent we)
    {
        webcam.open();
        panel.start();
    }

    @Override
    public void webcamClosed (WebcamEvent we)
    {
    }

    @Override
    public void webcamDisposed (WebcamEvent we)
    {
    }

    @Override
    public void webcamImageObtained (WebcamEvent we)
    {
    }

    @Override
    public void windowActivated (WindowEvent e)
    {
    }

    @Override
    public void windowClosed (WindowEvent e)
    {
        webcam.close();
    }

    @Override
    public void windowClosing (WindowEvent e)
    {
    }

    @Override
    public void windowOpened (WindowEvent e)
    {
    }

    @Override
    public void windowDeactivated (WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified (WindowEvent e)
    {
        panel.resume();
    }

    @Override
    public void windowIconified (WindowEvent e)
    {
        System.out.println("webcam viewer paused");
        panel.pause();
    }

    @Override
    public void uncaughtException (Thread t, Throwable e)
    {
        System.err.println(String.format("Exception in thread %s", t.getName()));
        e.printStackTrace();
    }

}
