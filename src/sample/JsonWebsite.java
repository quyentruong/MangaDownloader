package sample;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

class JsonWebsite {
    private JSONObject website;
    private JSONObject websites;
    private Set<String> mangaSet;
    private boolean isValidURL;


    JsonWebsite(String url) {
        mangaSet = new HashSet<>();
        init();

        for (String w : mangaSet) {
            if (url.contains(w)) {
                website = websites.getJSONObject(w);
                isValidURL = true;
                break;
            } else {
                isValidURL = false;
            }
        }

        if (!isValidURL || url.toLowerCase().contains("chap")) {
            website = null;
        }
    }

    JSONObject getWebsite() {
        return website;
    }

    private void init() {
        websites = new JSONObject();
        new VNManga("hamtruyen", "h1.tentruyen", "div.content section.row_chap",
                ".col_chap a", ".content_chap img", "a.link_truyen:eq(2)");

        new VNManga("truyensieuhay", "div.title h1", "div.list-chap-story ul li",
                "a", ".content_chap img", "ul.title-header li:eq(1)");

        new VNManga("nettruyen", "h1.title-detail", "div.list-chapter nav ul li.row div.chapter",
                "a", "div.page-chapter img", "h1.txt-primary span");

        new VNManga("truyenchon", "h1.title-detail", "div.list-chapter nav ul li.row div.chapter",
                "a", "div.page-chapter img", "h1.txt-primary span");

        new VNManga("truyenpub", "h1", "div.manga_chapter_list_content ul li span.chap_name",
                "a", "div.chap_viewer img", "div.chap_title h1");

        new VNManga("uptruyen", "div.breadcrumb > span > a:eq(5)", "div#chapter_table div.row div.detail-chap-name",
                "a", "div#reader-box img.img-page", "div#reader-box img.page-scroll-1");
    }


    private class VNManga {
        /**
         * If you want update a new website that loading many pages at once, start with this templage.
         * Otherwise, it doesn't work with a website only load one page at once.
         *
         * @param name       Name of manga website without .com, wwww, http(s). Ex: hamtruyen
         * @param mangaTitle Title of a manga. Often start with 'h1'
         * @param listChap   List of available chapters in that page. Often start with 'div' and has class 'list', 'chapter'
         * @param linkChap   Link to get to that chapter. Often start with 'a', a child of listChap
         * @param pages      Number of available pages in that chapter. Often end with img.
         * @param chapTitle  Chapter number. This one is tricky to get. Often start with h1, or get directly from img
         */
        private VNManga(String name, String mangaTitle, String listChap, String linkChap, String pages, String chapTitle) {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("mangaTitle", mangaTitle);
            json.put("listChap", listChap);
            json.put("linkChap", linkChap);
            json.put("pages", pages);
            json.put("chapTitle", chapTitle);
            websites.put(name, json);
            mangaSet.add(name);
        }
    }
}
