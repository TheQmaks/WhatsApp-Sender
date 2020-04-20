package qmaks.utils;

import java.util.List;
import java.util.Base64;
import java.io.InputStream;
import org.w3c.dom.Document;
import java.io.StringReader;
import javax.xml.xpath.XPath;
import java.io.BufferedReader;
import org.xml.sax.InputSource;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import se.vidstige.jadb.JadbDevice;
import javax.xml.xpath.XPathFactory;
import se.vidstige.jadb.JadbConnection;
import javax.xml.xpath.XPathExpression;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Android {
    public static JadbDevice noxEmulator;

    static {
        try {
            JadbConnection jadb = new JadbConnection();
            List<JadbDevice> devices = jadb.getDevices();
            for (JadbDevice device : devices) {
                noxEmulator = device;
                System.out.println("[*] Эмулятор найден.");
            }
        } catch(Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void tap(Point point) throws Exception {
        exec("input tap " + point.getX() + " " + point.getY());
    }

    public static void exec(String command) throws Exception {
        noxEmulator.executeShell(System.err, command, new String[0]);
    }

    public static void swipe(Point pos1, Point pos2, int delay) throws Exception {
        exec("input swipe " + pos1.getX() + " " + pos1.getY() + " " + pos2.getX() + " " + pos2.getY() + " " + delay);
    }

    public static void pressKey(int keyCode) throws Exception {
        exec("input keyevent " + keyCode);
    }

    public static void addNumber(String number) throws Exception {
        exec("am start -a android.intent.action.INSERT -t vnd.android.cursor.dir/contact -e phone '" + number + "' --activity-clear-top");
    }

    public static void clearData(String app) throws Exception {
        exec("pm clear " + app);
    }

    public static void waitPrint(String containsString) throws Exception {
        InputStream uiautomator = noxEmulator.executeShell("uiautomator events", new String[0]);
        BufferedReader uiautomatorListener = new BufferedReader(new InputStreamReader(uiautomator));

        for (String line = uiautomatorListener.readLine(); line != null; line = uiautomatorListener.readLine()) {
            if(line.contains(containsString)) {
                break;
            }
        }
    }

    public static void doubleTap(Point pos) throws Exception {
        swipe(pos, pos, 1000);
    }

    public static void writeText(String text) throws Exception {
        exec("input text " + text);
    }

    public static void removeFile(String filePath) throws Exception {
        exec("rm -f " + filePath);
    }

    public static void waitLoading(String xpath, String value) throws Exception {
        while(true) {
            if(getValueByXPath(xpath).equals(value)) {
                break;
            }
        }
    }

    public static void toClipboard(String text) throws Exception {
        exec("am broadcast -a set -e base64 '" + Base64.getEncoder().encodeToString(text.getBytes("UTF-8")) + "'");
    }

    public static void refreshMedia() throws Exception {
        exec("am broadcast -a android.intent.action.MEDIA_MOUNTED -d file://");
    }

    public static void startService(String service) throws Exception {
        exec("am startservice " + service);
    }

    public static void startActivity(String activity) throws Exception {
        exec("am start -n " + activity + " --activity-clear-top");
    }

    public static Point getPointByBounds(String bounds) {
        String[] split = bounds.split(Pattern.quote("]["));
        String[] leftUpper = split[0].replace("[", "").split(",");
        String[] rightLower = split[1].replace("]", "").split(",");
        Point p1 = new Point(Integer.valueOf(leftUpper[0]), Integer.valueOf(leftUpper[1]));
        Point p2 = new Point(Integer.valueOf(rightLower[0]), Integer.valueOf(rightLower[1]));
        int diffX = p2.getX() - p1.getX();
        int diffY = p2.getY() - p1.getY();
        return new Point(p1.getX() + (diffX / 2), p1.getY() + (diffY / 2));
    }

    public static String getValueByXPath(String xpathExpr) throws Exception {
        String value = "";

        //Вызываем дамп текущего интерфейса в xml для последующего парсинга
        InputStream uiautomatorDump = noxEmulator.executeShell("uiautomator dump /dev/tty", new String[0]);
        BufferedReader uiautomatorListener = new BufferedReader(new InputStreamReader(uiautomatorDump));

        for (String line = uiautomatorListener.readLine(); line != null; line = uiautomatorListener.readLine()) {
            if(line.endsWith("UI hierchary dumped to: /dev/tty")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(line.replace("UI hierchary dumped to: /dev/tty", ""))));
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile(xpathExpr);
                value = expr.evaluate(doc);
                break;
            }
        }
        uiautomatorDump.close();
        uiautomatorListener.close();

        return value;
    }
}