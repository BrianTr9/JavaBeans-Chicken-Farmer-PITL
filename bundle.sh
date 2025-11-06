#!/bin/bash

cd $(dirname "$0")

if [ -f submission.zip ]; then
  old=$(date +%s)
  echo "Back up old submission as submission-${old}\n"
  mv submission.zip submission-${old}.zip
fi


zip -r submission.zip ai/README.txt
zip -r submission.zip ai/*
zip -r submission.zip src/builder/world/World.java
zip -r submission.zip src/builder/world/WorldBuilder.java
zip -r submission.zip src/builder/world/BeanWorld.java
zip -r submission.zip src/builder/JavaBeanGameState.java
zip -r submission.zip src/builder/GameState.java
zip -r submission.zip src/builder/JavaBeanFarm.java
zip -r submission.zip src/builder/entities/Interactable.java
zip -r submission.zip src/builder/entities/resources/Cabbage.java
zip -r submission.zip src/builder/entities/resources/Ore.java
zip -r submission.zip src/builder/entities/tiles/Tile.java
zip -r submission.zip src/builder/entities/tiles/Dirt.java
zip -r submission.zip src/builder/entities/tiles/TileFactory.java
zip -r submission.zip src/builder/entities/tiles/OreVein.java
zip -r submission.zip src/builder/entities/tiles/Water.java
zip -r submission.zip src/builder/entities/tiles/Grass.java
zip -r submission.zip src/builder/entities/Usable.java
zip -r submission.zip src/builder/entities/Brutus.java
zip -r submission.zip src/builder/player/PlayerManager.java
zip -r submission.zip src/builder/player/ChickenFarmer.java
zip -r submission.zip src/builder/player/Player.java
zip -r submission.zip test/builder/inventory/TinyInventoryTest.java
zip -r submission.zip test/builder/world/BeanWorldTest.java

