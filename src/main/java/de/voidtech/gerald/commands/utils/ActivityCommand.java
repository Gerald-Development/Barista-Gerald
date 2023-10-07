package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.GlobalConfig;
import main.java.de.voidtech.gerald.service.GeraldConfigService;
import main.java.de.voidtech.gerald.service.GlobalConfigService;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ActivityCommand extends AbstractCommand {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private GlobalConfigService globalConfService;

    @Autowired
    private GeraldConfigService config;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (!config.getMasters().contains(context.getMember().getId())) return;

        if (StringUtils.join(args.toArray(), " ").length() > 128)
            context.reply("Too many characters! The activity can only be 128 letters");
        else {
            ActivityWrapper activityWrapperOpt = ActivityWrapper
                    .getActivityWrapperOpt(StringUtils.join(args.toArray(), " "));

            if (activityWrapperOpt != null) {
                String statusMessage = StringUtils.join(args.toArray(), " ").substring(activityWrapperOpt.toString().length());
                context.getJDA().getPresence()
                        .setActivity(EntityBuilder.createActivity(statusMessage, GlobalConstants.STREAM_URL, activityWrapperOpt.getActivityType()));
                updatePersistentActivity(activityWrapperOpt.getActivityType(), statusMessage);
                context.reply("**Set status to:** " + activityWrapperOpt.humanReadable + statusMessage);
            } else {
                context.reply("Please provide a valid activity: `playing, watching, listening to, streaming, competing in`");
            }
        }
    }

    @Override
    public String getDescription() {
        return "sets the activity of the Barista";
    }

    @Override
    public String getUsage() {
        return "activity {activity} {message}";
    }

    private void updatePersistentActivity(ActivityType activityType, String status) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();

            GlobalConfig globalConf = globalConfService.getGlobalConfig();
            globalConf.setActivity(activityType);
            globalConf.setStatus(status);

            session.saveOrUpdate(globalConf);
            session.getTransaction().commit();
        }
    }

    @Override
    public String getName() {
        return "activity";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.UTILS;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"setactivity", "status", "setstatus"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return false;
    }

    /**
     * Do not, under no circumstances, change the order of this enum
     * Except if you know what you're doing
     */
    private enum ActivityWrapper {
        DEFAULT("playing"),
        STREAMING("streaming"),
        LISTENING("listening to"),
        WATCHING("watching"),
        CUSTOM("custom"),
        COMPETING("competing in");

        private final String humanReadable;

        ActivityWrapper(String humanReadable) {
            this.humanReadable = humanReadable;
        }

        public static ActivityWrapper getActivityWrapperOpt(String activity) {
            if (activity.startsWith("listening to")) return ActivityWrapper.LISTENING;
            else if (activity.startsWith("watching")) return ActivityWrapper.WATCHING;
            else if (activity.startsWith("streaming")) return ActivityWrapper.STREAMING;
            else if (activity.startsWith("playing")) return ActivityWrapper.DEFAULT;
            else if (activity.startsWith("competing in")) return ActivityWrapper.COMPETING;
            return null;
        }

        @Override
        public String toString() {
            return this.humanReadable;
        }

        public ActivityType getActivityType() {
            return ActivityType.values()[this.ordinal()];
        }
    }

}
