package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeftmostReductionSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardCostModifierDesc costModifierDesc = new CardCostModifierDesc(CardCostModifierDesc.build(CardCostModifier.class));
        costModifierDesc = costModifierDesc.addArg(CardCostModifierArg.OPERATION, AlgebraicOperation.SUBTRACT);
        costModifierDesc = costModifierDesc.addArg(CardCostModifierArg.VALUE, 2);
        int id = player.getHand().get(0).getId();
        //System.out.println(player.getHand().get(0).getName());
        List<Integer> cardIds = new ArrayList<Integer>();
        cardIds.add(id);
        costModifierDesc = costModifierDesc.addArg(CardCostModifierArg.CARD_IDS,  cardIds);
        context.getLogic().addManaModifier(player, costModifierDesc.create(), player.getHand().get(0));
    }
}
