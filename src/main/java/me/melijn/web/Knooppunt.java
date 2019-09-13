package me.melijn.web;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Knooppunt {

    private static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        String url = "";
        String answerUrl = "";
        String cookie = "";
        int start = 0;
        int end = 340;

        if (args.length == 0) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Select mode (Answers and Questions: aaq; Answers: a; Questions: q)");
            String mode = reader.readLine();
            switch (mode.toLowerCase()) {
                case "aaq":
                    System.out.println("Enter questions url (without 001.png): ");
                    url = reader.readLine();

                    System.out.println("Enter answers url (without 001.png): ");
                    answerUrl = reader.readLine();

                    System.out.println("Enter your knooppunt cookie: ");
                    cookie = reader.readLine();

                    System.out.println("From page: ");
                    start = Integer.parseInt(reader.readLine());

                    System.out.println("To page: ");
                    end = Integer.parseInt(reader.readLine());

                    downloadBoth(url, answerUrl, cookie, start, end);
                    break;
                case "a":
                    System.out.println("Enter answers url (without 001.png): ");
                    answerUrl = reader.readLine();

                    System.out.println("Enter your knooppunt cookie: ");
                    cookie = reader.readLine();

                    System.out.println("From page: ");
                    start = Integer.parseInt(reader.readLine());

                    System.out.println("To page: ");
                    end = Integer.parseInt(reader.readLine());

                    downloadOne(answerUrl, "answers", cookie, start, end);

                    break;
                case "q":
                    System.out.println("Enter questions url (without 001.png): ");
                    url = reader.readLine();

                    System.out.println("Enter your knooppunt cookie: ");
                    cookie = reader.readLine();

                    System.out.println("From page: ");
                    start = Integer.parseInt(reader.readLine());

                    System.out.println("To page: ");
                    end = Integer.parseInt(reader.readLine());

                    downloadOne(url, "questions", cookie, start, end);

                    break;
                default:
                    System.out.println("Not a mode");
                    break;
            }
        } else if (args[0].equalsIgnoreCase("code")) {
            downloadBoth(url, answerUrl, cookie, start, end);
        }
    }

    private static void downloadOne(String url, String folder, String cookie, int start, int end) {
        for (int i = start; i < end; i++) {
            Request request = new Request.Builder()
                .get()
                .url(url + i + ".png")
                .header("Cookie", cookie)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.header("Content-Type") == null) return;
                if (response.header("Content-Type").equalsIgnoreCase("image/png")) {
                    if (response.body() == null) return;
                    File file = new File(folder + File.separator + "img-" + i + ".png");
                    file.getParentFile().mkdirs();
                    Files.write(Paths.get(file.getPath()), response.body().bytes());
                    System.out.println("Written file " + i + " to disk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void downloadBoth(String url, String answerUrl, String cookie, int start, int end) {
        for (int i = start; i < end; i++) {
            Request request = new Request.Builder()
                .get()
                .url(url + i + ".png")
                .header("Cookie", cookie)
                .build();

            Request answerRequest = new Request.Builder()
                .get()
                .url(answerUrl + i + ".png")
                .header("Cookie", cookie)
                .build();


            try (Response response = client.newCall(request).execute();
                 Response response1 = client.newCall(answerRequest).execute()) {
                if (response.header("Content-Type") == null) continue;
                if (response.header("Content-Type").equalsIgnoreCase("image/png")) {
                    if (response.body() == null) continue;
                    byte[] data = response.body().bytes();
                    File file = new File("normal" + File.separator + "img-" + i + ".png");
                    file.getParentFile().mkdirs();
                    Files.write(Paths.get(file.getPath()), data);
                    System.out.println("Written file " + i + " to disk");

                    BufferedImage answerImage = response1.body() == null ? null : ImageIO.read(response1.body().byteStream());
                    if (answerImage != null) {
                        BufferedImage img = ImageIO.read(file);
                        Graphics2D g = img.createGraphics();
                        g.drawImage(answerImage, 0, 0, null);
                        g.dispose();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(img, "png", baos);
                        baos.flush();
                        data = baos.toByteArray();
                        baos.close();
                    }


                    File filledFile = new File("filled" + File.separator + "img-" + i + ".png");
                    filledFile.getParentFile().mkdirs();
                    Files.write(Paths.get(filledFile.getPath()), data);

                    System.out.println("Written filled file " + i + " to disk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
