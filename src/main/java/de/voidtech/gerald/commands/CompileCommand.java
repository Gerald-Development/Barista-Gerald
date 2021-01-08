package main.java.de.voidtech.gerald.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CompileCommand extends AbstractCommand {

	@Override
	public void execute(Message message, List<String> args) {
		
		Map<String, String> langMap = new HashMap<String, String>();
		langMap.put("js", "nodejs-head");
		langMap.put("javascript", "nodejs-head");
		langMap.put("c++", "gcc-head");
		langMap.put("cpp", "gcc-head");
		langMap.put("py", "cpython-head");
		langMap.put("python", "cpython-head");
		langMap.put("cs", "mono-head");
		langMap.put("csharp", "mono-head");
		langMap.put("c", "gcc-head-c");
		langMap.put("java", "openjdk-head");
		langMap.put("ts", "typescript-3.9.5");
		langMap.put("typescript", "typescript-3.9.5");
		langMap.put("bash", "bash");
		langMap.put("php", "php-head");
		langMap.put("pas", "fpc-head");
		langMap.put("delphi", "delphi-mode");
		langMap.put("rust", "rust-head");
		langMap.put("rs", "rust-head");
		langMap.put("lua", "lua-5.4.0");
		
		if (args.get(0).equals("languages")) {
			
			String keysList = "";
			
			for (String key : langMap.keySet()) {
				keysList = keysList + key + "\n";
			}
			
			message.getChannel().sendMessage("**Supported Languages:**\n" + keysList).queue();
		} else {
			String content = message.getContentDisplay();
			content = content.replaceAll("```", "");
			String[] compilerArgs = content.split("\n| ");
			String language = compilerArgs[1];
			
			if (langMap.containsKey(language)) {
				String compiler = langMap.get(language);
				String code = message.getContentDisplay().substring(message.getContentDisplay().indexOf("```" + language));
				code = code.replaceAll("```","");
				String finalCode = code.substring(language.length());
				MessageEmbed compilationWaitingMessage = new EmbedBuilder()
						.setTitle("Your code is being compiled...")
						.setDescription("Please wait a moment")
						.addField("Compiler", compiler, true)
						.setColor(Color.RED)
						.setThumbnail("https://cdn.discordapp.com/attachments/772921190280724512/790283222156050462/7398c7bd6be35540-the-gallery-for-processing-gif-transparent-web-page-processing.gif")
						.build();
				message.getChannel().sendMessage(compilationWaitingMessage).queue(sentMessage -> {
					
					String payload = new JSONObject()
			                  .put("code", finalCode)
			                  .put("compiler", compiler)
			                  .toString();
					
					 URL requestURL = null;
					try {
						requestURL = new URL("https://wandbox.org/compile");
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				        HttpURLConnection con = null;
						try {
							con = (HttpURLConnection) requestURL.openConnection();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        try {
							con.setRequestMethod("POST");
						} catch (ProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        con.setRequestProperty("Content-Type", "application/json; utf-8");
				        con.setDoOutput(true);
				        con.setRequestProperty("Accept", "application/json");
				        
				        try(OutputStream os = con.getOutputStream()) {
				            byte[] input = payload.getBytes("utf-8");
				            os.write(input, 0, input.length);			
				        } catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        
				        try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				        {
				            String inputLine;
				            StringBuffer inputContent = new StringBuffer();
				            while ((inputLine = in.readLine()) != null) {
				                inputContent.append(inputLine);
				            }
				            in.close();
				            con.disconnect();
				            
				            String[] inputArray = inputContent.toString().split("data:");
				            String resultText = String.join("\n", inputArray);
				            resultText.replaceAll("\\StdOut:|^[a-zA-Z0-9]{2}$\\", "");
				            
				            
				            
				            MessageEmbed compilationCompleteMessage = new EmbedBuilder()
				            		.setColor(Color.GREEN)
				            		.setTitle("Compilation Results")
				            		.setDescription("```" + resultText + "```")
				            		.build();				            
				            sentMessage.editMessage(compilationCompleteMessage).queue();
				            
				        } catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        
				        con.disconnect();

				});
				
				} else {
				message.getChannel().sendMessage("That language could not be found!").queue();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to compile code via wandbox";
	}

	@Override
	public String getUsage() {
		return "compile \\`\\`\\`[name of language] [code] \\`\\`\\` \nOR compile languages";
	}

}
