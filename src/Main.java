import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class Main extends JFrame implements ActionListener {
    private JTextField enterUrl, enterClass, currentNew, path, period;
    private Task ttask;
    private static final String enterUrlString = "Введи адрес сайта: ";
    private static final String enterClassString = "Введи нужный класс: ";
    private static final String currentNewString = "Последняя добавленная новость: ";
    private static final String pathString = "Куда сохранить? ";
    private static final String periodString = "Частота обновления (в сек): ";
    private volatile java.util.ArrayList<String> list = new ArrayList<>();
    private volatile boolean cancelTask = false;
    private JButton startButton;
    private JButton stopButton;

    private JComponent createFields() {
        JPanel panel = new JPanel(new GridLayout(5, 2));

        String[] labelStrings = {
                enterUrlString,
                enterClassString,
                currentNewString,
                pathString,
                periodString
        };

        JLabel[] labels = new JLabel[labelStrings.length];
        JComponent[] fields = new JComponent[labelStrings.length];
        int fieldNumber = 0;
        enterUrl = new JTextField("https://", 80);
        fields[fieldNumber++] = enterUrl;

        enterClass = new JTextField("[class*=]", 80);
        fields[fieldNumber++] = enterClass;

        currentNew = new JTextField("", 80);
        currentNew.setEnabled(false);
        fields[fieldNumber++] = currentNew;

        path = new JTextField("", 80);
        fields[fieldNumber++] = path;
        path.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileOpen = new JFileChooser();
                int returnVal = fileOpen.showDialog(null, "Выбери файл / Напиши название нового.txt");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileOpen.getSelectedFile();
                    path.setText(file.getAbsolutePath());
                }
            }
        });
        path.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileOpen = new JFileChooser();
                int returnVal = fileOpen.showDialog(null, "Выбери файл / Напиши название нового.txt");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileOpen.getSelectedFile();
                    path.setText(file.getAbsolutePath());
                }
            }
        });

        period = new JTextField("", 80);
        fields[fieldNumber++] = period;

        for (int i = 0; i < labelStrings.length; i++) {
            labels[i] = new JLabel(labelStrings[i],
                    JLabel.TRAILING);
            labels[i].setLabelFor(fields[i]);
            panel.add(labels[i]);
            panel.add(fields[i]);
        }
        return panel;
    }

    private JComponent createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        startButton = new JButton("Start");
        startButton.setActionCommand("Start");
        startButton.addActionListener(this);
        panel.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        stopButton.setActionCommand("Stop");
        panel.add(stopButton);
        return panel;
    }

    //"C:/Users/Елена/Desktop/result.txt"
    public Main() {
        super("Штучка");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.NORTH, createFields());
        getContentPane().add(BorderLayout.SOUTH, createButtons());
        pack();
        setSize(500, 200);
        setVisible(true);
    }

    private class Task extends SwingWorker<Void, String> {
        @Override
        protected Void doInBackground() throws IOException, InterruptedException, ClassNotFoundException {
            String url = enterUrl.getText();
            String classNeed = enterClass.getText();
            double millis = Double.parseDouble(period.getText());
            while (!cancelTask) {
                list = startParsing(url, classNeed);
                cleanList();
                writeInFile(list);
                publish(list.get(list.size() - 1));
                sleep((long) (millis * 1000));
            }
            return null;
        }

        @Override
        protected void process(java.util.List<String> curString) {
            currentNew.setText(String.format("%s", curString));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if ("Start" == e.getActionCommand()) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            cancelTask = false;
            (ttask = new Task()).execute();
        } else if ("Stop" == e.getActionCommand()) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            ttask.cancel(true);
            cancelTask = true;
            ttask = null;
        }
    }

    private ArrayList<String> cleanList() {
        Set<String> set = new HashSet<String>(list);
        list.clear();
        for (String x : set) {
            if (!list.contains(x))
                list.add(x);
        }
        return list;
    }

    private ArrayList<String> startParsing(String urlFromGui, String classFromGui) throws IOException {
        print("Fetching %s...", urlFromGui);
        Document doc = Jsoup.connect(urlFromGui).get();
        Elements links = doc.select(classFromGui);
        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            String s = link.text();
            if (!list.contains(s))
                list.add(s + "\n");
        }
        for (String link : list) {
            System.out.println(link);
        }
        return list;
    }
//"C:/Users/Елена/Desktop/result.txt"

    private void writeInFile(ArrayList<String> list) throws ClassNotFoundException {
        try (BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.getText()), "windows-1251"))) {
            // try (BufferedWriter fw = new BufferedWriter(new FileWriter(path.getText(), false))) {
            for (String x : list) {
                fw.write(x);
            }
        } catch (IOException e) {
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

//    private static String trim(String s, int width) {
//        if (s.length() > width)
//            return s.substring(0, width - 1) + ".";
//        else
//            return s;
//    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}

