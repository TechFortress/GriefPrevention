# GriefPrevention - The self-service anti-griefing Bukkit plugin for Minecraft servers since 2011.

Stop responding to grief and prevent it instead. GriefPrevention stops grief 
before it starts automatically without any effort from administrators, 
and with very little (self service) effort from players.
Because GriefPrevention teaches players for you, you won't have to publish a
training manual or tutorial for players, or add explanatory signs to your world.

GriefPrevention is compatible with Purpur, Spigot, Paper, and any other implemention of the Bukkit API.

- [Downloads](https://dev.bukkit.org/projects/grief-prevention/files)
- [Release Notes](https://github.com/TechFortress/GriefPrevention/releases)
- [Documentation (Help and Support)](https://docs.griefprevention.com)

---

### Adding GriefPrevention as a maven/gradle/etc. dependency

GriefPrevention will be added to maven central soon - in the meantime, there's this neat thing called JitPack [![](https://jitpack.io/v/TechFortress/GriefPrevention.svg)](https://jitpack.io/#TechFortress/GriefPrevention) that makes a public maven repo for public Github repos on the fly.
According to it, this is all you need to do to add to your pom.xml:
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Replace `<version>` number with this number: [![](https://jitpack.io/v/TechFortress/GriefPrevention.svg)](https://jitpack.io/#TechFortress/GriefPrevention)
```xml
	<dependency>
	    <groupId>com.github.TechFortress</groupId>
	    <artifactId>GriefPrevention</artifactId>
	    <version>16.18</version>
        <scope>provided</scope>
	</dependency>
```

You can also add it to gradle/sbt/leiningen projects: https://jitpack.io/#TechFortress/GriefPrevention/

---

[![Weird flex but ok](https://bstats.org/signatures/bukkit/GriefPrevention-legacy.svg)](https://bstats.org/plugin/bukkit/GriefPrevention-legacy)
(Plugin usage stats since version 16.11 - actual use across all versions is larger)
