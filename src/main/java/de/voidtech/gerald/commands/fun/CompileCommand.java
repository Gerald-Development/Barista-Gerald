package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CompileCommand extends AbstractCommand {

	private static final String WANDBOX_COMPILE_URL = "https://wandbox.org/compile";
	private static final String EMBED_THUMBNAIL_URL = "https://cdn.discordapp.com/attachments/772921190280724512/790283222156050462/7398c7bd6be35540-the-gallery-for-processing-gif-transparent-web-page-processing.gif";

	@Override
	public String getDescription() {
		return "Allows you to compile code via wandbox";
	}

	@Override
	public String getUsage() {
		return "compile ```[name of language] [code]``` \nOR compile languages";
	}

	@Override
	public void executeInternal(Message message, List<String> args) {

		Map<String, String> langMap = getSupportedLangs();
		
		if (args.size() >= 0 && args.get(0).equals("languages")) {

			String supportedLangsString = StringUtils.join(langMap.keySet(), "\n");

			message.getChannel().sendMessage("**Supported Languages:**\n" + supportedLangsString).queue();
		} else {
			
			String content = message.getContentDisplay();
			String[] compilerArgs = content.replaceAll("```", "").split("\n| ");
			String language = compilerArgs[1];

			if (langMap.containsKey(language)) 
			{
				String compiler = langMap.get(language);
				String code = content//
						.substring(message.getContentDisplay()//
						.indexOf("```" + language));
				
				code = code.replaceAll("```","");
				
				final String finalCode = code.substring(language.length());
				
				MessageEmbed compilationWaitingMessage = new EmbedBuilder().setTitle("Your code is being compiled...")
						.setDescription("Please wait a moment")//
						.setColor(Color.RED)
						.setThumbnail(EMBED_THUMBNAIL_URL)
						.addField("Compiler", compiler, true)//
						.build();
				
				message.getChannel().sendMessage(compilationWaitingMessage).queue(sentMessage -> {
					try {
						String payload = new JSONObject().put("code", finalCode).put("compiler", compiler).toString();
						URL requestURL = new URL(WANDBOX_COMPILE_URL);
						
						HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
						con.setRequestMethod("POST");
						con.setRequestProperty("Content-Type", "application/json; utf-8");
						con.setDoOutput(true);
						con.setRequestProperty("Accept", "application/json");
						
						try (OutputStream os = con.getOutputStream()) {
							byte[] input = payload.getBytes("utf-8");
							os.write(input, 0, input.length);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						try (OutputStream os = con.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

							String response = in.lines().collect(Collectors.joining());
							
							String[] inputArray = response.split("data:");
							String resultText = String.join("\n", inputArray);
							resultText.replaceAll("StdOut:|^[a-zA-Z0-9]{2}$", "");
							
							MessageEmbed compilationCompleteMessage = new EmbedBuilder()//
									.setColor(Color.GREEN)//
									.setTitle("Compilation Results")//
									.setDescription("```" + resultText + "```")//
									.build();
							
							sentMessage.editMessage(compilationCompleteMessage).queue();
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						con.disconnect();
					} catch (IOException e1) {
						e1.printStackTrace();
					}


				});

			} else {
				message.getChannel().sendMessage("That language could not be found!").queue();
			}
		}

	}

	private Map<String, String> getSupportedLangs() {
		Map<String, String> supportedLangsMap = new HashMap<String, String>();

		supportedLangsMap.put("js", "nodejs-head");
		supportedLangsMap.put("javascript", "nodejs-head");
		supportedLangsMap.put("c++", "gcc-head");
		supportedLangsMap.put("cpp", "gcc-head");
		supportedLangsMap.put("py", "cpython-head");
		supportedLangsMap.put("python", "cpython-head");
		supportedLangsMap.put("cs", "mono-head");
		supportedLangsMap.put("csharp", "mono-head");
		supportedLangsMap.put("c", "gcc-head-c");
		supportedLangsMap.put("java", "openjdk-head");
		supportedLangsMap.put("ts", "typescript-3.9.5");
		supportedLangsMap.put("typescript", "typescript-3.9.5");
		supportedLangsMap.put("bash", "bash");
		supportedLangsMap.put("php", "php-head");
		supportedLangsMap.put("pas", "fpc-head");
		supportedLangsMap.put("delphi", "delphi-mode");
		supportedLangsMap.put("rust", "rust-head");
		supportedLangsMap.put("rs", "rust-head");
		supportedLangsMap.put("lua", "lua-5.4.0");

		return supportedLangsMap;
	}

}
