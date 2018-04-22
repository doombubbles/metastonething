package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepeatBattlecriesSpell extends Spell
{
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardList playedCards = new CardList();
        Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
        for (String card : cardIds.keySet()) {
            for (int turn : cardIds.get(card).keySet()) {
                for (int i = 0; i < cardIds.get(card).get(turn); i++) {
                    playedCards.add(context.getCardById(card));
                }
            }
        }
        playedCards.shuffle();


        List<BattlecryDesc> battlecries = new ArrayList<>();
        double delay = 0;
        for (Card playedCard : playedCards) {
            if (playedCard.hasAttribute(Attribute.BATTLECRY) && !playedCard.getCardId().equalsIgnoreCase("minion_shudderwock")) {
                BattlecryDesc battlecry = playedCard.getBattlecry();
                battlecries.add(battlecry);
                //context.fireGameEvent(new CardRevealedEvent(context, player.getId(), playedCard, delay += 1.2));
            }
        }

        context.getOpponent(player).setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, true);
        player.setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, true);
        for (BattlecryDesc battlecryDesc : battlecries) {
            BattlecryAction battlecry = BattlecryAction.createBattlecry(battlecryDesc.spell, battlecryDesc.getTargetSelection());
            GameAction battlecryAction = null;
            battlecry.setSource(source.getReference());
            if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
                List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
                if (validTargets.isEmpty()) {
                    continue;
                }
                List<GameAction> battlecryActions = new ArrayList<>();
                for (Entity validTarget : validTargets) {
                    GameAction targetedBattlecry = battlecry.clone();
                    targetedBattlecry.setTarget(validTarget);
                    battlecryActions.add(targetedBattlecry);
                }
                battlecryAction = battlecryActions.get(context.getLogic().random(battlecryActions.size()));
            } else {
                battlecryAction = battlecry;
            }
            context.getLogic().performGameAction(player.getId(), battlecryAction);
        }
        context.getOpponent(player).setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, false);
        player.setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, false);
    }
}
