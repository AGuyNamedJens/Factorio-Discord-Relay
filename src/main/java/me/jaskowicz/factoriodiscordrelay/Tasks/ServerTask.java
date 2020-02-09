package me.jaskowicz.factoriodiscordrelay.Tasks;

import me.jaskowicz.factoriodiscordrelay.Main;
import me.jaskowicz.factoriodiscordrelay.Settings.MAIN_SETTINGS;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerTask extends TimerTask {

    private List<String> lastResults = new ArrayList<>();
    
    private static String pretifyLogLine(String logLine) {
    	return logLine.replaceAll("^.+\\[\\w\\]\\s(.+)$", "$1");
    }
    
    private void sendMessage(String channelId, String message) {
    	Objects.requireNonNull(Objects.requireNonNull(Main.jda.getGuildById(Main.guildID)).getTextChannelById(channelId))
        .sendMessage(message).queue();
    }

    @Override
    public void run() {
        File file = new File(MAIN_SETTINGS.serverOutFilePath);

        if(file.exists()) {

            Scanner scanner = null;
            ReversedLinesFileReader reversedLinesFileReader = null;

            try {
                scanner = new Scanner(file);
                reversedLinesFileReader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.out.println("server.out file does not exist or can not be read! Please attempt to fix this.");
                return;
            }

            if (!scanner.hasNextLine()) {
                System.out.println("server.out file does not have any information!");
                return;
            }

            List<String> results = new ArrayList<>();

            try {
                String currentLine = "";
                while ((currentLine = reversedLinesFileReader.readLine()) != null && results.size() < 3) {
                    results.add(currentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Main.jda.getStatus() == JDA.Status.CONNECTED) {
                if (!results.get(0).equals(lastResults.get(0))) {
                    for(int i = 0; i < results.size(); i++) {
                        if (results.get(i).contains("[JOIN]")) {
                        	String message = ":heavy_plus_sign: " + pretifyLogLine(results.get(i));

                            sendMessage(Main.chatChannelID, message);
                            break;
                        } else if (results.get(i).contains("[LEAVE]")) {
                        	String message = ":heavy_minus_sign: " + pretifyLogLine(results.get(i));

                        	sendMessage(Main.chatChannelID, message);
                            break;
                        } else if (results.get(i).contains("[CHAT]")) {
                        	String message = ":speech_balloon: " + pretifyLogLine(results.get(i));

                        	sendMessage(Main.chatChannelID, message);
                            break;
                        } else if (results.get(i).contains("[COMMAND]")) {
                            String message = ":exclamation: " + results.get(i);

                            sendMessage(Main.consoleChannelID, message);
                            break;
                        } else if (results.get(i).contains("Cannot execute command.")) {
                            String message = ":warning: " + results.get(i);

                            sendMessage(Main.consoleChannelID, message);
                        }
                    }
                }
            }

            lastResults.clear();
            lastResults.addAll(results);
        } else {
            System.out.println("Server.out file is missing!");
        }
    }
}
