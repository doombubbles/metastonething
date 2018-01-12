package net.demilich.metastone.gui.deckbuilder.importer;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DeckCodeImporter implements IDeckImporter {

    @Override
    public Deck importFrom(String deckCode){
        HeroClass heroClass = null;
        List<Card> cards = new ArrayList<>();
        int line = 0;
        try {
            URL url = new URL("https://deck.codes/" + deckCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                line++;
                if (inputLine.contains("<img src=\"/_/img/hero/")) {
                    String hero = inputLine.substring(inputLine.indexOf("<img src=\"/_/img/hero/") + 22, inputLine.indexOf(".")).toUpperCase();
                    heroClass = HeroClass.valueOf(hero);
                }
                if (inputLine.contains("<span class=\"hs-tile-info-middle mdc-list-item__text\">")) {
                    String nextLine = in.readLine();
                    if (nextLine.contains("???")) {
                        continue;
                    }
                    String cardName = nextLine.substring(nextLine.indexOf("<span>") + 6, nextLine.indexOf("</span>")).replace("&#39;", "'");

                    in.readLine();
                    in.readLine();
                    in.readLine();
                    String amountLine = in.readLine();
                    int qty = amountLine.contains("2") ? 2 : 1;
                    for (int i = 1; i <= qty; i++) {
                        if (CardCatalogue.getCardByName(cardName) != null) {
                            cards.add(CardCatalogue.getCardByName(cardName));
                        }
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (heroClass == null) {
            return null;
        }
        Deck deck = new Deck(heroClass, false);
        deck.setName("New Deck");
        for (Card card : cards) {
            deck.getCards().add(card);
        }

        return deck;
    }


}
