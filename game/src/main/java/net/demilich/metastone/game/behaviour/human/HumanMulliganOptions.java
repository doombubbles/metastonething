package net.demilich.metastone.game.behaviour.human;

import java.io.Serializable;
import java.util.List;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

public class HumanMulliganOptions implements Serializable {

	private Player player;
	private Player opponent;
	private final HumanBehaviour behaviour;
	private final List<Card> offeredCards;

	public HumanMulliganOptions(Player player, HumanBehaviour behaviour, List<Card> offeredCards, Player opponent) {
		this.player = player;
		this.behaviour = behaviour;
		this.offeredCards = offeredCards;
		this.opponent = opponent;
	}

	public HumanBehaviour getBehaviour() {
		return behaviour;
	}

	public List<Card> getOfferedCards() {
		return offeredCards;
	}

	public Player getPlayer() {
		return player;
	}

	public Player getOpponent() {
		return opponent;
	}

	public HumanMulliganOptions switchPlayers() {
		Player temp = player;
		player = opponent;
		opponent = temp;
		return this;
	}

}
