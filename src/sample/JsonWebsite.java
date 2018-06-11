package sample;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

class JsonWebsite {
    private JSONObject website;
    private JSONObject websites;
    private Set<String> mangaSet;


    JsonWebsite(String url) {
        mangaSet = new HashSet<>();
        init();
        for (String w : mangaSet) {
            if (url.contains(w)) {
                website = websites.getJSONObject(w);
            }
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
