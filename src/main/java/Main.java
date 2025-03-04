import commands.Hello;
import config.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;


public class Main {


    public static void main(String[] args) {

        JDA jda = JDABuilder.createDefault(Configuration.getDiscordToken())
                //.enableIntents(GatewayIntent.MESSAGE_CONTENT)
//                .addEventListeners(new MapHistory())
                .addEventListeners(new Hello())
                .build();

        //Map History Implementation triggers on bot start
        // MapHistoryImpl mapHistory = new MapHistoryImpl();
        // commented this since i am calling it in another class which will be called here.


        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("hello", "says hello")
        );
        ArrayList<OptionData> listOfDifferentRotations = getOptionDataOfDifferentRotations();
        commands.addCommands(
                Commands.slash("SetCustomRotation", "Sets a custom rotation from a FIXED list of rotations")
                        .addOptions(new OptionData(boolean,"flag","true to turn it on, false to turn off")
                        .addOptions(listOfDifferentRotations)
                        .addOptions(listOfDifferentRotations)
                        .addOptions(listOfDifferentRotations)
                        .addOptions(listOfDifferentRotations)
                        .addOptions(listOfDifferentRotations)
                        .setContexts(InteractionContextType.GUILD)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
        );
        commands.queue();
    }

    @NotNull
    private static ArrayList<OptionData> getOptionDataOfDifferentRotations() {
        OptionData Rotation1 = new OptionData(OptionType.STRING, "fao", "Fao,Sez,Sinai,Soissons,Rupture");
        OptionData Rotation2 = new OptionData(OptionType.STRING, "Giants", "Giants, Grappa, Empire, Somme, Cape,Achi,Zee");
        OptionData Rotation3 = new OptionData(OptionType.STRING, "verdun", "Verdun, vaux,cape,achi, zee");
        OptionData Rotation4 = new OptionData(OptionType.STRING, "volga", "volga,tsar,scar,amiens,ball,argonne");
        OptionData Rotation5 = new OptionData(OptionType.STRING, "prise", "prise,zee");
        return new ArrayList<>(new ArrayList<>(Arrays.asList(Rotation1, Rotation2, Rotation3, Rotation4, Rotation5)));
    }
}

