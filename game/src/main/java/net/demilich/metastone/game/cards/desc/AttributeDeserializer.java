package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.demilich.metastone.game.Attribute;

public class AttributeDeserializer implements JsonDeserializer<Map<Attribute, Object>> {

	@Override
	public Map<Attribute, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		Map<Attribute, Object> map = new EnumMap<Attribute, Object>(Attribute.class);
		JsonObject jsonData = json.getAsJsonObject();
		parseAttribute(Attribute.HP, jsonData, map, ParseValueType.INTEGER);//TODO Remove from Heroes
		parseAttribute(Attribute.MAX_HP, jsonData, map, ParseValueType.INTEGER);//TODO Remove from Heroes
		
		parseAttribute(Attribute.ATTACK_EQUALS_HP, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.BATTLECRY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.BOTH_CHOOSE_ONE_OPTIONS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK_HERO_ON_SUMMON, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK_HEROES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CHARGE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.COMBO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DEATHRATTLES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DIVINE_SHIELD, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DOUBLE_BATTLECRIES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DOUBLE_DEATHRATTLES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.ENRAGABLE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.HERO_POWER_CAN_TARGET_MINIONS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.HERO_POWER_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWER_USAGES, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.IMMUNE_HERO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.INVERT_HEALING, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.MEGA_WINDFURY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.OPPONENT_SPELL_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.OVERLOAD, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SPELL_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SPELL_DAMAGE_MULTIPLIER, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.STEALTH, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.UNTARGETABLE_BY_SPELLS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS, jsonData, map, ParseValueType.BOOLEAN);//TODO Remove from Spellstopper
		parseAttribute(Attribute.TAUNT, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.WINDFURY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.COSTS_HEALTH, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DOUBLE_RACE_SPELL_DAMAGE, jsonData, map, ParseValueType.RACE);
		parseAttribute(Attribute.ENRAGED_ATTACK, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.LIFESTEAL, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.AURA, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.POISONOUS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.FREEZE_POWER, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.KRYPTONITE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.NO_DURABILITY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.ONE_TURN, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CUSTOM_7, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CUSTOM_8, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CUSTOM_6, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.DOUBLE_END_TURN, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.STARTED_IN_DECK, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.INSTANT_TRAPS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.QUEST, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.EXTRA_TURNS, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ENCHANTMENT_ATTACK, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ENCHANTMENT_HP, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ELUSIVE_HERO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.COUNTDOWN, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.NO_FILTER, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.ACTUAL_MANA_COST, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.CARD_TYPE_COSTS_HEALTH, jsonData, map, ParseValueType.CARD_TYPE);
		parseAttribute(Attribute.PHOENIX, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.RUSH, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CHOOSE_DISCARD, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.FRIENDLY_TARGET_ENEMIES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.MINIONS_COUNT_AS, jsonData, map, ParseValueType.STRING_ARRAY);
		parseAttribute(Attribute.ECHO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.SPELLS_DAMAGE_MULT, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SPELLS_HEAL_MULT, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWER_DAMAGE_MULT, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWER_HEAL_MULT, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ALL_OPTIONS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.REPLACED_WEAPON_SLOT, jsonData, map, ParseValueType.BOOLEAN);
		return map;
	}

	private void parseAttribute(Attribute attribute, JsonObject jsonData, Map<Attribute, Object> map, ParseValueType valueType) {
		String argName = attribute.toString();
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		Boolean bool = value instanceof Boolean ? (Boolean) value : null;
		if (bool != null && bool == true) {
			value = 1;
		}
		map.put(attribute, value);
	}

}
