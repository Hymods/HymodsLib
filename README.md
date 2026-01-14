# Hymods / HymodsLib

## Maven

Repository:

```xml
		<repository>
			<id>hymods-repo</id>
			<url>https://repo.hymods.io/maven</url>
		</repository>
```

Dependency:

```xml
		<dependency>
			<groupId>io.hymods</groupId>
			<artifactId>hymodslib</artifactId>
			<version>1.0.0</version>
			<scope>provided</scope>
		</dependency>
```

Be sure to include HymodsLib as a `provided` dependency to avoid packaging it within your mod jar, and to include Hytale in your Mod install :)
