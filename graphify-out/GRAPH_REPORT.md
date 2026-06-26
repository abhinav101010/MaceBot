# Graph Report - .  (2026-06-26)

## Corpus Check
- Corpus is ~7,251 words - fits in a single context window. You may not need a graph.

## Summary
- 183 nodes · 421 edges · 15 communities (10 shown, 5 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 35 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Command System|Command System]]
- [[_COMMUNITY_AI Combat Actions|AI Combat Actions]]
- [[_COMMUNITY_Client & Network|Client & Network]]
- [[_COMMUNITY_Server Mixins & PlayerBot Core|Server Mixins & PlayerBot Core]]
- [[_COMMUNITY_AI Raytracing & Skin Management|AI Raytracing & Skin Management]]
- [[_COMMUNITY_AI Controller Logic|AI Controller Logic]]
- [[_COMMUNITY_Bot Settings|Bot Settings]]
- [[_COMMUNITY_Item Stack Mixins|Item Stack Mixins]]
- [[_COMMUNITY_Data Generator|Data Generator]]
- [[_COMMUNITY_Mod Initialization|Mod Initialization]]
- [[_COMMUNITY_Player Settings|Player Settings]]

## God Nodes (most connected - your core abstractions)
1. `ActionManager` - 27 edges
2. `Controller` - 11 edges
3. `PlayerBot` - 10 edges
4. `PlayerBotConnection` - 7 edges
5. `PlayerBotNetHandler` - 6 edges
6. `PlayerBotSettings` - 6 edges
7. `RayTracer` - 6 edges
8. `execute()` - 5 edges
9. `displayName()` - 5 edges
10. `SkinManager` - 5 edges

## Surprising Connections (you probably didn't know these)
- `displayName()` --references--> `Override`  [EXTRACTED]
  src/main/java/net/katch0420/macebot/player/Kits.java →   _Bridges community 2 → community 0_
- `PlayerBot` --inherits--> `ServerPlayerEntity`  [EXTRACTED]
  src/main/java/net/katch0420/macebot/playerbot/PlayerBot.java →   _Bridges community 4 → community 3_
- `PlayerBotNetHandler` --inherits--> `ServerPlayNetworkHandler`  [EXTRACTED]
  src/main/java/net/katch0420/macebot/playerbot/PlayerBotNetHandler.java →   _Bridges community 3 → community 2_

## Import Cycles
- None detected.

## Communities (15 total, 5 thin omitted)

### Community 0 - "Command System"
Cohesion: 0.13
Nodes (17): ArgumentBuilder, Boolean, CommandContext, BotCommands, PlayerCommands, SkinCommands, Formatting, Kit (+9 more)

### Community 2 - "Client & Network"
Cohesion: 0.11
Nodes (12): ClientConnection, ClientModInitializer, MaceBotClient, NetworkSide, NetworkState, Override, Packet, PacketListener (+4 more)

### Community 3 - "Server Mixins & PlayerBot Core"
Cohesion: 0.15
Nodes (16): BlockPos, BlockState, CallbackInfoReturnable, CompletableFuture, ConnectedClientData, DamageSource, GameProfile, Inject (+8 more)

### Community 4 - "AI Raytracing & Skin Management"
Cohesion: 0.16
Nodes (10): BlockHitResult, Direction, Entity, HitResult, PlayerEntity_playerBotMixin, Redirect, ServerPlayerEntity, RayTracer (+2 more)

### Community 7 - "Item Stack Mixins"
Cohesion: 0.53
Nodes (3): CallbackInfo, LivingEntity, ItemStackMixin

### Community 8 - "Data Generator"
Cohesion: 0.60
Nodes (3): DataGeneratorEntrypoint, FabricDataGenerator, MaceBotDataGenerator

## Knowledge Gaps
- **1 isolated node(s):** `Colors`
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ActionManager` connect `AI Combat Actions` to `Server Mixins & PlayerBot Core`, `AI Raytracing & Skin Management`, `AI Controller Logic`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `PlayerBot` connect `Server Mixins & PlayerBot Core` to `Client & Network`, `AI Raytracing & Skin Management`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `displayName()` connect `Command System` to `Client & Network`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `Colors` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Command System` be split into smaller, more focused modules?**
  _Cohesion score 0.12944523470839261 - nodes in this community are weakly interconnected._
- **Should `Client & Network` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._