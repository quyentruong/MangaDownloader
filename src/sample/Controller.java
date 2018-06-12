package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
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

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

/**
 * Control elements in UI
 *
 * @author Quyen Truong
 * @version 1.1
 */
public class Controller implements Initializable {
    private File selectedDirectory;
    private ArrayList<String> log;
    private Boolean isLog = false;
    private Boolean stop = false;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControlSubThread ct = new ControlSubThread();
        StartBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory == null) {
                showText("No Directory selected", false, true);
            } else {
                isLog = true;
                log = new ArrayList<>();
                ct.start();
                HelpBtn.setDisable(true);
                StartBtn.setVisible(false);
                StopBtn.setVisible(true);
            }
        });
        StopBtn.setOnAction(event -> {
            ct.stop();
            HelpBtn.setDisable(false);
            StartBtn.setVisible(true);
            StopBtn.setVisible(false);
            isLog = false;
        });
        HelpBtn.setOnAction(event -> {
            isLog = false;
            showText("Support: hamtruyen, truyensieuhay, nettruyen\n               truyenchon, truyenpub, uptruyen");
            showText("Put the same number in 'begin' and 'end' chap will download that Chapter.\nEx: Put 20 in 'begin' and 'end' will download Chapter 20", true);
            showText("Put in URL like examples below", true);
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
            t.setFill(Color.BLACK);
            t.setFont(Font.font(20));
            if (warning) t.setFill(Color.RED);
            statusTxt.getChildren().add(t);
            if (isLog) log.add(text);
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

    /**
     * Parse all information in manga website then start download
     */
    private class Modules {
        private String url;
        private int begin;
        private int end;
        private Element title;
        private JSONObject website;
        private Extra extra;

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
            extra = new Extra();
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

                String chapTitle_s = chapTitle.text().toLowerCase().contains("chap") ? chapTitle.text() : chapTitle.attr("alt");
                chapTitle_s = chapTitle_s.replaceAll("[\\\\/:*?\"<>|]", "");
                showText(String.format("Downloading %s ...", chapTitle_s), true);
                int pageNumber = 1;

                for (Element page : pages) {
                    if (stop) {
                        showText("Stop downloading", true, true);
                        stopAction(true);
                        stop = false;
                        return;
                    }
                    String src = page.attr("src");

                    String decode;
                    URL image;
                    String fileName = String.format("%s/%s/%s/%s/%03d.jpg", selectedDirectory.getAbsolutePath(), website.getString("name"), title.text(), chapTitle_s, pageNumber);
                    try {
                        decode = URLDecoder.decode(src, "UTF-8");
                        if (!decode.contains("http")) decode = "https:" + decode;
                        image = new URL(decode);
                        if (decode.contains("url")) {
                            image = new URL(decode.split("url=")[1]);
                        }
                        FileUtils.copyURLToFile(image, new File(fileName), 15000, 15000);
                    } catch (Exception e) {
                        try {
                            new RandomAccessFile(new File(fileName), "rw").setLength(100);
                            showText(fileName + " is a null file.", true, true);
                        } catch (Exception e1) {
                            showText(fileName + " is not available", true, true);
                        }
                    }
                    pageNumber += 1;
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showText("Done", true);
            stopAction(true);
        }

        private void stopAction(boolean downloaded) {
            if (downloaded)
                showText(String.format("Your manga downloaded in %s/%s/%s/", selectedDirectory.getAbsolutePath(), website.getString("name"), title.text()), true);
            try (PrintWriter writer = new PrintWriter(title.text() + ".log", "UTF-8")) {
                for (String l : log) {
                    writer.println(l);
                }
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            showText(String.format("Log saved in %s/%s.log", System.getProperty("user.dir"), title.text()), true);
            StartBtn.setVisible(true);
            StopBtn.setVisible(false);
            HelpBtn.setDisable(false);
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


            Document doc = null;
            extra.enableSSLSocket();
            try {
                doc = Jsoup.connect(url).timeout(10000).get();
            } catch (IOException e) {
                e.printStackTrace();
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
            showText("MaxChap: " + maxChap, true);
            try {
                chaps = chaps.subList(begin, end > maxChap ? maxChap : end);
            } catch (IndexOutOfBoundsException e) {
                showText(String.format("Chapter %d not exist", end), true);
                return Collections.emptyList();
            }

            ArrayList<String> links = new ArrayList<>();

            for (Element chap : chaps) {
                String link = chap.select(website.getString("linkChap")).attr("href");
                link = String.format("%s%s%s", link.contains("http") ? "" : scheme + "://",
                        link.contains(website.getString("name")) ? "" : domain, link);
                links.add(link);
            }
//            System.out.println(links);
//            System.exit(0);
            return links;
        }
    }
}
