package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.awt.*;
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

@Command
public class CompileCommand extends AbstractCommand {

	private static final String WANDBOX_COMPILE_URL = "https://wandbox.org/api/compile.json";
	private static final String EMBED_THUMBNAIL_URL = "https://cdn.discordapp.com/attachments/727233195380310016/823533201279418399/808411850555261028.gif";
	private static final char ESCAPE_CHAR = ((char)8204);
	private Map<String, String> langMap = getSupportedLangs();
	
	private String getWandboxResponse(String payload) {
		try {
			
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

				return in.lines().collect(Collectors.joining());
							
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			con.disconnect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return "";
	}
	
	private void sendResponse(String finalCode, String compiler, Message sentMessage) {
		String payload = new JSONObject().put("code", finalCode).put("compiler", compiler).put("save", true).toString();
		String response = getWandboxResponse(payload);
		
		JSONObject compilerResponse = new JSONObject(response);
		String responseText = "";
		Color color = null;
		String titleMessage = "";
		String statusCode = "";
		String permLink = compilerResponse.getString("url");
		
		if (compilerResponse.has("program_error")) {
			responseText = compilerResponse.get("program_error").toString();
			color = Color.RED;
			titleMessage = "Compilation Error!";
			statusCode = compilerResponse.getString("status").toString();
		} else {
			if (compilerResponse.has("program_output")) {
				responseText = compilerResponse.get("program_output").toString();	
			} else {
				responseText = "No return";
			}
			color = Color.GREEN;
			titleMessage = "Compilation Successful!";
			statusCode = "0";
		}
		
		String output = responseText.length() <= 500 ? responseText : responseText.substring(0, 500);
		MessageEmbed compilationCompleteMessage = new EmbedBuilder()//
				.setColor(color)//
				.setTitle(titleMessage)//
				.addField("Program Output", "```" + output + "```", false)//
				.addField("Status", "```\nCompiler responded with status code " + statusCode + "```", false)
				.addField("Permalink", "**[Wandbox URL](" + permLink + ")**", false)
				.build();
		
		sentMessage.editMessageEmbeds(compilationCompleteMessage).queue();
	}
	
	private void runCompilerSystem(Message message, List<String> args) {
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
					.setColor(Color.ORANGE)
					.setThumbnail(EMBED_THUMBNAIL_URL)
					.addField("Compiler", compiler, true)//
					.build();
			
			message.replyEmbeds(compilationWaitingMessage).mentionRepliedUser(false).queue(sentMessage -> {
				sendResponse(finalCode, compiler, sentMessage);					

			});

		} else {
			message.reply("That language could not be found!").mentionRepliedUser(false).queue();
		}
	}
	//TODO (from: Franziska): This should probably not be available in SlashCommands?
	@Override
	public void executeInternal(CommandContext context, List<String> args) {

		if (args.size() >= 0 && args.get(0).equals("languages")) {
			String supportedLangsString = StringUtils.join(langMap.keySet(), "\n");
			context.reply("**Supported Languages:**\n" + supportedLangsString);
		
		} else {
			runCompilerSystem(context.getMessage(), args);
		}
		context.reply("This command is not available due to SlashCommand the rework. Please contact a developer");
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
	
	@Override
	public String getDescription() {
		return "Allows you to compile code via the magic of wandbox! (Technical support from Bongo Cat)";
	}

	@Override
	public String getUsage() {
		return "compile " + ESCAPE_CHAR + "`" + ESCAPE_CHAR + "`" + ESCAPE_CHAR + "`[name of language]\n[code]\n" + ESCAPE_CHAR + "`" + ESCAPE_CHAR + "`" + ESCAPE_CHAR + "` \nOR compile languages";
	}

	@Override
	public String getName() {
		return "compile";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"execute", "run"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
}
