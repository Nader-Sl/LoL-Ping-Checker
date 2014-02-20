
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Nader Sl
 *
 * A very close to accurate multi-threaded LoL pinger supporting different
 * servers. It works on pinging IPs gathered from a route trace request to the
 * real game servers (since they don't accept ping requests) and gathered the
 * closest pingable server in the hierarchy (within same physical location)
 */
public class PingChecker extends JFrame {

    private static final TreeMap<String, Ping> routTracedIPs = new TreeMap<String, Ping>();//TreeMap with natural sorting.
    private static final long updateTime = 1000;// in ms
    private static final int pingsPerRequest = 1;
    final static private Font title = new Font(Font.MONOSPACED, Font.BOLD, 36);
    final static private Font main = new Font(Font.MONOSPACED, Font.BOLD, 28);
    final static private Font note = new Font(Font.MONOSPACED, Font.BOLD, 13);
    private gameCanvas gs;
    private BufferedImage backGround;
    private ArrayList<Process> processes = new ArrayList<Process>();
    private final ExecutorService executor;
    private final static Logger LOGGER = Logger.getLogger(PingChecker.class.getName());

    static {
        routTracedIPs.put("NA", new Ping("216.52.255.103"));
        // routtracing EU servers showed that both EUW and EUNE servers are derived from same shared local server thus they have the same ping.
        routTracedIPs.put("EU West", new Ping("95.172.67.1"));
        routTracedIPs.put("EU Nordic East", new Ping("95.172.67.1"));
        routTracedIPs.put("OCE", new Ping("154.54.89.57"));

    }

    public static void main(String[] argV) {

        final PingChecker ptm = new PingChecker();
        ptm.setSize(1200, 650);
        ptm.setTitle("Lol Ping Checker v 1.0 - by NaderSl");
        ptm.setVisible(true);
        ptm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public PingChecker() {
        executor = Executors.newFixedThreadPool(routTracedIPs.size());
        try {
            backGround = ImageIO.read(getClass().getResourceAsStream("data/bg.jpg"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Background image is missing.");
        }
        gs = new gameCanvas();
        gs.setSize(getWidth(), getHeight());
        add(gs);
        for (final Entry<String, Ping> entry : routTracedIPs.entrySet()) {

            executor.execute(new Runnable() {
                final private Timer updateT = new Timer(updateTime);

                @Override
                public void run() {
                    while (true) {
                        if (updateT.isUpThenReset()) {
                            entry.getValue().setPing(getServerPing(entry.getValue().getIp()));

                        }
                        gs.repaint();
                    }
                }
            });

        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
         @Override
            public void run() {
               
                executor.isShutdown();
            }
        }));
    }

    private int getServerPing(final String ip) {

        try {

            Runtime runtime = Runtime.getRuntime();

            Process proc = runtime.exec("ping -" + (System.getProperty("os.name").startsWith("Windows") ? "n" : "c") + " " + pingsPerRequest + " " + ip);
            processes.add(proc);
            proc.waitFor();

            int exit = proc.exitValue();
            if (exit == 0) { // normal exit
                InputStream in = proc.getInputStream();
                InputStreamReader is = new InputStreamReader(in);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(is);
                String read = br.readLine();

                while (read != null) {
                    //System.out.println(read);
                    sb.append(read);
                    read = br.readLine();

                }
                String[] data = sb.toString().split("time=");
                return Integer.parseInt(data[1].split("ms")[0]);
            } else { // abnormal exit, so decide that Ithe server is not reachable
                LOGGER.log(Level.SEVERE, "Server ".concat(ip).concat(" can't be reached."));
                return -1;
            }
        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, e.getMessage());
            return -1;
        }
    }

    class gameCanvas extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backGround, 0, 0, null);
            g.setColor(Color.RED);
            g.setFont(title);


            int x = 50, y = 50;
            g.drawString("Your Current Ping to LoL Servers:", x, y);
            //Set the color to blue
            g.setColor(Color.white);
            g.setFont(main);
            for (final Entry<String, Ping> entry : routTracedIPs.entrySet()) {
                g.drawString(entry.getKey() + " : " + entry.getValue().getPing() + "ms", x, y += 100);
            }
            g.setFont(note);
            g.setColor(Color.yellow);
            g.drawString("*note: ping of value -1 means that there wasn't a connection to corresponding server", x, y += 150);

        }
    }
}
