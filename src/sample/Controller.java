package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

public class Controller implements Initializable {
    private File selectedDirectory;
    private ArrayList<String> log;

    @FXML
    private TextField urlTxt;

    @FXML
    private Button StartBtn;

    @FXML
    private TextFlow statusTxt;

    @FXML
    private Button HelpBtn;

    @FXML
    private TextField beginTxt;

    @FXML
    private TextField endTxt;

    @FXML
    private Button StopBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControlSubThread ct = new ControlSubThread();
        StartBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory == null) {
                showText("No Directory selected", false, true);
            } else {
//                System.out.println(selectedDirectory.getAbsolutePath());
                log = new ArrayList<>();
                ct.start();
                StartBtn.setVisible(false);
                StopBtn.setVisible(true);
            }
        });
        StopBtn.setOnAction(event -> {
            ct.stop();
            StartBtn.setVisible(true);
            StopBtn.setVisible(false);
        });
        HelpBtn.setOnAction(event -> {
            showText("Support: hamtruyen, truyensieuhay, nettruyen\n               truyenchon, truyenpub, uptruyen");
            showText("Put the same number in 'begin' and 'end' chap will download that Chapter.\nEx: Put 20 in 'begin' and 'end' will download Chapter 20", true);
            showText("Put in URL like examples below", true);
            ClickAbleLink("http://truyensieuhay.com/thoi-dai-x-long-1386.html");
            ClickAbleLink("http://uptruyen.com/manga/32227/adventure/the-gioi-tien-hiep.html");
            ClickAbleLink("http://www.nettruyen.com/truyen-tranh/dau-la-dai-luc");
        });
        ChapterTextFormat();
    }

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

    private void ClickAbleLink(String text) {
        Platform.runLater(() -> {
            Hyperlink example = new Hyperlink(text);
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

    private void showText(String text, boolean append, boolean warning) {
        Platform.runLater(() -> {
            if (!append) statusTxt.getChildren().clear();
            Text t = new Text(text + "\n");
            t.setFill(Color.BLACK);
            t.setFont(Font.font(20));
            if (warning) t.setFill(Color.RED);
            statusTxt.getChildren().add(t);
            log.add(text);
        });

    }

    private void showText(String text, boolean append) {
        showText(text, append, false);
    }

    private void showText(String text) {
        showText(text, false, false);
    }

    class ControlSubThread implements Runnable {
        Modules Manga;
        private Thread worker;

        void start() {
            worker = new Thread(this);
            worker.setDaemon(true);
            worker.start();
        }

        void stop() {
            Manga.setStop(true);
        }

        @Override
        public void run() {
            Manga = new Modules(urlTxt.getText(), Integer.parseInt(beginTxt.getText()), Integer.parseInt(endTxt.getText()));
            Manga.DownloadSaveChap();
        }
    }

    class Modules {
        private String url;
        private int begin;
        private int end;
        private Element title;
        private JSONObject website;
        private Extra extra;
        private Boolean stop = false;

        void setStop(Boolean stop) {
            this.stop = stop;
        }

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
            if (links.isEmpty()) return;

            for (String url : links) {
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).timeout(10000).get();
                } catch (IOException e) {
                    e.printStackTrace();
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
                        return;
                    }
                    String src = page.attr("src");
                    String decode;
                    URL image;
                    String fileName = String.format("%s/%s/%s/%s/%03d.jpg", selectedDirectory.getAbsolutePath(), website.getString("name"), title.text(), chapTitle_s, pageNumber);
                    try {
                        decode = URLDecoder.decode(src, "UTF-8");
                        image = new URL(src);
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
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showText("Done", true);
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
        }

        private List<String> getListChapter() {
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
            return links;
        }
    }
}
