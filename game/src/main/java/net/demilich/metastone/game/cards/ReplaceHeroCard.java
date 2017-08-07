package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayReplaceHeroCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.ReplaceHeroCardDesc;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;

public class ReplaceHeroCard extends Card {

	public String hero;
	public BattlecryAction battlecry;
	public int armor;
	
	public ReplaceHeroCard(ReplaceHeroCardDesc desc) {
		super(desc);
		hero = desc.hero;
		armor = desc.armor;
		BattlecryDesc battlecrydesc = desc.battlecry;
		if (battlecrydesc != null) {
			BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecrydesc.spell, battlecrydesc.getTargetSelection());
			if (battlecrydesc.condition != null) {
				battlecryAction.setCondition(battlecrydesc.condition.create());
			}

			battlecry = battlecryAction;
		}
		else battlecry = null;
	}

	@Override
	public PlayCardAction play() {
		return new PlayReplaceHeroCardAction(getCardReference(), battlecry);
	}

}
