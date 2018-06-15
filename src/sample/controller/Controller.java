package sample.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.extra.Ekstra;
import sample.extra.JsonWebsite;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Control elements in UI
 *
 * @author Quyen Truong
 * @version 1.4
 */
public class Controller {
    private File selectedDirectory;
    private ArrayList<String> log;
    private Boolean isLog = false;
    private Boolean stop = false;
    private int task_count;
    private int totalPages;
    private Element title;
    private JSONObject website;
    private Ekstra ekstra;
    private String chapTitle_s;

    @FXML
    private AnchorPane anchorP;

    @FXML
    private TextField beginTxt;

    @FXML
    private TextField endTxt;

    @FXML
    private TextFlow statusTxt;

    @FXML
    private JFXTextField urlTxt;

    @FXML
    private JFXButton HelpBtn;

    @FXML
    private JFXButton StartBtn;

    @FXML
    private JFXButton StopBtn;

    @FXML
    private JFXProgressBar pbar;

    @FXML
    private Label processTxt;

    @FXML
    private ScrollPane scrollP;

    JFXComboBox<Integer> comboBox;

    private void comboxSetup() {
        comboBox = new JFXComboBox<>();
        comboBox.setId("textField");
        comboBox.setStyle("-fx-font-size: 20px;");
        comboBox.getItems().add(1);
        comboBox.getItems().add(2);
        comboBox.getItems().add(3);
        comboBox.getItems().add(4);
        comboBox.getSelectionModel().selectFirst();
        comboBox.setLayoutX(149);
        comboBox.setLayoutY(122);
        comboBox.setPrefHeight(41);
        comboBox.setPrefWidth(90);
        anchorP.getChildren().add(comboBox);
    }


