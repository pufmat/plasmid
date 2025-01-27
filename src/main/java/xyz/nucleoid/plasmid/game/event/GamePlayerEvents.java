package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collection;

/**
 * Events relating to players being added and removed from a {@link GameSpace} or {@link GameActivity}.
 */
public final class GamePlayerEvents {
    /**
     * Called when a {@link ServerPlayerEntity} is added to a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayerEntity} should be tracked by a {@link GameActivity}, and is NOT limited to a player
     * specifically joining.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayerEntity} intentionally joins this {@link GameSpace}</li>
     * <li>A new {@link GameActivity} is created, and all players are transferred</li>
     * <p>
     * This event will always be fired after {@link GameActivityEvents#CREATE} and before {@link GameActivityEvents#ENABLE}.
     * <p>
     * This event is invoked after the player has been added to the {@link GameSpace#getPlayers() game player set}.
     *
     * @see GamePlayerEvents#JOIN
     */
    public static final StimulusEvent<Add> ADD = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} is removed from a {@link GameActivity}. This involves all cases where a
     * {@link ServerPlayerEntity} should be no longer be tracked by a {@link GameActivity}, and is NOT limited to a
     * player specifically leaving the game.
     * <p>
     * This will be fired when:
     * <li>A {@link ServerPlayerEntity} intentionally leaves this {@link GameSpace}</li>
     * <li>A {@link GameSpace} is closed or {@link GameActivity} replaced</li>
     * <p>
     * This event will always be fired before {@link GameActivityEvents#DESTROY} and after {@link GameActivityEvents#DISABLE}.
     * <p>
     * This event is invoked before the player is removed from the {@link GameSpace#getPlayers() game player set}.
     *
     * @see GamePlayerEvents#LEAVE
     */
    public static final StimulusEvent<Remove> REMOVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} intentionally joins a {@link GameSpace}.
     *
     * @see GamePlayerEvents#ADD
     * @see GamePlayerEvents#OFFER
     */
    public static final StimulusEvent<Add> JOIN = StimulusEvent.create(Add.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onAddPlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a {@link ServerPlayerEntity} intentionally leaves a {@link GameSpace} or leaves the server entirely.
     *
     * @see GamePlayerEvents#REMOVE
     */
    public static final StimulusEvent<Remove> LEAVE = StimulusEvent.create(Remove.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRemovePlayer(player);
            }
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
        }
    });

    /**
     * Called when a group of players try to join this game. This should be used to reject multiple players as a group,
     * such as when a party tries to join but has too many players to fit into the game.
     * <p>
     * This is called before {@link GamePlayerEvents#OFFER} which handles specifically bringing a player into the game.
     *
     * @see GamePlayerEvents#OFFER
     */
    public static final StimulusEvent<ScreenJoins> SCREEN_JOINS = StimulusEvent.create(ScreenJoins.class, ctx -> players -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.screenJoins(players);
                if (result.isError()) {
                    return result;
                }
            }
            return GameResult.ok();
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return GameResult.error(GameTexts.Join.unexpectedError());
        }
    });

    /**
     * Called when a single {@link ServerPlayerEntity} tries to join this game. This event is responsible for bringing
     * the player into the {@link GameSpace} world in the correct location.
     * <p>
     * Games must respond to this event in order for a player to be able to join by returning either
     * {@link PlayerOffer#accept(ServerWorld, Vec3d)} or {@link PlayerOffer#reject(Text)}.
     *
     * @see PlayerOffer
     * @see PlayerOfferResult
     * @see GamePlayerEvents#SCREEN_JOINS
     * @see GamePlayerEvents#JOIN
     */
    public static final StimulusEvent<Offer> OFFER = StimulusEvent.create(Offer.class, ctx -> offer -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onOfferPlayer(offer);
                if (result.isTerminal()) {
                    return result;
                }
            }
            return offer.pass();
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return offer.reject(GameTexts.Join.unexpectedError());
        }
    });

    /**
     * Called when display name of {@link ServerPlayerEntity} is created.
     * Can be used to manipulate it in game.
     */
    public static final StimulusEvent<Name> DISPLAY_NAME = StimulusEvent.create(Name.class, ctx -> (player, current, vanillaText) -> {
        try {
            for (var listener : ctx.getListeners()) {
                current = listener.onDisplayNameCreation(player, current, vanillaText);
            }
            return current;
        } catch (Throwable throwable) {
            ctx.handleException(throwable);
            return vanillaText;
        }
    });

    public interface Add {
        void onAddPlayer(ServerPlayerEntity player);
    }

    public interface Remove {
        void onRemovePlayer(ServerPlayerEntity player);
    }

    public interface ScreenJoins {
        GameResult screenJoins(Collection<ServerPlayerEntity> players);
    }

    public interface Offer {
        PlayerOfferResult onOfferPlayer(PlayerOffer offer);
    }

    public interface Name {
        Text onDisplayNameCreation(ServerPlayerEntity player, Text currentText, Text vanillaText);
    }
}
