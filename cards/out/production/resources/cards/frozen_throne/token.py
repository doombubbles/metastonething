import json
import shutil

file = open("token.txt", "r")
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
        if "Tribe: " in line:
                line = line.replace("Tribe: ", "").replace("\n", "")
                race = line.upper()
        if "Class: " in line:
                line = line.replace("Class: ", "").replace("\n", "")
                if line != "Neutral":
                        heroClass = line.upper()
                else: heroClass = "ANY"
				
dictionary = {
	"name": a[0].replace("\n", ""),
	"baseManaCost": baseManaCost,
	"type": "MINION",
	"baseAttack": baseAttack,
	"baseHp": baseHp,
	"heroClass": heroClass,
	"rarity": "FREE",
	"race": race,
	"description": "",
	"collectible": True,
	"set": "FROZEN_THRONE",
	"fileFormatVersion": 1
} 
        

filename = "token_" + dictionary["name"].lower().replace(",", "").replace("'", "").replace(" ", "_") + ".json"

file = open(filename, "w")

json.dump(dictionary, file, indent=4)

file.close()
if heroClass != "ANY":
        src = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\frozen_throne\\" + filename
        dest = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\frozen_throne\\" + heroClass[0] + heroClass[1:].lower() + "\\" + filename

        shutil.move(str(src), str(dest))
else:
        src = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\frozen_throne\\" + filename
        dest = "C:\\Users\\James Gale\\Documents\\metastone\\cards\\frozen_throne\\neutral\\" + filename

        shutil.move(str(src), str(dest)) 