    @FXML
    public void initialize() {
        comboxSetup();
        HelpBtn.setId("circle");
        StartBtn.setId("circle");
        ControlSubThread ct = new ControlSubThread();
        StartBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory == null) {
                showText("No Directory selected", false, true);
            } else {
                stop = false;
                isLog = true;
                log = new ArrayList<>();
                ct.start();
                statusComponent(false);
                statusTxt.requestFocus();
            }
        });
        StopBtn.setOnAction(event -> {
            ct.stop();
            statusComponent(true);
            isLog = false;
        });
        HelpBtn.setOnAction(event -> {
            isLog = false;

            showText("Support: hamtruyen, truyensieuhay, nettruyen\n               truyenchon, truyenpub, uptruyen");
            showText("Put the same number in 'begin' and 'end' chap will download that Chapter.\nEx: Put 20 in 'begin' and 'end' will download Chapter 20", true);
            showText("Put in URL like examples below", true);
            showText(String.valueOf(comboBox.getValue()), true);
            ClickAbleLink("http://truyensieuhay.com/thoi-dai-x-long-1386.html");
            ClickAbleLink("http://uptruyen.com/manga/32227/adventure/the-gioi-tien-hiep.html");
            ClickAbleLink("http://www.nettruyen.com/truyen-tranh/dau-la-dai-luc");
        });
        ChapterTextFormat();

    }

    /**
     * Text format for 'begin' and 'end' TextField
     * such as Limit to 4 chars and only allow numbers
     */
    private void ChapterTextFormat() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String input = change.getText();
            if (input.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        beginTxt.setOnKeyTyped(event -> {
            int maxCharacters = 4;
            if (beginTxt.getText().length() >= maxCharacters) event.consume();
        });
        endTxt.setOnKeyTyped(event -> {
            int maxCharacters = 4;
            if (endTxt.getText().length() >= maxCharacters) event.consume();
        });
        beginTxt.setTextFormatter(new TextFormatter<>(integerFilter));
        endTxt.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    private void statusComponent(boolean stop) {
        if (stop) {
            StartBtn.setVisible(true);
            StopBtn.setVisible(false);
            HelpBtn.setDisable(false);
            urlTxt.setDisable(false);
            beginTxt.setDisable(false);
            endTxt.setDisable(false);
            pbar.setVisible(false);
            processTxt.setVisible(false);
        } else {
            StartBtn.setVisible(false);
            StopBtn.setVisible(true);
            HelpBtn.setDisable(true);
            urlTxt.setDisable(true);
            beginTxt.setDisable(true);
            endTxt.setDisable(true);
            pbar.setVisible(true);
            processTxt.setVisible(true);
        }
    }

    private void stopAction(boolean downloaded) {
        if (downloaded)
            showText(String.format("Your manga downloaded in %s/%s/%s/", selectedDirectory.getAbsolutePath(), website.getString("name"), title.text()), true);
        String logName = title != null ? title.text() + "_" + website.getString("name") : "error";
        try (PrintWriter writer = new PrintWriter(String.format("%s.log", logName), "UTF-8")) {
            for (String l : log) {
                writer.println(l);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showText(String.format("Log saved in %s/%s.log", System.getProperty("user.dir"), logName), true);
        statusComponent(true);
    }

    /**
     * @param link A valid link to click and add to TextFlow
     */
    private void ClickAbleLink(String link) {
        Platform.runLater(() -> {
            Hyperlink example = new Hyperlink(link);
            example.setFont(Font.font(20));
            example.setOnAction(event1 -> {
                try {
                    Desktop.getDesktop().browse(new URI(example.getText()));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            });
            statusTxt.getChildren().add(example);
        });
    }

    /**
     * @param text    A simple text
     * @param append  Set true to append without clear old text in TextFlow
     * @param warning Set true the text is red. Otherwise, black
     */
    private void showText(String text, boolean append, boolean warning) {
        Platform.runLater(() -> {
            if (!append) statusTxt.getChildren().clear();
            Text t = new Text(text + "\n");
            t.setId("default");
            if (warning) t.setId("warning");
            statusTxt.getChildren().add(t);
            if (isLog) log.add(text);
            anchorP.layout();
            scrollP.setVvalue(1);
        });

    }

    /**
     * @param text   A simple text
     * @param append Set true to append without clear old text in TextFlow
     */
    private void showText(String text, boolean append) {
        showText(text, append, false);
    }

    /**
     * This method cleans old text
     *
     * @param text A simple text
     */
    private void showText(String text) {
        showText(text, false, false);
    }

    /**
     * Thread to run DownloadSaveChap() of Modules
     */
    private class ControlSubThread implements Runnable {
        Modules Manga;
        private Thread worker;

        void start() {
            stop = false;
            worker = new Thread(this);
            worker.setDaemon(true);
            worker.start();
        }

        void stop() {
            stop = true;
        }

        @Override
        public void run() {
            Manga = new Modules(urlTxt.getText(), Integer.parseInt(beginTxt.getText()), Integer.parseInt(endTxt.getText()));
            Manga.DownloadSaveChap();
        }
    }

    class URLtoFile implements Runnable {
        List<Element> pages;
        private int pageNumber;
        String ThreadName;

        URLtoFile(String ThreadName, List<Element> pages, int pageNumber) {
            this.pages = pages;
            this.pageNumber = pageNumber;
            this.ThreadName = ThreadName;
        }

        @Override
        public void run() {
            for (Element page : pages) {
                if (stop) {
//                    showText("Stop downloading", true, true);
//                    stopAction(true);
//                    stop = false;
                    return;
                }
                String src = page.attr("src");

                URL image;
                String fileName = String.format("%s/%s/%s/%s/%03d", selectedDirectory.getAbsolutePath(), website.getString("name"), title.text().trim(), chapTitle_s.trim(), pageNumber);
                try {
                    image = ekstra.parseURL(src);
                    if (image == null)
                        throw new Exception("Cannot parse URL");
//                    System.out.println(ThreadName + " is running " + image + "  " + pageNumber);

                    FileUtils.copyURLToFile(image, new File(fileName), 15000, 15000);

                    if (ekstra.isValidJPG(fileName)) {
                        if (new File(fileName + ".jpg").exists())
                            FileUtils.forceDelete(new File(fileName + ".jpg"));
                        FileUtils.moveFile(new File(fileName), new File(fileName + ".jpg"));
                    } else if (ekstra.isValidPNG(fileName)) {
                        if (new File(fileName + ".png").exists())
                            FileUtils.forceDelete(new File(fileName + ".png"));
                        FileUtils.moveFile(new File(fileName), new File(fileName + ".png"));
                    } else if (ekstra.isValidGIF(fileName)) {
                        FileUtils.forceDelete(new File(fileName));
                        showText(String.format("Delete %s", fileName + ".gif"), true, true);
                    }
                } catch (Exception e) {
                    try {
                        new RandomAccessFile(new File(fileName), "rw").setLength(100);
                        showText(fileName + " is a null file.", true, true);
                    } catch (Exception e1) {
                        showText(fileName + " is not available", true, true);
                    }
                }

                task_count += 1;
                Platform.runLater(() -> processTxt.setText(String.format("%d/%d", task_count, totalPages)));
                pbar.setProgress(task_count * 1.0 / totalPages);
                pageNumber += 1;
            }

        }
    }

    /**
     * Parse all information in manga website then start download
     */
    private class Modules {
        private String url;
        private int begin;
        private int end;

        /**
         * For example, download this manga from chapter 1 to chapter 30
         *
         * @param url   Ex:http://truyensieuhay.com/thoi-dai-x-long-1386.html
         * @param begin Ex: 1
         * @param end   Ex: 30
         */
        Modules(String url, int begin, int end) {
            this.url = url;
            this.begin = begin;
            this.end = end;
            ekstra = new Ekstra();
            assert begin != 0 : "begin has to be greater than 0";
            assert end != 0 : "end has to be greater than 0";
            website = new JsonWebsite(url).getWebsite();
        }

        void DownloadSaveChap() {
            List<String> links = getListChapter();
            if (links.isEmpty()) {
                stopAction(false);
                return;
            }

            for (String url : links) {
                task_count = 0;
                Document doc;
                try {
                    doc = Jsoup.connect(url).timeout(10000).get();
                } catch (IOException e) {
                    try {
                        showText("Connection problem. Trying to reconnect ...", true, true);
                        doc = Jsoup.connect(url).timeout(50000).get();
                    } catch (IOException e1) {
                        showText("Cannot reconnect. Please try download again later.", true, true);
                        stopAction(true);
                        return;
//                        e1.printStackTrace();
                    }
                }
                assert doc != null;
                Elements pages = doc.select(website.getString("pages"));
                Elements chapTitle = doc.select(website.getString("chapTitle"));

                chapTitle_s = chapTitle.text().toLowerCase().contains("chap") ? chapTitle.text() : chapTitle.attr("alt");
                chapTitle_s = chapTitle_s.replaceAll("[\\\\/:*?\"<>|]", "");
                if (!stop) showText(String.format("Downloading %s ...", chapTitle_s), true);
//                int pageNumber = 1;
                pbar.setProgress(0);
                totalPages = pages.size();
                int maxThread = 3;
                URLtoFile[] tasks = new URLtoFile[maxThread];
                Thread[] threads = new Thread[maxThread];
                for (int i = 0; i < maxThread; i++) {
                    double low = totalPages * i * 1.0 / maxThread;
                    double high = totalPages * (i + 1) * 1.0 / maxThread;
                    int pageNumber = (int) low + 1;
                    tasks[i] = new URLtoFile("Task " + (i + 1), pages.subList((int) low, (int) high), pageNumber);
                    threads[i] = new Thread(tasks[i]);
                    threads[i].start();
                }
//                double mid1 = pages.size() / 3.0;
//                double mid2 = pages.size() * 2 / 3.0;
//                URLtoFile task1 = new URLtoFile("Task 1", pages.subList(0, (int) mid1), chapTitle_s, 1);
//                URLtoFile task2 = new URLtoFile("Task 2", pages.subList((int) mid1, (int) mid2), chapTitle_s, (int) Math.ceil(mid1) + 1);
//                URLtoFile task3 = new URLtoFile("Task 3", pages.subList((int) mid2, pages.size()), chapTitle_s, (int) Math.ceil(mid2) + 1);
//                Thread t = new Thread(task1);
//                Thread t2 = new Thread(task2);
//                Thread t3 = new Thread(task3);
//
//                t.start();
//                t2.start();
//                t3.start();
                if (stop) {
                    showText("Stop downloading", true, true);
                    stopAction(true);
                    return;
                }

                try {
                    for (Thread thread : threads) {
                        thread.join(0);
                    }
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showText("Done", true);
            stopAction(true);
        }


        /**
         * @return List of chapter of a manga
         */
        private List<String> getListChapter() {
            if (website == null) {
                showText("Invalid URL", false, true);
                return Collections.emptyList();
            }
            URI uri = null;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            String domain = null;
            String scheme = null;
            try {
                assert uri != null;
                domain = uri.getHost();
                scheme = uri.getScheme();
                domain = domain.startsWith("www.") ? domain.substring(4) : domain;
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.exit(0);
            }


            Document doc;
            ekstra.enableSSLSocket();
            try {
                doc = Jsoup.connect(url).timeout(10000).get();
            } catch (Exception e) {
                showText("Invalid URL", false, true);
                return Collections.emptyList();
            }

            assert doc != null : "doc cannot be empty";
            showText("Please wait until it's done ...");

            title = doc.select(website.getString("mangaTitle")).first();
            showText(String.format("Fetching %s...", url), true);
            List<Element> chaps = doc.select(website.getString("listChap"));
            Collections.reverse(chaps);
            begin -= 1;

            assert begin <= end : "begin has to be smaller than end";
            int maxChap = chaps.size();
            try {
                showText("Found " + title.text(), true);
                showText("MaxChap: " + maxChap, true);
            } catch (NullPointerException e) {
                showText("Found nothing", true, true);
            }
            try {
                chaps = chaps.subList(begin, end > maxChap ? maxChap : end);
            } catch (IllegalArgumentException e) {
                showText("'Begin chap' has to be less than 'End Chap'", true, true);
                showText("Or 'Begin chap' doesn't exist", true, true);
                showText("Or 'End chap' doesn't exist", true, true);
                return Collections.emptyList();
            }

            ArrayList<String> links = new ArrayList<>();

            for (Element chap : chaps) {
                String link = chap.select(website.getString("linkChap")).attr("href");
                link = String.format("%s%s%s", link.contains("http") ? "" : scheme + "://",
                        link.contains(website.getString("name")) ? "" : domain, link);
                links.add(link);
            }

            return links;
        }
    }
}
