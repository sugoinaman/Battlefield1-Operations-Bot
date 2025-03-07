import commands.Hello;
import commands.MyCommands;
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

        JDA jda = JDABuilder.createDefault("MTM0MTM0NjI1MzgzMjk3ODUyNQ.GwkSuk.NU5o2VYmGkoVMni8NfDQPHN290s1XVBvN0S2iI")
                //.enableIntents(GatewayIntent.MESSAGE_CONTENT)
//                .addEventListeners(new MapHistory())
                .addEventListeners(new MyCommands())
                .build();

        //Map History Implementation triggers on bot start
        // MapHistoryImpl mapHistory = new MapHistoryImpl();
        // commented this since i am calling it in another class which will be called here.

    }

}

