package com.hollowdread.command;

import com.hollowdread.HollowDread;
import com.hollowdread.sanity.Sanity;
import com.hollowdread.scare.DreadDirector;
import com.hollowdread.scare.DreadEvent;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Locale;

/**
 * /dread sanity get <jogador>
 * /dread sanity set <jogador> <0-100>
 * /dread trigger <jogador> <evento>
 */
@Mod.EventBusSubscriber(modid = HollowDread.MODID)
public final class DreadCommands {
    private DreadCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("dread")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("sanity")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            float v = Sanity.get(p);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    p.getName().getString() + ": " + String.format(Locale.ROOT, "%.1f", v) + "% de sanidade"), false);
                                            return (int) v;
                                        })))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(ctx -> {
                                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                                    float v = FloatArgumentType.getFloat(ctx, "value");
                                                    Sanity.set(p, v);
                                                    Sanity.sync(p);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "Sanidade de " + p.getName().getString() + " = " + v), true);
                                                    return 1;
                                                })))))
                .then(Commands.literal("trigger")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("event", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(DreadEvent.values()).map(x -> x.name().toLowerCase(Locale.ROOT)), b))
                                        .executes(ctx -> trigger(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "event")))))));
    }

    private static int trigger(CommandSourceStack src, ServerPlayer p, String name) {
        DreadEvent ev;
        try {
            ev = DreadEvent.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            src.sendFailure(Component.literal("Evento desconhecido: " + name));
            return 0;
        }
        boolean ok = DreadDirector.run(p, ev);
        if (ok) src.sendSuccess(() -> Component.literal("Evento " + ev.name() + " disparado."), true);
        else src.sendFailure(Component.literal("Nao foi possivel disparar " + ev.name() + " agora (sem espaco/alvo/config desligada)."));
        return ok ? 1 : 0;
    }
}
