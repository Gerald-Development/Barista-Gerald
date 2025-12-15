package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import org.apiguardian.api.API;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    private static final String SEARCH_BASE_URL = "https://imgflip.com/memesearch?q=";
    private static final String API_URL = "https://api.imgflip.com/caption_image";

    private static final int CARD_WIDTH = 1000;
    private static final int CARD_HEIGHT = 300;
    private static final int CORNER_RADIUS = 20;
    private static final int BAR_WIDTH = 650;
    private static final int BAR_X = 310;
    private static final int BAR_Y = 185;
    private static final int AVATAR_SIZE = 224;
    private static final int AVATAR_X = 38;
    private static final int AVATAR_Y = 38;

    private static final String BAR_FROM = "#F24548";
    private static final String BAR_TO = "#3B43D5";
    private static final String BACKGROUND = "#2F3136";

    @Autowired
    private GeraldConfigService geraldConfigService;
    @Autowired
    private HttpClientService httpClientService;

    public byte[] createExperienceCard(
            String avatarURL,
            long xpAchieved,
            long xpNeededToLevelUp,
            long level,
            long rank,
            String username
    ) throws IOException {
        BufferedImage canvas = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ctx = canvas.createGraphics();

        ctx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ctx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        BufferedImage avatar = ImageIO.read(new URL(avatarURL));

        ctx.setClip(new RoundRectangle2D.Float(0, 0, CARD_WIDTH, CARD_HEIGHT, CORNER_RADIUS * 2, CORNER_RADIUS * 2));
        ctx.setColor(Color.decode(BACKGROUND));
        ctx.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
        ctx.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        ctx.setColor(Color.decode("#242424"));
        ctx.drawLine(BAR_X, BAR_Y, BAR_X + BAR_WIDTH, BAR_Y);

        float progress = (float) xpAchieved / xpNeededToLevelUp;
        int progressWidth = (int) (BAR_WIDTH * progress);
        GradientPaint gradient = new GradientPaint(
                BAR_X, BAR_Y, Color.decode(BAR_FROM),
                BAR_X + BAR_WIDTH, BAR_Y, Color.decode(BAR_TO));
        ctx.setPaint(gradient);
        ctx.drawLine(BAR_X, BAR_Y, BAR_X + progressWidth, BAR_Y);

        ctx.setColor(Color.WHITE);
        setTextSize(ctx, username, 680, 50);
        ctx.drawString(username, 295, 100);

        ctx.setFont(loadCustomFont("/fonts/whitneylight.otf", 35));
        String rankLevel = "Rank #" + rank + " • Level " + level;
        ctx.drawString(rankLevel, 295, 240);

        ctx.setColor(Color.decode("#737373"));
        ctx.setFont(loadCustomFont("/fonts/whitneylight.otf", 35));
        String experienceFraction = xpAchieved + " / " + xpNeededToLevelUp + " XP";
        int percentage = (int) ((xpAchieved * 100.0) / xpNeededToLevelUp);
        String experiencePercentage = percentage + "%";
        String xpText = experienceFraction + " • " + experiencePercentage;
        ctx.drawString(xpText, 295, 155);

        ctx.setClip(new RoundRectangle2D.Float(AVATAR_X, AVATAR_Y, AVATAR_SIZE, AVATAR_SIZE, CORNER_RADIUS * 2, CORNER_RADIUS * 2));
        ctx.drawImage(avatar, AVATAR_X, AVATAR_Y, AVATAR_SIZE, AVATAR_SIZE, null);

        ctx.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", baos);
        return baos.toByteArray();
    }

    private void setTextSize(Graphics2D ctx, String text, int maxWidth, int startSize) {
        int size = startSize;
        Font font = loadCustomFont("/fonts/whitneylight.otf", size);
        ctx.setFont(font);
        FontMetrics metrics = ctx.getFontMetrics(font);

        while (metrics.stringWidth(text) > maxWidth && size > 8) {
            size--;
            font = loadCustomFont("/fonts/whitneylight.otf", size);
            ctx.setFont(font);
            metrics = ctx.getFontMetrics(font);
        }
    }

    private Font loadCustomFont(String fontPath, float size) {
        try (InputStream is = getClass().getResourceAsStream(fontPath)) {

            if (is == null) {
                throw new IllegalStateException("Font not found at path: " + fontPath);
            }

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            return baseFont.deriveFont(Font.BOLD, size);

        } catch (Exception e) {
            e.printStackTrace();
            return new Font(Font.SANS_SERIF, Font.BOLD, (int) size);
        }
    }

    public String searchMeme(String name) {
        StringBuilder searchUrl = new StringBuilder(SEARCH_BASE_URL);
        String[] words = name.split(" ");

        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                searchUrl.append(URLEncoder.encode(words[i], StandardCharsets.UTF_8));
            } else {
                searchUrl.append("+").append(URLEncoder.encode(words[i], StandardCharsets.UTF_8));
            }
        }
        searchUrl.append("&nsfw=on");

        try {
            Document doc = Jsoup.connect(searchUrl.toString()).get();
            Elements links = doc.select("a");
            List<String> hrefs = new ArrayList<>();

            for (Element link : links) {
                hrefs.add(link.attr("href"));
            }

            List<String> ids = new ArrayList<>();
            for (String href : hrefs) {
                String[] parts = href.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("meme") && i + 1 < parts.length) {
                        ids.add(parts[i + 1]);
                    }
                }
            }

            if (!ids.isEmpty()) {
                return ids.get(0);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public JSONObject generateMeme(String id, List<String> text) {

        HttpUrl.Builder formBuilder = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("username", geraldConfigService.getMemeApiUsername())
                .addQueryParameter("password", geraldConfigService.getMemeApiPassword())
                .addQueryParameter("template_id", id);

        for (int i = 0; i < text.size(); i++) {
            formBuilder.addQueryParameter("boxes[" + i + "][text]", text.get(i));
        }

        if (text.isEmpty()) {
            formBuilder.addQueryParameter("text0", " ");
        }

        String response = httpClientService.postEmptyBody(formBuilder.build());
        return new JSONObject(response);
    }

    public JSONObject getMeme(String name, List<String> text) {
        String id = searchMeme(name);
        if (id == null) {
            return null;
        }
        return generateMeme(id, text);
    }

}
