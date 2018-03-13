import json
import shutil

file = open("stuff.txt", "r")
a = file.readlines()
file.close()

race = "NONE"

for line in a:
        if "Mana Cost: " in line:
                line = line.replace("Mana Cost: ", "").replace("\n", "")
                baseManaCost = int(line)
        if "Attack: " in line:
                line = line.replace("Attack: ", "").replace("\n", "")
                baseAttack = int(line)
        if "Health: " in line:
                line = line.replace("Health: ", "").replace("\n", "")
                baseHp = int(line)
        if "Durability: " in line:
                line = line.replace("Durability: ", "").replace("\n", "")
                durability = int(line)
        if "Type: " in line:
                line = line.replace("Type: ", "").replace("\n", "")
                _type = line.upper()
        if "Tribe: " in line:
                line = line.replace("Tribe: ", "").replace("\n", "")
                race = line.upper()
        if "Rarity: " in line:
                line = line.replace("Rarity: ", "").replace("\n", "")
                rarity = line.upper()
        if "Class: " in line:
                line = line.replace("Class: ", "").replace("\n", "")
                if line != "Neutral":
                        heroClass = line.upper()
                else: heroClass = "ANY"
        if "Text: " in line:
                line = line.replace("Text: ", "").replace("\n", "")
                description = line
if _type == "MINION":
        if "attlecry" not in description:
                if "eathrattle" not in description:
                        dictionary = {
                                "name": a[0].replace("\n", ""),
                                "baseManaCost": baseManaCost,
                                "type": _type,
                                "baseAttack": baseAttack,
                                "baseHp": baseHp,
                                "heroClass": heroClass,
                                "rarity": rarity,
                                "race": race,
                                "description": description,
                                "collectible": True,
                                "set": "WITCHWOOD",
                                "fileFormatVersion": 1
                        }
                else:
                      dictionary = {
                                "name": a[0].replace("\n", ""),
                                "baseManaCost": baseManaCost,
                                "type": _type,
                                "baseAttack": baseAttack,
                                "baseHp": baseHp,
                                "heroClass": heroClass,
                                "rarity": rarity,
                                "race": race,
                                "description": description,
                                "deathrattle": {
                                        "class": "",
                                },
                                "attributes": {
                                        "DEATHRATTLES": True
                                },
                                "collectible": True,
                                "set": "WITCHWOOD",
                                "fileFormatVersion": 1
                        }  
        else:
               dictionary = {
                        "name": a[0].replace("\n", ""),
                        "baseManaCost": baseManaCost,
                        "type": _type,
                        "baseAttack": baseAttack,
                        "baseHp": baseHp,
                        "heroClass": heroClass,
                        "rarity": rarity,
                        "race": race,
                        "description": description,
                        "battlecry": {
                                "targetSelection": "NONE",
                                "spell": {}
                        },
                        "attributes": {
                                "BATTLECRY": True
                        },
                        "collectible": True,
                        "set": "WITCHWOOD",
                        "fileFormatVersion": 1
                } 
elif _type == "WEAPON":
        dictionary = {
                "name": a[0].replace("\n", ""),
                "baseManaCost": baseManaCost,
                "type": _type,
                "damage": baseAttack,
                "durability": durability,
                "heroClass": heroClass,
                "rarity": rarity,
                "description": description,
                "collectible": True,
                "set": "WITCHWOOD",
                "fileFormatVersion": 1
        }
elif _type == "SPELL":
        dictionary = {
                "name": a[0].replace("\n", ""),
                "baseManaCost": baseManaCost,
                "type": _type,
                "heroClass": heroClass,
                "rarity": rarity,
                "description": description,
                "targetSelection": "",
                "spell": {
                        "class": ""
                },
                "collectible": True,
                "set": "WITCHWOOD",
                "fileFormatVersion": 1
        }
else:
        dictionary = {
                "name": a[0].replace("\n", ""),
                "baseManaCost": baseManaCost,
                "type": _type,
                "heroClass": heroClass,
                "rarity": rarity,
                "description": description,
                "collectible": True,
                "set": "WITCHWOOD",
                "fileFormatVersion": 1
        }

    
        
        

filename = dictionary["type"].lower() + "_" + dictionary["name"].lower().replace(",", "").replace("'", "").replace(" ", "_") + ".json"

file = open(filename, "w")

json.dump(dictionary, file, indent=4)

file.close()
if heroClass != "ANY":
        src = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\witchwood\\" + filename
        dest = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\witchwood\\" + heroClass[0] + heroClass[1:].lower() + "\\" + filename

        shutil.move(str(src), str(dest))
else:
        src = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\witchwood\\" + filename
        dest = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\witchwood\\neutral\\" + filename

        shutil.move(str(src), str(dest)) 

