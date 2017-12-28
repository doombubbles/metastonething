package net.demilich.metastone.gui.deckbuilder.importer;

public class ImporterFactory {

    public IDeckImporter createDeckImporter(String url)
    {
        if(url == null)
            return null;

        if(url.contains("hearthpwn.com")) {
            return new HearthPwnImporter();
        } else if(url.contains("tempostorm.com")) {
            return new TempostormImporter();
        } else if (!url.equals("")){
            return new DeckCodeImporter();
        }
        return null;
    }
}
