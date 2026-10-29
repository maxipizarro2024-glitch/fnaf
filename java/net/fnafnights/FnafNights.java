package net.fnafnights;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * FNaF: Nights at Fazbear's  -  versión FORGE 1.20.1 (mapeos oficiales de Mojang), todo en un solo archivo.
 * Las demás clases son clases estáticas anidadas.
 */
@Mod(FnafNights.MOD_ID)
public class FnafNights {
    public static final String MOD_ID = "fnafnights";

    public static ResourceLocation id(String path) { return new ResourceLocation(MOD_ID, path); }

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fnafnights.main"))
            .icon(() -> new ItemStack(ModItems.FAZ_COIN.get()))
            .displayItems((params, out) -> { for (RegistryObject<Item> i : ModItems.ALL) out.accept(i.get()); })
            .build());

    public FnafNights() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.init();
        ModBlocks.init();
        ModItems.init();
        ModEntities.TYPES_REG.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(FnafNights::onAttributes);
        Net.init();
    }

    private static void onAttributes(EntityAttributeCreationEvent e) {
        for (Kind k : Kind.values()) e.put(ModEntities.TYPES.get(k).get(), AnimatronicEntity.createAttributes(k).build());
    }

    /** Eventos del bus de Forge (servidor). */
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent e) {
            if (e.phase == TickEvent.Phase.END) {
                MinecraftServer s = ServerLifecycleHooks.getCurrentServer();
                if (s != null) NightManager.tick(s);
            }
        }
        @SubscribeEvent public static void onStopping(ServerStoppingEvent e) { NightManager.reset(); }
        @SubscribeEvent public static void onCommands(RegisterCommandsEvent e) { FnafCommand.register(e.getDispatcher()); }
        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
            if (e.getEntity() instanceof ServerPlayer sp) NightManager.onDisconnect(sp);
        }
    }

    // ======================================================================== Kind
    public enum Kind {
        FREDDY("freddy", "Freddy Fazbear"), BONNIE("bonnie", "Bonnie"), CHICA("chica", "Chica"), FOXY("foxy", "Foxy"),
        GOLDEN_FREDDY("golden_freddy", "Golden Freddy"), SPRINGTRAP("springtrap", "Springtrap");

        public final String id, display;
        Kind(String id, String display) { this.id = id; this.display = display; }
        public static Kind byOrdinal(int i) { Kind[] v = values(); return v[Math.floorMod(i, v.length)]; }
        public static Kind byId(String s) { for (Kind k : values()) if (k.id.equalsIgnoreCase(s)) return k; return null; }
    }

    // ======================================================================== Registros: entidades
    public static final class ModEntities {
        public static final DeferredRegister<EntityType<?>> TYPES_REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
        public static final Map<Kind, RegistryObject<EntityType<AnimatronicEntity>>> TYPES = new EnumMap<>(Kind.class);
        static {
            for (Kind k : Kind.values()) {
                TYPES.put(k, TYPES_REG.register(k.id, () -> EntityType.Builder.<AnimatronicEntity>of((t, w) -> new AnimatronicEntity(t, w, k), MobCategory.MISC)
                        .sized(0.9f, 2.3f).clientTrackingRange(10).fireImmune().build(MOD_ID + ":" + k.id)));
            }
        }
        public static void init() { }
    }

    // ======================================================================== Registros: items
    public static final class ModItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
        public static final List<RegistryObject<Item>> ALL = new ArrayList<>();

        public static final RegistryObject<Item> FAZ_COIN = reg("faz_coin", () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> PAYCHECK = reg("paycheck", () -> new Item(new Item.Properties().stacksTo(16)));
        public static final RegistryObject<Item> FREDDY_MASK = reg("freddy_mask",
                () -> new ArmorItem(FazbearMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties().durability(40)));
        public static final RegistryObject<Item> FLASHLIGHT = reg("flashlight", () -> new FlashlightItem(new Item.Properties().durability(400).stacksTo(1)));
        public static final RegistryObject<Item> PIZZA_SLICE = reg("pizza_slice", () -> new Item(new Item.Properties()
                .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7f).build())));
        public static final RegistryObject<Item> CUPCAKE = reg("cupcake", () -> new Item(new Item.Properties()
                .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4f).build())));
        static {
            egg(Kind.FREDDY, 0x70462a, 0x14141a); egg(Kind.BONNIE, 0x6054c4, 0xbe1e28); egg(Kind.CHICA, 0xeec624, 0xf08c1e);
            egg(Kind.FOXY, 0x962618, 0xbebec8); egg(Kind.GOLDEN_FREDDY, 0xdeb026, 0x1a1a1a); egg(Kind.SPRINGTRAP, 0x929442, 0x282814);
        }
        private static void egg(Kind k, int a, int b) {
            reg(k.id + "_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.TYPES.get(k), a, b, new Item.Properties()));
        }
        static RegistryObject<Item> reg(String name, Supplier<Item> s) {
            RegistryObject<Item> o = ITEMS.register(name, s);
            ALL.add(o);
            return o;
        }
        public static void init() { }
    }

    public enum FazbearMaterial implements ArmorMaterial {
        INSTANCE;
        @Override public int getDurabilityForType(ArmorItem.Type type) { return 40; }
        @Override public int getDefenseForType(ArmorItem.Type type) { return 0; }
        @Override public int getEnchantmentValue() { return 5; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.LEATHER); }
        @Override public String getName() { return MOD_ID + ":fazbear"; }   // Forge: textures/models/armor/fazbear_layer_1.png en el namespace del mod
        @Override public float getToughness() { return 0; }
        @Override public float getKnockbackResistance() { return 0; }
    }

    /** Linterna (FNaF 2): mantén click derecho. Visión nocturna y ralentiza al animatrónico al que apuntas. */
    public static class FlashlightItem extends Item {
        public FlashlightItem(Properties p) { super(p); }
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        @Override public int getUseDuration(ItemStack stack) { return 72000; }
        @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
        @Override
        public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
            if (level.isClientSide) return;
            if (user.tickCount % 20 == 0) user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, false, false, false));
            if (user.tickCount % 10 == 0) {
                Vec3 eye = user.getEyePosition();
                Vec3 look = user.getViewVector(1f).scale(12);
                AABB box = user.getBoundingBox().expandTowards(look).inflate(1);
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(user, eye, eye.add(look), box, e -> e instanceof AnimatronicEntity, 144);
                if (hit != null && hit.getEntity() instanceof AnimatronicEntity a) {
                    a.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                    a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, false));
                }
                stack.hurtAndBreak(1, user, p -> p.broadcastBreakEvent(user.getUsedItemHand()));
            }
        }
    }

    // ======================================================================== Registros: bloques
    public static final class ModBlocks {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
        public static final Map<String, RegistryObject<Block>> ALL = new LinkedHashMap<>();

        private static BlockBehaviour.Properties base(SoundType snd, float hardness) {
            return BlockBehaviour.Properties.of().strength(hardness, 6f).sound(snd).isValidSpawn((s, g, p, t) -> false);
        }
        private static BlockBehaviour.Properties metal() { return base(SoundType.METAL, 3f); }
        private static BlockBehaviour.Properties stone() { return base(SoundType.STONE, 2f); }
        private static BlockBehaviour.Properties cloth() { return base(SoundType.WOOL, 0.8f); }

        public static final RegistryObject<Block> CHECKER_TILE = reg("checker_tile", () -> new Block(stone()));
        public static final RegistryObject<Block> OFFICE_FLOOR = reg("office_floor", () -> new Block(stone()));
        public static final RegistryObject<Block> PIZZERIA_WALL = reg("pizzeria_wall", () -> new Block(stone()));
        public static final RegistryObject<Block> PIZZERIA_WALL_STRIPED = reg("pizzeria_wall_striped", () -> new Block(stone()));
        public static final RegistryObject<Block> STAGE_FLOOR = reg("stage_floor", () -> new Block(base(SoundType.WOOD, 2f)));
        public static final RegistryObject<Block> STAGE_CURTAIN = reg("stage_curtain", () -> new Block(cloth()));
        public static final RegistryObject<Block> PIRATE_COVE_CURTAIN = reg("pirate_cove_curtain", () -> new Block(cloth()));
        public static final RegistryObject<Block> PARTY_TABLE = reg("party_table", () -> new PartyTableBlock(cloth().noOcclusion()));
        public static final RegistryObject<Block> DIM_LIGHT = reg("dim_light", () -> new Block(metal().lightLevel(s -> 7)));
        public static final RegistryObject<Block> VENT_GRATE = reg("vent_grate", () -> new Block(metal()));

        public static final RegistryObject<Block> SECURITY_DOOR_LEFT = reg("security_door_left", () -> new SecurityDoorBlock(metal().noOcclusion().strength(-1f, 3600000f), true));
        public static final RegistryObject<Block> SECURITY_DOOR_RIGHT = reg("security_door_right", () -> new SecurityDoorBlock(metal().noOcclusion().strength(-1f, 3600000f), false));
        public static final RegistryObject<Block> DOOR_BUTTON_LEFT = reg("door_button_left", () -> new ControlButtonBlock(metal().noOcclusion(), ControlButtonBlock.Func.DOOR, true));
        public static final RegistryObject<Block> DOOR_BUTTON_RIGHT = reg("door_button_right", () -> new ControlButtonBlock(metal().noOcclusion(), ControlButtonBlock.Func.DOOR, false));
        public static final RegistryObject<Block> LIGHT_BUTTON_LEFT = reg("light_button_left", () -> new ControlButtonBlock(metal().noOcclusion(), ControlButtonBlock.Func.LIGHT, true));
        public static final RegistryObject<Block> LIGHT_BUTTON_RIGHT = reg("light_button_right", () -> new ControlButtonBlock(metal().noOcclusion(), ControlButtonBlock.Func.LIGHT, false));
        public static final RegistryObject<Block> HALL_LIGHT_LEFT = reg("hall_light_left", () -> new HallLightBlock(metal().noOcclusion().lightLevel(s -> s.getValue(HallLightBlock.LIT) ? 15 : 0), true));
        public static final RegistryObject<Block> HALL_LIGHT_RIGHT = reg("hall_light_right", () -> new HallLightBlock(metal().noOcclusion().lightLevel(s -> s.getValue(HallLightBlock.LIT) ? 15 : 0), false));
        public static final RegistryObject<Block> SECURITY_MONITOR = reg("security_monitor", () -> new SecurityMonitorBlock(metal().noOcclusion().lightLevel(s -> 4)));
        public static final RegistryObject<Block> OFFICE_DESK = reg("office_desk", () -> new OfficeDeskBlock(metal()));
        public static final RegistryObject<Block> SECURITY_CAMERA = reg("security_camera", () -> new WallDecorBlock(metal().noOcclusion(), WallDecorBlock.Style.CAMERA));

        public static final RegistryObject<Block> POSTER_CELEBRATE = reg("poster_celebrate", () -> new WallDecorBlock(cloth().noOcclusion().noCollission(), WallDecorBlock.Style.POSTER));
        public static final RegistryObject<Block> POSTER_ITS_ME = reg("poster_its_me", () -> new WallDecorBlock(cloth().noOcclusion().noCollission(), WallDecorBlock.Style.POSTER));
        public static final RegistryObject<Block> PLUSH_FREDDY = reg("plush_freddy", () -> new WallDecorBlock(cloth().noOcclusion(), WallDecorBlock.Style.PLUSH));
        public static final RegistryObject<Block> PLUSH_BONNIE = reg("plush_bonnie", () -> new WallDecorBlock(cloth().noOcclusion(), WallDecorBlock.Style.PLUSH));
        public static final RegistryObject<Block> PLUSH_CHICA = reg("plush_chica", () -> new WallDecorBlock(cloth().noOcclusion(), WallDecorBlock.Style.PLUSH));
        public static final RegistryObject<Block> PLUSH_FOXY = reg("plush_foxy", () -> new WallDecorBlock(cloth().noOcclusion(), WallDecorBlock.Style.PLUSH));

        private static RegistryObject<Block> reg(String name, Supplier<Block> s) {
            RegistryObject<Block> o = BLOCKS.register(name, s);
            ModItems.reg(name, () -> new BlockItem(o.get(), new Item.Properties()));
            ALL.put(name, o);
            return o;
        }
        public static void init() { }
    }

    // ======================================================================== Clases de bloque
    static final class WallShapes {
        /** Placa pegada a la pared (la espalda queda en el lado contrario a "facing"). */
        static VoxelShape wall(Direction f, int m, int d) {
            return switch (f) {
                case NORTH -> Block.box(m, m, 16 - d, 16 - m, 16 - m, 16);
                case SOUTH -> Block.box(m, m, 0, 16 - m, 16 - m, d);
                case EAST -> Block.box(0, m, m, d, 16 - m, 16 - m);
                default -> Block.box(16 - d, m, m, 16, 16 - m, 16 - m);
            };
        }
    }

    /** Botón del escritorio del guardia: puerta o luz, izquierda o derecha. */
    public static class ControlButtonBlock extends HorizontalDirectionalBlock {
        public enum Func { DOOR, LIGHT }
        public static final BooleanProperty ON = BooleanProperty.create("on");
        public final Func func;
        public final boolean left;

        public ControlButtonBlock(Properties p, Func func, boolean left) {
            super(p);
            this.func = func; this.left = left;
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ON, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, ON); }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
        }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return WallShapes.wall(s.getValue(FACING), 3, 3); }
        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) NightManager.press((ServerLevel) level, sp, this, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** Puerta de seguridad blindada (se controla desde los botones). */
    public static class SecurityDoorBlock extends HorizontalDirectionalBlock {
        public static final BooleanProperty OPEN = BooleanProperty.create("open");
        private static final VoxelShape NS = Block.box(0, 0, 6, 16, 16, 10);
        private static final VoxelShape EW = Block.box(6, 0, 0, 10, 16, 16);
        public final boolean left;

        public SecurityDoorBlock(Properties p, boolean left) {
            super(p);
            this.left = left;
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.EAST).setValue(OPEN, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, OPEN); }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getHorizontalDirection());
        }
        private static VoxelShape shape(BlockState s) {
            Direction f = s.getValue(FACING);
            return (f == Direction.EAST || f == Direction.WEST) ? EW : NS;
        }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return shape(s); }
        @Override public VoxelShape getCollisionShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) {
            return s.getValue(OPEN) ? Shapes.empty() : shape(s);
        }
    }

    /** Foco de pasillo: se enciende con el botón de luz. */
    public static class HallLightBlock extends HorizontalDirectionalBlock {
        public static final BooleanProperty LIT = BooleanProperty.create("lit");
        public final boolean left;
        public HallLightBlock(Properties p, boolean left) {
            super(p);
            this.left = left;
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, LIT); }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
        }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return WallShapes.wall(s.getValue(FACING), 4, 4); }
    }

    /** Monitor de cámaras. Click derecho = subir el monitor. */
    public static class SecurityMonitorBlock extends HorizontalDirectionalBlock {
        private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
        public SecurityMonitorBlock(Properties p) {
            super(p);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
        }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return SHAPE; }
        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) NightManager.openMonitor(sp);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    /** Escritorio del guardia. Click = empezar turno. Agachado + click = volver a la Noche 1. */
    public static class OfficeDeskBlock extends Block {
        public OfficeDeskBlock(Properties p) { super(p); }
        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) NightManager.deskUse(sp);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    public static class PartyTableBlock extends Block {
        private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(6, 0, 6, 10, 12, 10));
        public PartyTableBlock(Properties p) { super(p); }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return SHAPE; }
    }

    /** Pósters, cámaras de seguridad y peluches. */
    public static class WallDecorBlock extends HorizontalDirectionalBlock {
        public enum Style { POSTER, CAMERA, PLUSH }
        private final Style style;
        private static final VoxelShape PLUSH = Block.box(3, 0, 4, 13, 13, 12);
        private static final VoxelShape CAMERA = Block.box(4, 3, 2, 12, 13, 16);
        public WallDecorBlock(Properties p, Style style) {
            super(p);
            this.style = style;
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c) {
            return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
        }
        @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) {
            return switch (style) {
                case POSTER -> WallShapes.wall(s.getValue(FACING), 1, 1);
                case CAMERA -> CAMERA;
                case PLUSH -> PLUSH;
            };
        }
    }

    // ======================================================================== Entidad animatrónica
    /** Freddy/Bonnie/Chica/Foxy/Golden: sin IA (los mueve NightManager). Springtrap: cazador con IA vainilla. */
    public static class AnimatronicEntity extends PathfinderMob {
        private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(AnimatronicEntity.class, EntityDataSerializers.INT);
        public final Kind kind;

        public AnimatronicEntity(EntityType<? extends AnimatronicEntity> type, Level level, Kind kind) {
            super(type, level);
            this.kind = kind;
            this.setPersistenceRequired();
            if (kind != Kind.SPRINGTRAP) this.setNoAi(true);
        }

        public static AttributeSupplier.Builder createAttributes(Kind k) {
            return Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, k == Kind.SPRINGTRAP ? 80 : 200)
                    .add(Attributes.MOVEMENT_SPEED, 0.27)
                    .add(Attributes.ATTACK_DAMAGE, 9)
                    .add(Attributes.FOLLOW_RANGE, 48)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
        }

        @Override protected void defineSynchedData() { super.defineSynchedData(); this.entityData.define(STAGE, 0); }
        public int getStage() { return this.entityData.get(STAGE); }
        public void setStage(int s) { this.entityData.set(STAGE, s); }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7));
            this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10f));
            this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
            this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        public void tick() {
            super.tick();
            if (!level().isClientSide && kind != Kind.SPRINGTRAP) {
                Player p = level().getNearestPlayer(this, 14);
                if (p != null) this.lookAt(p, 25f, 25f);   // los ojos siempre te siguen
            }
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            if (kind != Kind.SPRINGTRAP && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.isCreativePlayer()) return false;
            return super.hurt(source, amount);
        }

        @Override public boolean isPushable() { return kind == Kind.SPRINGTRAP; }
        @Override public boolean removeWhenFarAway(double d) { return false; }
        @Override protected SoundEvent getAmbientSound() { return kind == Kind.SPRINGTRAP ? SoundEvents.ZOMBIE_AMBIENT : null; }
        @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ANVIL_HIT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.IRON_GOLEM_DEATH; }

        @Override public void addAdditionalSaveData(CompoundTag nbt) { super.addAdditionalSaveData(nbt); nbt.putInt("FoxyStage", getStage()); }
        @Override public void readAdditionalSaveData(CompoundTag nbt) { super.readAdditionalSaveData(nbt); setStage(nbt.getInt("FoxyStage")); }
    }

    // ======================================================================== Red
    public static final class Net {
        private static final String PROTO = "1";
        public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(id("main"), () -> PROTO, PROTO::equals, PROTO::equals);

        public record CamInfo(String id, String name, double x, double y, double z, float yaw, float pitch, boolean audioOnly) {}
        public record StateMsg(boolean active, int night, int ticks, float power, int usage, boolean ld, boolean rd, boolean ll, boolean rl, boolean powerOut) {}
        public record MonitorMsg(List<CamInfo> cams) {}
        public record JumpMsg(int kind) {}
        public record MonitorClosedMsg() {}
        public record CamSelectMsg(String id) {}

        public static void init() {
            int i = 0;
            CH.messageBuilder(StateMsg.class, i++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder((m, b) -> { b.writeBoolean(m.active); b.writeInt(m.night); b.writeInt(m.ticks); b.writeFloat(m.power); b.writeInt(m.usage);
                        b.writeBoolean(m.ld); b.writeBoolean(m.rd); b.writeBoolean(m.ll); b.writeBoolean(m.rl); b.writeBoolean(m.powerOut); })
                    .decoder(b -> new StateMsg(b.readBoolean(), b.readInt(), b.readInt(), b.readFloat(), b.readInt(),
                            b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readBoolean()))
                    .consumerMainThread((m, c) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onState(m)))
                    .add();
            CH.messageBuilder(MonitorMsg.class, i++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder((m, b) -> { b.writeInt(m.cams.size());
                        for (CamInfo c : m.cams) { b.writeUtf(c.id, 16); b.writeUtf(c.name, 48); b.writeDouble(c.x); b.writeDouble(c.y); b.writeDouble(c.z);
                            b.writeFloat(c.yaw); b.writeFloat(c.pitch); b.writeBoolean(c.audioOnly); } })
                    .decoder(b -> { int n = b.readInt(); List<CamInfo> l = new ArrayList<>();
                        for (int k = 0; k < n; k++) l.add(new CamInfo(b.readUtf(16), b.readUtf(48), b.readDouble(), b.readDouble(), b.readDouble(), b.readFloat(), b.readFloat(), b.readBoolean()));
                        return new MonitorMsg(l); })
                    .consumerMainThread((m, c) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onMonitor(m)))
                    .add();
            CH.messageBuilder(JumpMsg.class, i++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder((m, b) -> b.writeInt(m.kind)).decoder(b -> new JumpMsg(b.readInt()))
                    .consumerMainThread((m, c) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onJump(m)))
                    .add();
            CH.messageBuilder(MonitorClosedMsg.class, i++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder((m, b) -> { }).decoder(b -> new MonitorClosedMsg())
                    .consumerMainThread((m, c) -> { ServerPlayer p = c.get().getSender(); if (p != null) NightManager.onMonitorClosed(p); })
                    .add();
            CH.messageBuilder(CamSelectMsg.class, i++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder((m, b) -> b.writeUtf(m.id, 16)).decoder(b -> new CamSelectMsg(b.readUtf(16)))
                    .consumerMainThread((m, c) -> { ServerPlayer p = c.get().getSender(); if (p != null) NightManager.onCamSelect(p, m.id); })
                    .add();
        }

        static void toPlayer(ServerPlayer p, Object msg) { CH.send(PacketDistributor.PLAYER.with(() -> p), msg); }
    }

    // ======================================================================== Datos guardados del mapa
    public static class FnafMapState extends SavedData {
        public static class Node {
            public String id = "", name = "";
            public double sx, sy, sz;
            public boolean hasCam, audioOnly;
            public double cx, cy, cz;
            public float cyaw, cpitch;
        }
        public final Map<String, Node> nodes = new LinkedHashMap<>();
        public BlockPos office;
        public int nextNight = 1;

        public boolean isReady() {
            return office != null && nodes.containsKey("1A") && nodes.containsKey("1C") && nodes.containsKey("LEFT_DOOR") && nodes.containsKey("RIGHT_DOOR");
        }

        public static FnafMapState get(ServerLevel any) {
            return any.getServer().overworld().getDataStorage().computeIfAbsent(FnafMapState::load, FnafMapState::new, "fnafnights_map");
        }

        @Override
        public CompoundTag save(CompoundTag nbt) {
            ListTag list = new ListTag();
            for (Node n : nodes.values()) {
                CompoundTag c = new CompoundTag();
                c.putString("id", n.id); c.putString("name", n.name);
                c.putDouble("sx", n.sx); c.putDouble("sy", n.sy); c.putDouble("sz", n.sz);
                c.putBoolean("hasCam", n.hasCam); c.putBoolean("audio", n.audioOnly);
                c.putDouble("cx", n.cx); c.putDouble("cy", n.cy); c.putDouble("cz", n.cz);
                c.putFloat("cyaw", n.cyaw); c.putFloat("cpitch", n.cpitch);
                list.add(c);
            }
            nbt.put("nodes", list);
            if (office != null) nbt.putLong("office", office.asLong());
            nbt.putInt("next", nextNight);
            return nbt;
        }

        public static FnafMapState load(CompoundTag nbt) {
            FnafMapState s = new FnafMapState();
            ListTag list = nbt.getList("nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                Node n = new Node();
                n.id = c.getString("id"); n.name = c.getString("name");
                n.sx = c.getDouble("sx"); n.sy = c.getDouble("sy"); n.sz = c.getDouble("sz");
                n.hasCam = c.getBoolean("hasCam"); n.audioOnly = c.getBoolean("audio");
                n.cx = c.getDouble("cx"); n.cy = c.getDouble("cy"); n.cz = c.getDouble("cz");
                n.cyaw = c.getFloat("cyaw"); n.cpitch = c.getFloat("cpitch");
                s.nodes.put(n.id, n);
            }
            if (nbt.contains("office")) s.office = BlockPos.of(nbt.getLong("office"));
            s.nextNight = Math.max(1, nbt.getInt("next"));
            return s;
        }
    }

    // ======================================================================== Lógica de la noche
    /**
     * Mecánicas fieles a FNaF 1: 6 horas de 89 s, energía (puertas/luces/monitor), nivel de IA 0-20 con d20 por intento,
     * Bonnie por la izquierda, Chica y Freddy por la derecha, Foxy corriendo por el pasillo oeste, Golden Freddy alucinando.
     */
    public static final class NightManager {
        private NightManager() {}
        public static final int HOUR = 1780;

        public static boolean active, leftDoor, rightDoor, leftLight, rightLight, monitorUp, powerOut;
        public static int night, ticks;
        public static double power;
        public static String camViewed = "";

        public static final int[] AI_OVERRIDE = {-1, -1, -1, -1};
        private static final Kind[] CORE = {Kind.FREDDY, Kind.BONNIE, Kind.CHICA, Kind.FOXY};
        private static final int[][] AI = {{0, 0, 0, 0}, {0, 3, 1, 1}, {1, 0, 5, 2}, {1, 2, 4, 6}, {3, 5, 7, 5}, {4, 10, 12, 16}, {10, 10, 10, 10}};

        private static final Map<String, String[]> BONNIE_PATH = new HashMap<>();
        private static final Map<String, String[]> CHICA_PATH = new HashMap<>();
        static {
            BONNIE_PATH.put("1A", new String[]{"1B"});
            BONNIE_PATH.put("1B", new String[]{"5", "2A", "3"});
            BONNIE_PATH.put("5", new String[]{"2A", "3", "1B"});
            BONNIE_PATH.put("2A", new String[]{"3", "2B"});
            BONNIE_PATH.put("3", new String[]{"2A", "2B"});
            BONNIE_PATH.put("2B", new String[]{"LEFT_DOOR", "3"});
            CHICA_PATH.put("1A", new String[]{"1B"});
            CHICA_PATH.put("1B", new String[]{"7", "4A"});
            CHICA_PATH.put("7", new String[]{"6", "4A", "1B"});
            CHICA_PATH.put("6", new String[]{"4A", "7"});
            CHICA_PATH.put("4A", new String[]{"4B", "6"});
            CHICA_PATH.put("4B", new String[]{"RIGHT_DOOR", "4A"});
        }

        private static final String[][] CALLS = {
                {"¡Hola, hola! Soy el guardia que estuvo antes que tú; esto es una llamada de bienvenida.",
                        "Los muñecos de la pizzería tienen un problema: de noche se quedan sin 'modo libre' y se mueven por el local.",
                        "Si te encuentran sin la oficina cerrada, te confundirán con un esqueleto sin traje... y no acaba bien.",
                        "Mira las cámaras, cierra las puertas cuando haga falta, pero ojo con la energía: no es infinita.",
                        "Esta noche debería ser tranquila. Aguanta hasta las seis. ¡Buena suerte!"},
                {"Segunda noche. Bonnie suele ser el primero en rondar, atento al pasillo de la izquierda.",
                        "Si ves que un compañero se acerca a la ventana, enciende la luz y cierra la puerta a tiempo.",
                        "Y no malgastes batería con la luz: sólo cuando sospeches algo."},
                {"Tercera noche. Revisa la Cala del Pirata de vez en cuando: Foxy lleva un tiempo averiado... o eso dicen.",
                        "Si lo oyes correr por el pasillo oeste, cierra la puerta izquierda YA.",
                        "Y recuerda: cada golpe de Foxy te cuesta energía."},
                {"Cuarta noche... el cuerpo de Freddy se mueve cuando no lo miras.",
                        "Cuando llegue a la esquina del pasillo este, no bajes el monitor con él en la puerta. Seguro que lo entiendes.",
                        "... ¿has oído eso? Tengo que colgar..."},
                {"(Se oye una respiración y metal chirriando. La línea se corta.)"}, {}, {}};

        private static final int[] MELODY = {0, 4, 7, 12, 7, 4, 0, -5, 0, 4, 7, 12, 16, 12, 7, 4, 7, 4, 0, -5, -12, -5, 0};

        private static final Random R = new Random();
        private static final Map<Kind, Anim> anims = new EnumMap<>(Kind.class);
        private static UUID guardId;
        private static ServerLevel world;
        private static int jumpTimer, powerTimer, powerFreddyAt, golden, goldenTimer;
        private static UUID goldenUuid;
        private static boolean prevMonitor;

        private static class Anim { Kind kind; String node; int ai, stage, timer, knocks; boolean running; UUID uuid; }

        // ---------------------------------------------------------------- Entrada
        public static void deskUse(ServerPlayer p) {
            FnafMapState st = FnafMapState.get(p.serverLevel());
            if (active) {
                p.displayClientMessage(Component.literal("§eNoche " + night + " §7- " + hourLabel(ticks / HOUR) + " §7- Energía: §f" + (int) Math.ceil(power) + "%"), true);
                return;
            }
            if (p.isShiftKeyDown()) {
                st.nextNight = 1; st.setDirty();
                p.displayClientMessage(Component.literal("§7Progreso reiniciado: la próxima será la §eNoche 1§7."), true);
                return;
            }
            startNight(p, st.nextNight);
        }

        public static void startNight(ServerPlayer p, int n) {
            MinecraftServer server = p.getServer();
            ServerLevel w = server.overworld();
            if (p.serverLevel() != w) { p.sendSystemMessage(Component.literal("§cLa pizzería sólo funciona en el Overworld.")); return; }
            FnafMapState st = FnafMapState.get(w);
            if (!st.isReady()) { p.sendSystemMessage(Component.literal("§cNo hay pizzería configurada. Usa §e/fnaf generate§c.")); return; }
            if (active) { p.sendSystemMessage(Component.literal("§cYa hay un turno en curso.")); return; }
            n = Math.max(1, Math.min(7, n));

            world = w; guardId = p.getUUID(); night = n; ticks = 0; power = 100;
            leftDoor = rightDoor = leftLight = rightLight = monitorUp = powerOut = false;
            golden = 0; jumpTimer = 0; prevMonitor = false; camViewed = ""; goldenUuid = null;
            cleanup(w, st);
            anims.clear();
            int[] lv = AI[n - 1].clone();
            if (n == 4) lv[0] = 1 + R.nextInt(2);
            for (int i = 0; i < 4; i++) {
                Kind k = CORE[i];
                Anim a = new Anim();
                a.kind = k; a.ai = AI_OVERRIDE[i] >= 0 ? AI_OVERRIDE[i] : lv[i];
                a.node = k == Kind.FOXY ? "1C" : "1A";
                a.uuid = spawn(w, st, k, a.node).getUUID();
                anims.put(k, a);
            }
            applyBlocks(w, st.office);
            BlockPos o = st.office;
            p.teleportTo(w, o.getX() + 0.5, o.getY(), o.getZ() + 0.5, 0f, 10f);
            active = true;
            title(p, "§eNoche " + n, "§712:00 AM");
            w.playSound(null, o, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER, 1f, 0.7f);
            sendState(p);
        }

        public static void stop(MinecraftServer server, String reason) {
            if (!active) return;
            ServerPlayer p = guard(server);
            if (p != null && reason != null) p.sendSystemMessage(Component.literal("§7" + reason));
            endInternal(server);
        }

        public static void reset() { active = false; anims.clear(); guardId = null; world = null; monitorUp = false; }

        public static void onDisconnect(ServerPlayer p) { if (active && p.getUUID().equals(guardId)) endInternal(p.getServer()); }

        public static void setAi(Kind k, int level) {
            for (int i = 0; i < 4; i++) if (CORE[i] == k) AI_OVERRIDE[i] = level;
            Anim a = anims.get(k);
            if (a != null) a.ai = level;
        }

        // ---------------------------------------------------------------- Interacciones
        public static void press(ServerLevel w, ServerPlayer p, ControlButtonBlock b, BlockPos pos) {
            if (active && powerOut) { p.displayClientMessage(Component.literal("§cSin energía..."), true); return; }
            if (b.func == ControlButtonBlock.Func.DOOR) {
                boolean now;
                if (b.left) now = leftDoor = !leftDoor; else now = rightDoor = !rightDoor;
                w.playSound(null, pos, now ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
            } else {
                boolean now;
                if (b.left) now = leftLight = !leftLight; else now = rightLight = !rightLight;
                w.playSound(null, pos, now ? SoundEvents.LEVER_CLICK : SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 1f, 1.2f);
            }
            applyBlocks(w, pos);
            if (active) sendState(p);
        }

        public static void openMonitor(ServerPlayer p) {
            ServerLevel w = p.serverLevel();
            FnafMapState st = FnafMapState.get(w);
            List<Net.CamInfo> cams = new ArrayList<>();
            for (FnafMapState.Node n : st.nodes.values())
                if (n.hasCam) cams.add(new Net.CamInfo(n.id, n.name, n.cx, n.cy, n.cz, n.cyaw, n.cpitch, n.audioOnly));
            if (cams.isEmpty()) { p.displayClientMessage(Component.literal("§cEste monitor no tiene cámaras configuradas (/fnaf generate)."), true); return; }
            boolean guardMode = active && p.getUUID().equals(guardId);
            if (guardMode && powerOut) { p.displayClientMessage(Component.literal("§cSin energía..."), true); return; }
            if (guardMode && jumpTimer > 0) return;
            Net.toPlayer(p, new Net.MonitorMsg(cams));
            w.playSound(null, p.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.3f);
            if (guardMode) { monitorUp = true; camViewed = cams.get(0).id(); }
        }

        public static void onMonitorClosed(ServerPlayer p) {
            if (active && p.getUUID().equals(guardId)) {
                monitorUp = false; camViewed = "";
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 1.3f);
            }
        }

        public static void onCamSelect(ServerPlayer p, String id) { if (active && p.getUUID().equals(guardId) && monitorUp) camViewed = id; }

        // ---------------------------------------------------------------- Tick
        public static void tick(MinecraftServer server) {
            if (!active) return;
            ServerPlayer p = guard(server);
            if (p == null) { endInternal(server); return; }
            if (jumpTimer > 0) { if (--jumpTimer == 0) gameOver(p); return; }
            ticks++;

            if (ticks % HOUR == 0) {
                int h = ticks / HOUR;
                if (h >= 6) { win(p); return; }
                hourChanged(h, p);
            }
            phoneCall(p);

            if (!powerOut) {
                power -= usage() / 192.0;
                if (power <= 0) { power = 0; startPowerOut(p); }
            } else tickPowerOut(p);

            if (!powerOut) {
                if (ticks % 60 == 0) freddyOpp();
                if (ticks % 99 == 0) bonnieOpp(p);
                if (ticks % 100 == 0) { chicaOpp(p); foxyOpp(); }
                tickFreddyDoor(p);
                tickFoxyRun(p);
            }

            if (prevMonitor != monitorUp) {
                if (monitorUp) onMonitorRaised(); else onMonitorLowered(p);
                prevMonitor = monitorUp;
            }
            if (ticks % 20 == 0) tickSecond(p);
            if (golden == 2 && ++goldenTimer >= 100) { removeGolden(); golden = 0; jumpscare(p, Kind.GOLDEN_FREDDY); return; }
            if (ticks % 5 == 0) sendState(p);
        }

        private static void tickSecond(ServerPlayer p) {
            if (golden == 0 && monitorUp && camViewed.equals("2B") && R.nextInt(70) == 0) {
                goldenUuid = spawn(world, FnafMapState.get(world), Kind.GOLDEN_FREDDY, "2B").getUUID();
                golden = 1;
            } else if (golden == 1 && !camViewed.equals("2B") && monitorUp) { removeGolden(); golden = 0; }
            Anim chica = anims.get(Kind.CHICA);
            if (monitorUp && camViewed.equals("6") && chica != null && chica.node.equals("6") && R.nextInt(2) == 0)
                world.playSound(null, p.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.7f, 0.5f + R.nextFloat() * 0.6f);
            Anim b = anims.get(Kind.BONNIE), c = anims.get(Kind.CHICA);
            if (leftLight && b != null && b.node.equals("LEFT_DOOR")) p.displayClientMessage(Component.literal("§c¡Hay alguien en la ventana izquierda!"), true);
            if (rightLight && c != null && c.node.equals("RIGHT_DOOR")) p.displayClientMessage(Component.literal("§c¡Hay alguien en la ventana derecha!"), true);
        }

        private static void onMonitorRaised() { if (golden == 2) { removeGolden(); golden = 0; } }

        private static void onMonitorLowered(ServerPlayer p) {
            if (golden == 1) {
                Entity e = world.getEntity(goldenUuid);
                Vec3 look = Vec3.directionFromRotation(0, p.getYRot()).normalize();
                if (e != null) {
                    e.moveTo(p.getX() + look.x * 2, p.getY(), p.getZ() + look.z * 2, p.getYRot() + 180, 0);
                    e.setYHeadRot(p.getYRot() + 180); e.setYBodyRot(p.getYRot() + 180);
                }
                golden = 2; goldenTimer = 0;
                p.displayClientMessage(Component.literal("§6§o...sube el monitor..."), true);
            }
        }

        private static void removeGolden() {
            if (goldenUuid != null && world != null) { Entity e = world.getEntity(goldenUuid); if (e != null) e.discard(); }
            goldenUuid = null;
        }

        // ---------------------------------------------------------------- Hora / llamadas
        private static void hourChanged(int h, ServerPlayer p) {
            p.displayClientMessage(Component.literal("§e" + hourLabel(h)), true);
            world.playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.MASTER, 1f, 1.5f);
            if (h == 2) boost(Kind.BONNIE);
            if (h == 3 || h == 4) { boost(Kind.BONNIE); boost(Kind.CHICA); boost(Kind.FOXY); }
        }
        private static void boost(Kind k) { Anim a = anims.get(k); if (a != null) a.ai = Math.min(20, a.ai + 1); }

        private static void phoneCall(ServerPlayer p) {
            if (night < 1 || night > CALLS.length) return;
            String[] lines = CALLS[night - 1];
            for (int i = 0; i < lines.length; i++)
                if (ticks == 100 + i * 240) p.sendSystemMessage(Component.literal("§8[§7Teléfono§8] §7" + lines[i]));
        }

        public static String hourLabel(int h) { return (h == 0 ? 12 : h) + " AM"; }
        private static int usage() { return 1 + (leftDoor ? 1 : 0) + (rightDoor ? 1 : 0) + (leftLight ? 1 : 0) + (rightLight ? 1 : 0) + (monitorUp ? 1 : 0); }

        // ---------------------------------------------------------------- IA
        private static boolean roll(Anim a) { return a.ai > 0 && R.nextInt(20) + 1 <= a.ai; }
        private static String pick(Map<String, String[]> g, String node) { String[] o = g.get(node); return o == null ? "1B" : o[R.nextInt(o.length)]; }

        private static void freddyOpp() {
            Anim a = anims.get(Kind.FREDDY);
            if (a.node.equals("RIGHT_DOOR")) return;
            if (monitorUp && camViewed.equals(a.node)) return;
            if (!roll(a)) return;
            String next = switch (a.node) {
                case "1A" -> "1B"; case "1B" -> "7"; case "7" -> "6"; case "6" -> "4A"; case "4A" -> "4B"; case "4B" -> "RIGHT_DOOR";
                default -> "1B";
            };
            move(a, next);
        }

        private static void tickFreddyDoor(ServerPlayer p) {
            Anim a = anims.get(Kind.FREDDY);
            if (!a.node.equals("RIGHT_DOOR")) return;
            if (monitorUp || rightDoor) { a.timer = 0; return; }
            if (++a.timer >= 140) attempt(p, a);
        }

        private static void bonnieOpp(ServerPlayer p) {
            Anim a = anims.get(Kind.BONNIE);
            if (!roll(a)) return;
            if (a.node.equals("LEFT_DOOR")) { if (!leftDoor) attempt(p, a); else move(a, "1B"); return; }
            move(a, pick(BONNIE_PATH, a.node));
        }

        private static void chicaOpp(ServerPlayer p) {
            Anim a = anims.get(Kind.CHICA);
            if (!roll(a)) return;
            if (a.node.equals("RIGHT_DOOR")) { if (!rightDoor) attempt(p, a); else move(a, "1B"); return; }
            move(a, pick(CHICA_PATH, a.node));
        }

        private static void foxyOpp() {
            Anim a = anims.get(Kind.FOXY);
            if (a.running) return;
            if (monitorUp && camViewed.equals("1C")) return;
            if (!roll(a)) return;
            if (a.stage < 3) {
                a.stage++;
                setFoxyStage(a, a.stage);
                world.playSound(null, BlockPos.containing(nodePos(a.node, Kind.FOXY)), SoundEvents.CHAIN_STEP, SoundSource.HOSTILE, 1f, 0.6f);
            } else {
                a.running = true; a.timer = 0;
                setFoxyStage(a, 4);
                move(a, "2A");
            }
        }

        private static void tickFoxyRun(ServerPlayer p) {
            Anim a = anims.get(Kind.FOXY);
            if (!a.running) return;
            a.timer++;
            if (a.timer == 60) move(a, "LEFT_DOOR");
            if (a.timer == 85) {
                if (leftDoor) {
                    power = Math.max(0, power - (1 + 5 * a.knocks));
                    a.knocks++;
                    world.playSound(null, p.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 1.5f, 0.6f);
                    a.running = false; a.stage = 1 + R.nextInt(2);
                    setFoxyStage(a, a.stage);
                    move(a, "1C");
                } else attempt(p, a);
            }
        }

        private static void setFoxyStage(Anim a, int s) { Entity e = world.getEntity(a.uuid); if (e instanceof AnimatronicEntity ae) ae.setStage(s); }

        private static void attempt(ServerPlayer p, Anim a) {
            ItemStack head = p.getItemBySlot(EquipmentSlot.HEAD);
            boolean maskable = a.kind == Kind.FREDDY || a.kind == Kind.BONNIE || a.kind == Kind.CHICA;
            if (maskable && head.is(ModItems.FREDDY_MASK.get())) {
                head.hurtAndBreak(10, p, pl -> pl.broadcastBreakEvent(EquipmentSlot.HEAD));
                p.displayClientMessage(Component.literal("§e¡La máscara de Freddy los ha engañado!"), true);
                a.timer = 0;
                move(a, "1B");
                return;
            }
            jumpscare(p, a.kind);
        }

        // ---------------------------------------------------------------- Apagón
        private static void startPowerOut(ServerPlayer p) {
            powerOut = true; powerTimer = 0;
            leftDoor = rightDoor = leftLight = rightLight = false;
            if (monitorUp) { monitorUp = false; camViewed = ""; }
            applyBlocks(world, FnafMapState.get(world).office);
            powerFreddyAt = 250 + R.nextInt(250);
            p.displayClientMessage(Component.literal("§4§l¡Se fue la energía!"), true);
            world.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.MASTER, 1.5f, 0.6f);
            sendState(p);
        }

        private static void tickPowerOut(ServerPlayer p) {
            powerTimer++;
            if (powerTimer % 14 == 0) {
                int i = powerTimer / 14;
                if (i < MELODY.length) {
                    float pitch = (float) Math.pow(2.0, (MELODY[i] - 6) / 12.0);
                    world.playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.RECORDS, 1.6f, pitch);
                }
            }
            Anim f = anims.get(Kind.FREDDY);
            if (powerTimer == powerFreddyAt) move(f, "RIGHT_DOOR");
            if (powerTimer == powerFreddyAt + 100) {
                Entity e = world.getEntity(f.uuid);
                Vec3 look = Vec3.directionFromRotation(0, p.getYRot()).normalize();
                if (e != null) e.moveTo(p.getX() + look.x * 2, p.getY(), p.getZ() + look.z * 2, p.getYRot() + 180, 0);
                world.playSound(null, p.blockPosition(), SoundEvents.IRON_GOLEM_STEP, SoundSource.HOSTILE, 2f, 0.4f);
            }
            if (powerTimer >= powerFreddyAt + 170) attempt(p, f);
        }

        // ---------------------------------------------------------------- Fin
        private static void jumpscare(ServerPlayer p, Kind k) {
            if (jumpTimer > 0) return;
            jumpTimer = 45;
            monitorUp = false; camViewed = "";
            Net.toPlayer(p, new Net.JumpMsg(k.ordinal()));
            world.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_SCREAM, SoundSource.MASTER, 2f, 0.5f);
            world.playSound(null, p.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.MASTER, 2f, 0.6f);
        }

        private static void gameOver(ServerPlayer p) {
            endInternal(p.getServer());
            titleFade(p);
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§4FIN DEL JUEGO")));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§7Vuelve a intentar la noche " + night)));
        }

        private static void win(ServerPlayer p) {
            int n = night;
            FnafMapState st = FnafMapState.get(world);
            st.nextNight = Math.min(7, n + 1); st.setDirty();
            endInternal(p.getServer());
            titleFade(p);
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a6:00 AM")));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§7¡Sobreviviste a la noche " + n + "!")));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.PAYCHECK.get()));
            p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.FAZ_COIN.get(), 4 + n * 3));
            String extra = n == 5 ? " §6¡Noche 6 desbloqueada!" : n == 6 ? " §6¡Noche 7 (personalizada) desbloqueada!" : "";
            p.sendSystemMessage(Component.literal("§aHas cobrado tu cheque de Fazbear Entertainment." + extra));
        }

        private static void endInternal(MinecraftServer server) {
            ServerLevel w = world != null ? world : server.overworld();
            FnafMapState st = FnafMapState.get(w);
            active = false; monitorUp = false; powerOut = false;
            leftDoor = rightDoor = leftLight = rightLight = false;
            if (world != null) removeGolden();
            cleanup(w, st);
            anims.clear();
            if (st.office != null) applyBlocks(w, st.office);
            ServerPlayer p = guard(server);
            if (p != null) sendState(p);
        }

        public static void spawnShowcase(ServerLevel w, FnafMapState st) {
            cleanup(w, st);
            spawn(w, st, Kind.FREDDY, "1A"); spawn(w, st, Kind.BONNIE, "1A"); spawn(w, st, Kind.CHICA, "1A"); spawn(w, st, Kind.FOXY, "1C");
        }

        private static void cleanup(ServerLevel w, FnafMapState st) {
            if (st.office == null) return;
            AABB box = new AABB(st.office).inflate(80);
            for (AnimatronicEntity e : w.getEntitiesOfClass(AnimatronicEntity.class, box, x -> x.kind != Kind.SPRINGTRAP)) e.discard();
        }

        // ---------------------------------------------------------------- Movimiento / mundo
        private static ServerPlayer guard(MinecraftServer s) { return guardId == null ? null : s.getPlayerList().getPlayer(guardId); }

        private static double offsetFor(Kind k, String node) {
            double ox = switch (k) { case BONNIE -> -1.2; case CHICA -> 1.2; default -> 0.0; };
            return node.equals("1A") ? ox * 3.2 : ox;
        }

        private static Vec3 nodePos(String node, Kind kind) {
            FnafMapState.Node n = FnafMapState.get(world).nodes.get(node);
            return new Vec3(n.sx + offsetFor(kind, node), n.sy, n.sz);
        }

        private static float yawFor(FnafMapState st, FnafMapState.Node n, Vec3 from) {
            double tx = n.hasCam ? n.cx : st.office.getX() + 0.5;
            double tz = n.hasCam ? n.cz : st.office.getZ() + 0.5;
            return (float) (Math.toDegrees(Math.atan2(tz - from.z, tx - from.x)) - 90.0);
        }

        private static void move(Anim a, String node) {
            FnafMapState st = FnafMapState.get(world);
            a.node = node;
            if (a.kind == Kind.FREDDY) a.timer = 0;
            Entity e = world.getEntity(a.uuid);
            if (!(e instanceof AnimatronicEntity)) { AnimatronicEntity fresh = spawn(world, st, a.kind, node); a.uuid = fresh.getUUID(); e = fresh; }
            Vec3 pos = nodePos(node, a.kind);
            float yaw = yawFor(st, st.nodes.get(node), pos);
            e.moveTo(pos.x, pos.y, pos.z, yaw, 0);
            e.setYHeadRot(yaw); e.setYBodyRot(yaw);
            world.playSound(null, BlockPos.containing(pos), SoundEvents.IRON_GOLEM_STEP, SoundSource.HOSTILE, 0.9f, 0.55f);
        }

        public static AnimatronicEntity spawn(ServerLevel w, FnafMapState st, Kind k, String node) {
            AnimatronicEntity e = ModEntities.TYPES.get(k).get().create(w);
            FnafMapState.Node n = st.nodes.get(node);
            Vec3 pos = new Vec3(n.sx + offsetFor(k, node), n.sy, n.sz);
            float yaw = yawFor(st, n, pos);
            e.moveTo(pos.x, pos.y, pos.z, yaw, 0);
            e.setYHeadRot(yaw); e.setYBodyRot(yaw);
            w.addFreshEntity(e);
            return e;
        }

        /** Sincroniza puertas, focos y botones físicos con el estado lógico. */
        public static void applyBlocks(ServerLevel w, BlockPos around) {
            for (BlockPos bp : BlockPos.betweenClosed(around.offset(-14, -5, -14), around.offset(14, 6, 14))) {
                BlockState s = w.getBlockState(bp);
                Block b = s.getBlock();
                if (b instanceof SecurityDoorBlock d) {
                    boolean open = !(d.left ? leftDoor : rightDoor);
                    if (s.getValue(SecurityDoorBlock.OPEN) != open) w.setBlock(bp.immutable(), s.setValue(SecurityDoorBlock.OPEN, open), 3);
                } else if (b instanceof HallLightBlock l) {
                    boolean lit = l.left ? leftLight : rightLight;
                    if (s.getValue(HallLightBlock.LIT) != lit) w.setBlock(bp.immutable(), s.setValue(HallLightBlock.LIT, lit), 3);
                } else if (b instanceof ControlButtonBlock cb) {
                    boolean on = cb.func == ControlButtonBlock.Func.DOOR ? (cb.left ? leftDoor : rightDoor) : (cb.left ? leftLight : rightLight);
                    if (s.getValue(ControlButtonBlock.ON) != on) w.setBlock(bp.immutable(), s.setValue(ControlButtonBlock.ON, on), 3);
                }
            }
        }

        private static void sendState(ServerPlayer p) {
            Net.toPlayer(p, new Net.StateMsg(active, night, ticks, (float) power, usage(), leftDoor, rightDoor, leftLight, rightLight, powerOut));
        }

        private static void title(ServerPlayer p, String main, String sub) {
            titleFade(p);
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(main)));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
        }
        private static void titleFade(ServerPlayer p) { p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 60, 20)); }
    }

    // ======================================================================== Comandos
    /**
     * /fnaf generate | start [1-7] | stop | ai <animatronico> <0-20> | cam <id> <nombre> | stand <id> | office
     */
    public static final class FnafCommand {
        private FnafCommand() {}

        public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> d) {
            d.register(Commands.literal("fnaf")
                    .then(Commands.literal("generate").requires(s -> s.hasPermission(2)).executes(c -> generate(c.getSource())))
                    .then(Commands.literal("start").executes(c -> start(c.getSource(), -1))
                            .then(Commands.argument("noche", IntegerArgumentType.integer(1, 7))
                                    .executes(c -> start(c.getSource(), IntegerArgumentType.getInteger(c, "noche")))))
                    .then(Commands.literal("stop").executes(c -> { NightManager.stop(c.getSource().getServer(), "Turno cancelado."); return 1; }))
                    .then(Commands.literal("ai").requires(s -> s.hasPermission(2))
                            .then(Commands.argument("animatronico", StringArgumentType.word())
                                    .then(Commands.argument("nivel", IntegerArgumentType.integer(0, 20))
                                            .executes(c -> ai(c.getSource(), StringArgumentType.getString(c, "animatronico"), IntegerArgumentType.getInteger(c, "nivel"))))))
                    .then(Commands.literal("cam").requires(s -> s.hasPermission(2))
                            .then(Commands.argument("id", StringArgumentType.word())
                                    .then(Commands.argument("nombre", StringArgumentType.greedyString())
                                            .executes(c -> cam(c.getSource(), StringArgumentType.getString(c, "id"), StringArgumentType.getString(c, "nombre"))))))
                    .then(Commands.literal("stand").requires(s -> s.hasPermission(2))
                            .then(Commands.argument("id", StringArgumentType.word()).executes(c -> stand(c.getSource(), StringArgumentType.getString(c, "id")))))
                    .then(Commands.literal("office").requires(s -> s.hasPermission(2)).executes(c -> office(c.getSource()))));
        }

        private static int generate(CommandSourceStack src) throws CommandSyntaxException {
            ServerPlayer p = src.getPlayerOrException();
            ServerLevel w = src.getServer().overworld();
            if (p.serverLevel() != w) { src.sendFailure(Component.literal("Sólo en el Overworld.")); return 0; }
            MapBuilder.build(w, p.blockPosition());
            BlockPos o = FnafMapState.get(w).office;
            p.teleportTo(w, o.getX() + 0.5, o.getY(), o.getZ() + 0.5, 0f, 10f);
            src.sendSuccess(() -> Component.literal("§aPizzería de Freddy Fazbear construida. §7Usa el §eEscritorio§7 (click derecho) para empezar la Noche 1."), true);
            return 1;
        }

        private static int start(CommandSourceStack src, int night) throws CommandSyntaxException {
            ServerPlayer p = src.getPlayerOrException();
            FnafMapState st = FnafMapState.get(p.serverLevel());
            NightManager.startNight(p, night < 0 ? st.nextNight : night);
            return 1;
        }

        private static int ai(CommandSourceStack src, String name, int level) {
            Kind k = Kind.byId(name);
            if (k == null || k.ordinal() > Kind.FOXY.ordinal()) { src.sendFailure(Component.literal("Usa: freddy, bonnie, chica o foxy")); return 0; }
            NightManager.setAi(k, level);
            src.sendSuccess(() -> Component.literal("§aIA de " + k.display + " = " + level), true);
            return 1;
        }

        private static int cam(CommandSourceStack src, String id, String name) throws CommandSyntaxException {
            ServerPlayer p = src.getPlayerOrException();
            FnafMapState st = FnafMapState.get(p.serverLevel());
            FnafMapState.Node n = st.nodes.computeIfAbsent(id, k -> { FnafMapState.Node x = new FnafMapState.Node(); x.id = k; return x; });
            n.name = name; n.hasCam = true; n.audioOnly = name.toLowerCase().contains("audio");
            n.cx = p.getX(); n.cy = p.getEyeY(); n.cz = p.getZ(); n.cyaw = p.getYRot(); n.cpitch = p.getXRot();
            st.setDirty();
            src.sendSuccess(() -> Component.literal("§aCámara " + id + " guardada. §7(Si el nombre contiene 'audio' será sólo sonido.)"), false);
            return 1;
        }

        private static int stand(CommandSourceStack src, String id) throws CommandSyntaxException {
            ServerPlayer p = src.getPlayerOrException();
            FnafMapState st = FnafMapState.get(p.serverLevel());
            FnafMapState.Node n = st.nodes.computeIfAbsent(id, k -> { FnafMapState.Node x = new FnafMapState.Node(); x.id = k; x.name = k; return x; });
            n.sx = p.getX(); n.sy = p.getY(); n.sz = p.getZ();
            st.setDirty();
            src.sendSuccess(() -> Component.literal("§aPunto '" + id + "' guardado. §7IDs: 1A 1B 1C 2A 2B 3 4A 4B 5 6 7 LEFT_DOOR RIGHT_DOOR"), false);
            return 1;
        }

        private static int office(CommandSourceStack src) throws CommandSyntaxException {
            ServerPlayer p = src.getPlayerOrException();
            FnafMapState st = FnafMapState.get(p.serverLevel());
            st.office = p.blockPosition(); st.setDirty();
            src.sendSuccess(() -> Component.literal("§aOficina definida aquí."), false);
            return 1;
        }
    }

    // ======================================================================== Generador de la pizzería
    /**
     * Mapa local 45x45; (22,34) es la silla del guardia. Norte (-Z) arriba:
     *   Backstage(5) | Escenario(1A) | Cocina(6)   -   Comedor(1B) al centro   -   Cala Pirata(1C), Almacén(3), Baños(7)
     *   Pasillo Oeste(2A,2B) y Pasillo Este(4A,4B) a los lados de la Oficina.
     */
    public static final class MapBuilder {
        private MapBuilder() {}
        private static ServerLevel w;
        private static int ox, oz, y0;

        private record Room(int x1, int z1, int x2, int z2, Block wall, Block floor) {}
        private record Cam(String id, String name, int cx, int cz, int tx, int tz, int sx, int sz, boolean audio, boolean stage) {}

        private static BlockPos P(int lx, int y, int lz) { return new BlockPos(ox + lx, y, oz + lz); }
        private static void set(int lx, int y, int lz, BlockState s) { w.setBlock(P(lx, y, lz), s, 2); }
        private static void set(int lx, int y, int lz, Block b) { set(lx, y, lz, b.defaultBlockState()); }
        private static void fill(int x1, int y1, int z1, int x2, int y2, int z2, Block b) {
            BlockState s = b.defaultBlockState();
            for (int x = x1; x <= x2; x++) for (int y = y1; y <= y2; y++) for (int z = z1; z <= z2; z++) set(x, y, z, s);
        }
        private static BlockState facing(Block b, Direction d) { return b.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, d); }

        public static void build(ServerLevel world, BlockPos playerPos) {
            w = world;
            ox = playerPos.getX() - 22; oz = playerPos.getZ() - 34; y0 = playerPos.getY() - 1;
            Block WALL = ModBlocks.PIZZERIA_WALL.get(), STRIPED = ModBlocks.PIZZERIA_WALL_STRIPED.get(), CHECK = ModBlocks.CHECKER_TILE.get(),
                    OFFICE = ModBlocks.OFFICE_FLOOR.get(), STAGEF = ModBlocks.STAGE_FLOOR.get();

            Room[] rooms = {
                    new Room(14, 2, 30, 7, STRIPED, CHECK), new Room(12, 9, 32, 22, STRIPED, CHECK),
                    new Room(2, 2, 10, 10, WALL, OFFICE), new Room(2, 13, 10, 20, WALL, STAGEF),
                    new Room(12, 24, 16, 38, WALL, OFFICE), new Room(28, 24, 32, 38, WALL, OFFICE),
                    new Room(5, 27, 10, 31, WALL, OFFICE), new Room(18, 30, 26, 38, WALL, OFFICE),
                    new Room(34, 9, 42, 18, STRIPED, CHECK), new Room(34, 21, 42, 28, WALL, OFFICE)};

            fill(0, y0 - 1, 0, 44, y0 + 9, 44, Blocks.AIR);
            for (Room r : rooms) {
                fill(r.x1 - 1, y0 - 1, r.z1 - 1, r.x2 + 1, y0 - 1, r.z2 + 1, Blocks.POLISHED_DEEPSLATE);
                fill(r.x1 - 1, y0, r.z1 - 1, r.x2 + 1, y0 + 6, r.z2 + 1, r.wall);
                fill(r.x1 - 1, y0 + 7, r.z1 - 1, r.x2 + 1, y0 + 7, r.z2 + 1, WALL);
            }
            for (Room r : rooms) {
                fill(r.x1, y0 + 1, r.z1, r.x2, y0 + 6, r.z2, Blocks.AIR);
                fill(r.x1, y0, r.z1, r.x2, y0, r.z2, r.floor);
            }
            opening(14, 8, 30, 8, 6); opening(11, 9, 11, 10, 3); opening(11, 15, 11, 18, 4); opening(13, 23, 15, 23, 3);
            opening(29, 23, 31, 23, 3); opening(11, 28, 11, 30, 3); opening(33, 12, 33, 14, 3); opening(33, 25, 33, 27, 3);
            opening(17, 33, 17, 35, 3); opening(27, 33, 27, 35, 3);

            fill(15, y0 + 1, 2, 29, y0 + 1, 6, STAGEF);
            fill(14, y0 + 1, 2, 14, y0 + 6, 6, ModBlocks.STAGE_CURTAIN.get());
            fill(30, y0 + 1, 2, 30, y0 + 6, 6, ModBlocks.STAGE_CURTAIN.get());
            fill(10, y0 + 1, 15, 10, y0 + 4, 18, ModBlocks.PIRATE_COVE_CURTAIN.get());

            int[][] tables = {{16, 13}, {22, 13}, {28, 13}, {16, 19}, {22, 19}, {28, 19}};
            for (int[] t : tables) set(t[0], y0 + 1, t[1], ModBlocks.PARTY_TABLE.get());
            int[][] lights = {{22, 4}, {17, 4}, {27, 4}, {16, 12}, {22, 15}, {28, 12}, {16, 20}, {28, 20}, {6, 6}, {6, 16}, {8, 29}, {14, 26}, {30, 26}, {22, 32}, {38, 13}, {38, 24}};
            for (int[] l : lights) set(l[0], y0 + 6, l[1], ModBlocks.DIM_LIGHT.get());

            set(20, y0 + 2, 29, ModBlocks.VENT_GRATE.get()); set(24, y0 + 2, 29, ModBlocks.VENT_GRATE.get());
            set(22, y0 + 1, 36, ModBlocks.OFFICE_DESK.get());
            set(22, y0 + 2, 36, facing(ModBlocks.SECURITY_MONITOR.get(), Direction.NORTH));
            set(21, y0 + 2, 36, facing(ModBlocks.PLUSH_FREDDY.get(), Direction.NORTH));
            set(23, y0 + 2, 36, facing(ModBlocks.PLUSH_CHICA.get(), Direction.NORTH));
            for (int z = 33; z <= 35; z++) for (int y = y0 + 1; y <= y0 + 3; y++) {
                set(17, y, z, facing(ModBlocks.SECURITY_DOOR_LEFT.get(), Direction.EAST).setValue(SecurityDoorBlock.OPEN, true));
                set(27, y, z, facing(ModBlocks.SECURITY_DOOR_RIGHT.get(), Direction.EAST).setValue(SecurityDoorBlock.OPEN, true));
            }
            set(18, y0 + 2, 32, facing(ModBlocks.DOOR_BUTTON_LEFT.get(), Direction.EAST));
            set(18, y0 + 2, 31, facing(ModBlocks.LIGHT_BUTTON_LEFT.get(), Direction.EAST));
            set(26, y0 + 2, 32, facing(ModBlocks.DOOR_BUTTON_RIGHT.get(), Direction.WEST));
            set(26, y0 + 2, 31, facing(ModBlocks.LIGHT_BUTTON_RIGHT.get(), Direction.WEST));
            set(16, y0 + 4, 34, facing(ModBlocks.HALL_LIGHT_LEFT.get(), Direction.WEST));
            set(28, y0 + 4, 34, facing(ModBlocks.HALL_LIGHT_RIGHT.get(), Direction.EAST));

            set(12, y0 + 3, 14, facing(ModBlocks.POSTER_CELEBRATE.get(), Direction.EAST));
            set(32, y0 + 3, 14, facing(ModBlocks.POSTER_CELEBRATE.get(), Direction.WEST));
            set(22, y0 + 4, 2, facing(ModBlocks.POSTER_CELEBRATE.get(), Direction.SOUTH));
            set(12, y0 + 3, 35, facing(ModBlocks.POSTER_ITS_ME.get(), Direction.EAST));
            set(3, y0 + 1, 14, facing(ModBlocks.PLUSH_FOXY.get(), Direction.SOUTH));
            set(3, y0 + 1, 3, facing(ModBlocks.PLUSH_BONNIE.get(), Direction.SOUTH));

            FnafMapState st = FnafMapState.get(world);
            st.nodes.clear();
            Cam[] cams = {
                    new Cam("1A", "Escenario", 22, 9, 22, 4, 22, 4, false, true), new Cam("1B", "Comedor", 12, 9, 24, 17, 22, 16, false, false),
                    new Cam("1C", "Cala del Pirata", 10, 13, 4, 18, 5, 17, false, false), new Cam("2A", "Pasillo oeste", 13, 24, 14, 34, 14, 29, false, false),
                    new Cam("2B", "Esquina pasillo oeste", 12, 30, 15, 36, 13, 36, false, false), new Cam("3", "Almacén", 5, 27, 9, 30, 8, 29, false, false),
                    new Cam("4A", "Pasillo este", 31, 24, 30, 34, 30, 29, false, false), new Cam("4B", "Esquina pasillo este", 32, 30, 29, 36, 31, 36, false, false),
                    new Cam("5", "Trastienda", 2, 2, 7, 7, 6, 6, false, false), new Cam("6", "Cocina (sólo audio)", 34, 21, 39, 25, 38, 25, true, false),
                    new Cam("7", "Baños", 34, 9, 39, 14, 38, 13, false, false)};
            for (Cam c : cams) {
                FnafMapState.Node n = new FnafMapState.Node();
                n.id = c.id; n.name = c.name; n.hasCam = true; n.audioOnly = c.audio;
                n.sx = ox + c.sx + 0.5; n.sy = y0 + (c.stage ? 2 : 1); n.sz = oz + c.sz + 0.5;
                double ex = ox + c.cx + 0.5, ey = y0 + 6.4, ez = oz + c.cz + 0.5;
                double tx = ox + c.tx + 0.5, ty = y0 + 2.2, tz = oz + c.tz + 0.5;
                double dx = tx - ex, dz = tz - ez, dh = Math.hypot(dx, dz);
                n.cyaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                n.cpitch = (float) -Math.toDegrees(Math.atan2(ty - ey, dh));
                n.cx = ex + dx / dh * 0.7; n.cy = ey; n.cz = ez + dz / dh * 0.7;
                st.nodes.put(c.id, n);
                set(c.cx, y0 + 6, c.cz, facing(ModBlocks.SECURITY_CAMERA.get(), Direction.fromYRot(n.cyaw)));
            }
            addDoorNode(st, "LEFT_DOOR", 16, 34);
            addDoorNode(st, "RIGHT_DOOR", 28, 34);
            st.office = P(22, y0 + 1, 34);
            st.setDirty();
            NightManager.spawnShowcase(world, st);
        }

        private static void opening(int x1, int z1, int x2, int z2, int height) {
            for (int x = x1; x <= x2; x++) for (int z = z1; z <= z2; z++) {
                set(x, y0, z, ModBlocks.CHECKER_TILE.get());
                for (int y = 1; y <= height; y++) set(x, y0 + y, z, Blocks.AIR);
            }
        }

        private static void addDoorNode(FnafMapState st, String id, int lx, int lz) {
            FnafMapState.Node n = new FnafMapState.Node();
            n.id = id; n.name = id;
            n.sx = ox + lx + 0.5; n.sy = y0 + 1; n.sz = oz + lz + 0.5;
            st.nodes.put(id, n);
        }
    }

    // ======================================================================== CLIENTE
    /** Datos de la noche recibidos del servidor (para el HUD). */
    public static final class ClientState {
        public static boolean active, leftDoor, rightDoor, leftLight, rightLight, powerOut;
        public static int night, ticks, usage, jumpTicks, jumpKind;
        public static float power;
        private ClientState() {}
    }

    /** Receptores de paquetes que sólo se ejecutan en el cliente. */
    public static final class ClientHooks {
        private ClientHooks() {}
        public static void onState(Net.StateMsg m) {
            ClientState.active = m.active(); ClientState.night = m.night(); ClientState.ticks = m.ticks();
            ClientState.power = m.power(); ClientState.usage = m.usage();
            ClientState.leftDoor = m.ld(); ClientState.rightDoor = m.rd(); ClientState.leftLight = m.ll(); ClientState.rightLight = m.rl();
            ClientState.powerOut = m.powerOut();
            Minecraft mc = Minecraft.getInstance();
            if (m.powerOut() && mc.screen instanceof MonitorScreen) mc.setScreen(null);
        }
        public static void onMonitor(Net.MonitorMsg m) { Minecraft.getInstance().setScreen(new MonitorScreen(m.cams())); }
        public static void onJump(Net.JumpMsg m) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof MonitorScreen) mc.setScreen(null);
            ClientState.jumpKind = m.kind(); ClientState.jumpTicks = 45;
        }
    }

    /**
     * Modelo único (textura 128x128) con variantes por animatrónico. Los UV coinciden con tools/gen_assets.py.
     * Freddy/Golden: orejas redondas, sombrero y pajarita. Bonnie/Springtrap: orejas largas. Chica: pico y babero. Foxy: hocico largo y garfio.
     */
    public static class AnimatronicModel extends EntityModel<AnimatronicEntity> {
        private static final Map<Kind, ModelLayerLocation> LAYERS = new EnumMap<>(Kind.class);
        public static ModelLayerLocation layer(Kind k) { return LAYERS.computeIfAbsent(k, kk -> new ModelLayerLocation(id(kk.id), "main")); }

        private final ModelPart root, head, body, rightArm, leftArm, rightLeg, leftLeg;

        public AnimatronicModel(ModelPart root) {
            super(RenderType::entityCutoutNoCull);
            this.root = root;
            this.head = root.getChild("head"); this.body = root.getChild("body");
            this.rightArm = root.getChild("right_arm"); this.leftArm = root.getChild("left_arm");
            this.rightLeg = root.getChild("right_leg"); this.leftLeg = root.getChild("left_leg");
        }

        public static LayerDefinition createLayer(Kind k) {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-5, -14, -3, 10, 14, 6), PartPose.offset(0, 12, 0));
            switch (k) {
                case CHICA -> body.addOrReplaceChild("bib", CubeListBuilder.create().texOffs(34, 46).addBox(-4, -12, -4, 8, 6, 1), PartPose.ZERO);
                case FOXY -> { }
                default -> body.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(34, 40).addBox(-3, -13, -5, 6, 2, 2), PartPose.ZERO);
            }
            PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -9, -4.5f, 10, 9, 9), PartPose.offset(0, -2, 0));
            switch (k) {
                case FREDDY, GOLDEN_FREDDY -> {
                    head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(40, 0).addBox(-3, -4, -7.5f, 6, 4, 3), PartPose.ZERO);
                    head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(64, 0).addBox(-6.5f, -12, -1, 4, 4, 2), PartPose.ZERO);
                    head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(2.5f, -12, -1, 4, 4, 2), PartPose.ZERO);
                    head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 20).addBox(-4, -16, -4, 8, 6, 8), PartPose.ZERO);
                    head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(34, 20).addBox(-6, -10, -6, 12, 1, 12), PartPose.ZERO);
                }
                case BONNIE, SPRINGTRAP -> {
                    head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(40, 0).addBox(-3, -3.5f, -7.5f, 6, 3, 3), PartPose.ZERO);
                    head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(64, 0).addBox(-4.5f, -18, -1, 3, 9, 2), PartPose.offsetAndRotation(0, 0, 0, 0, 0, -0.08f));
                    if (k == Kind.SPRINGTRAP)
                        head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(1.5f, -13, -1, 3, 5, 2), PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0.5f));
                    else
                        head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(1.5f, -18, -1, 3, 9, 2), PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0.08f));
                }
                case CHICA -> {
                    head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(40, 0).addBox(-3, -4, -7.5f, 6, 2, 3), PartPose.ZERO);
                    head.addOrReplaceChild("tuft_a", CubeListBuilder.create().texOffs(64, 0).addBox(-1, -12, -0.5f, 2, 3, 1), PartPose.ZERO);
                    head.addOrReplaceChild("tuft_b", CubeListBuilder.create().texOffs(64, 0).addBox(-3.5f, -11, -0.5f, 2, 3, 1), PartPose.offsetAndRotation(0, 0, 0, 0, 0, -0.4f));
                    head.addOrReplaceChild("tuft_c", CubeListBuilder.create().texOffs(64, 0).addBox(1.5f, -11, -0.5f, 2, 3, 1), PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0.4f));
                }
                case FOXY -> {
                    head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(40, 0).addBox(-2, -4, -10.5f, 4, 3, 6), PartPose.ZERO);
                    head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(64, 0).addBox(-5, -14, -1, 3, 5, 2), PartPose.offsetAndRotation(0, 0, 0, 0, 0, -0.15f));
                    head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(2, -14, -1, 3, 5, 2), PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0.15f));
                }
            }
            CubeListBuilder rArm = CubeListBuilder.create().texOffs(60, 40).addBox(-2, -1, -2, 4, 13, 4);
            if (k == Kind.FOXY) rArm.texOffs(80, 40).addBox(-1, 12, -1, 2, 6, 2);   // garfio
            root.addOrReplaceChild("right_arm", rArm, PartPose.offset(-7, -1, 0));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 40).mirror().addBox(-2, -1, -2, 4, 13, 4), PartPose.offset(7, -1, 0));
            root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 64).addBox(-2.5f, 0, -2.5f, 5, 12, 5), PartPose.offset(-2.5f, 12, 0));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 64).mirror().addBox(-2.5f, 0, -2.5f, 5, 12, 5), PartPose.offset(2.5f, 12, 0));
            return LayerDefinition.create(mesh, 128, 128);
        }

        @Override
        public void setupAnim(AnimatronicEntity e, float limbSwing, float limbDist, float age, float headYaw, float headPitch) {
            float t = age * 0.06f;
            head.yRot = headYaw * ((float) Math.PI / 180f);
            head.xRot = headPitch * ((float) Math.PI / 180f);
            head.zRot = 0; body.xRot = 0;
            rightArm.xRot = Mth.sin(t) * 0.03f; leftArm.xRot = -Mth.sin(t) * 0.03f;
            rightArm.zRot = 0.05f + Mth.cos(t * 0.8f) * 0.02f; leftArm.zRot = -0.05f - Mth.cos(t * 0.8f) * 0.02f;
            rightLeg.xRot = 0; leftLeg.xRot = 0;
            if (limbDist > 0.01f) {
                rightLeg.xRot = Mth.cos(limbSwing * 0.6662f) * 1.1f * limbDist;
                leftLeg.xRot = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 1.1f * limbDist;
                rightArm.xRot += Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 0.8f * limbDist;
                leftArm.xRot += Mth.cos(limbSwing * 0.6662f) * 0.8f * limbDist;
            }
            switch (e.kind) {
                case FOXY -> {
                    switch (e.getStage()) {
                        case 0 -> { head.xRot += 0.8f; body.xRot = 0.18f; }
                        case 1 -> head.xRot += 0.15f;
                        case 2 -> { head.xRot -= 0.1f; rightArm.xRot = -0.5f; }
                        case 3 -> { body.xRot = 0.3f; head.xRot -= 0.2f; rightArm.xRot = -1.2f; leftArm.xRot = -0.4f; }
                        default -> {
                            body.xRot = 0.65f;
                            float run = Mth.sin(age * 0.9f);
                            rightArm.xRot = -1.0f + run * 0.6f; leftArm.xRot = -1.0f - run * 0.6f;
                            rightLeg.xRot = run; leftLeg.xRot = -run;
                        }
                    }
                }
                case GOLDEN_FREDDY -> {
                    head.xRot = 0.7f; head.zRot = 0.45f; body.xRot = 0.25f;
                    rightArm.xRot = 0.2f; leftArm.xRot = 0.2f; rightArm.zRot = 0.3f; leftArm.zRot = -0.3f;
                }
                case SPRINGTRAP -> { head.zRot = Mth.sin(age * 0.11f) * 0.12f; body.xRot = 0.12f; }
                default -> head.zRot = Mth.sin(t * 0.7f) * 0.02f;
            }
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
            root.render(pose, vc, light, overlay, r, g, b, a);
        }
    }

    /** Renderer con ojos brillantes (capa fullbright). */
    public static class AnimatronicRenderer extends MobRenderer<AnimatronicEntity, AnimatronicModel> {
        private final ResourceLocation texture;
        public AnimatronicRenderer(EntityRendererProvider.Context ctx, Kind kind) {
            super(ctx, new AnimatronicModel(ctx.bakeLayer(AnimatronicModel.layer(kind))), 0.6f);
            this.texture = id("textures/entity/" + kind.id + ".png");
            RenderType eyes = RenderType.eyes(id("textures/entity/" + kind.id + "_eyes.png"));
            this.addLayer(new EyesLayer<AnimatronicEntity, AnimatronicModel>(this) {
                @Override public RenderType renderType() { return eyes; }
            });
        }
        @Override public ResourceLocation getTextureLocation(AnimatronicEntity e) { return texture; }
    }

    /** Monitor de seguridad: la vista de cada cámara es REAL (la cámara del juego se mueve al punto de la cámara). */
    public static class MonitorScreen extends Screen {
        private final List<Net.CamInfo> cams;
        private Net.CamInfo selected;
        private boolean prevHidden, started;
        private final Random rnd = new Random();

        public MonitorScreen(List<Net.CamInfo> cams) { super(Component.literal("Monitor")); this.cams = cams; }

        @Override
        protected void init() {
            if (!started) { started = true; prevHidden = minecraft.options.hideGui; minecraft.options.hideGui = true; }
            int cols = 2, bw = 34, bh = 18;
            int x0 = width - cols * (bw + 4) - 14;
            int y0 = height - ((cams.size() + 1) / cols) * (bh + 4) - 30;
            for (int i = 0; i < cams.size(); i++) {
                Net.CamInfo c = cams.get(i);
                addRenderableWidget(Button.builder(Component.literal(c.id()), b -> select(c))
                        .bounds(x0 + (i % cols) * (bw + 4), y0 + (i / cols) * (bh + 4), bw, bh).build());
            }
            if (selected == null && !cams.isEmpty()) select(cams.get(0));
        }

        private void select(Net.CamInfo c) {
            selected = c;
            if (c.audioOnly()) {
                minecraft.setCameraEntity(minecraft.player);
            } else {
                ArmorStand cam = new ArmorStand(minecraft.level, c.x(), c.y(), c.z());
                cam.setInvisible(true);
                float eye = cam.getEyeHeight(Pose.STANDING);
                cam.moveTo(c.x(), c.y() - eye, c.z(), c.yaw(), c.pitch());
                cam.setYHeadRot(c.yaw());
                minecraft.setCameraEntity(cam);
            }
            Net.CH.sendToServer(new Net.CamSelectMsg(c.id()));
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float delta) {
            if (selected != null && selected.audioOnly()) g.fill(0, 0, width, height, 0xFF000000);
            for (int i = 0; i < 45; i++) { int y = rnd.nextInt(height); g.fill(0, y, width, y + 1, 0x14FFFFFF); }   // estática
            for (int y = 0; y < height; y += 4) g.fill(0, y, width, y + 1, 0x18000000);                            // scanlines
            g.fill(0, 0, width, 5, 0xFF000000); g.fill(0, height - 5, width, height, 0xFF000000);
            g.fill(0, 0, 5, height, 0xFF000000); g.fill(width - 5, 0, width, height, 0xFF000000);
            if (selected != null) {
                g.drawString(font, "CAM " + selected.id(), 16, 14, 0xFFFFFFFF);
                g.drawString(font, selected.name(), 16, 26, 0xFFCCCCCC);
                if (selected.audioOnly()) g.drawCenteredString(font, "SÓLO AUDIO - escucha atentamente...", width / 2, height / 2, 0xFF55FF55);
            }
            if ((minecraft.level != null ? minecraft.level.getGameTime() / 12 : 0) % 2 == 0) g.fill(width - 42, 14, width - 34, 22, 0xFFFF2020);
            g.drawString(font, "REC", width - 30, 14, 0xFFFF4040);
            if (ClientState.active) {
                int h = ClientState.ticks / 1780;
                g.drawString(font, (h == 0 ? 12 : h) + " AM", width - 60, 30, 0xFFFFFFFF);
                g.drawString(font, "Energía: " + (int) Math.ceil(ClientState.power) + "%", 16, height - 22, 0xFFFFFFFF);
            }
            super.render(g, mx, my, delta);
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            if (minecraft.options.keyInventory.matches(key, scan)) { onClose(); return true; }
            return super.keyPressed(key, scan, mods);
        }

        @Override public boolean isPauseScreen() { return false; }

        @Override
        public void removed() {
            if (minecraft != null) { minecraft.setCameraEntity(minecraft.player); minecraft.options.hideGui = prevHidden; }
            Net.CH.sendToServer(new Net.MonitorClosedMsg());
        }
    }

    /** Eventos del bus MOD, sólo cliente. */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent e) {
            e.enqueueWork(() -> { for (RegistryObject<Block> b : ModBlocks.ALL.values()) ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.cutout()); });
        }
        @SubscribeEvent
        public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
            for (Kind k : Kind.values()) e.registerLayerDefinition(AnimatronicModel.layer(k), () -> AnimatronicModel.createLayer(k));
        }
        @SubscribeEvent
        public static void onRenderers(EntityRenderersEvent.RegisterRenderers e) {
            for (Kind k : Kind.values()) e.registerEntityRenderer(ModEntities.TYPES.get(k).get(), ctx -> new AnimatronicRenderer(ctx, k));
        }
        @SubscribeEvent
        public static void onOverlays(RegisterGuiOverlaysEvent e) {
            e.registerAboveAll("fnaf_hud", (gui, g, partial, w, h) -> renderHud(g, w, h));
        }
    }

    /** Eventos del bus de Forge, sólo cliente. */
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent e) {
            if (e.phase == TickEvent.Phase.END && ClientState.jumpTicks > 0) ClientState.jumpTicks--;
        }
    }

    private static final Random HUD_RND = new Random();

    private static void renderHud(GuiGraphics g, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (ClientState.jumpTicks > 0) {
            Kind k = Kind.byOrdinal(ClientState.jumpKind);
            ResourceLocation tex = id("textures/gui/jumpscare_" + k.id + ".png");
            int s = Math.max(w, h);
            int sx = HUD_RND.nextInt(31) - 15, sy = HUD_RND.nextInt(31) - 15;
            g.fill(0, 0, w, h, 0xFF000000);
            g.blit(tex, (w - s) / 2 + sx, (h - s) / 2 + sy, s, s, 0, 0, 64, 64, 64, 64);
            if (ClientState.jumpTicks % 4 < 2) g.fill(0, 0, w, h, 0x40FF0000);
            return;
        }
        if (!ClientState.active) return;
        int hour = ClientState.ticks / 1780;
        g.pose().pushPose();
        g.pose().scale(2f, 2f, 1f);
        g.drawString(mc.font, (hour == 0 ? 12 : hour) + " AM", (w - 60) / 2, 4, 0xFFFFFFFF);
        g.pose().popPose();
        g.drawString(mc.font, "Noche " + ClientState.night, w - 60, 28, 0xFFE0E0E0);

        int pw = (int) Math.ceil(ClientState.power);
        int col = pw > 40 ? 0xFFFFFFFF : pw > 20 ? 0xFFFFFF55 : 0xFFFF5555;
        g.pose().pushPose();
        g.pose().scale(1.5f, 1.5f, 1f);
        g.drawString(mc.font, "Energía: " + pw + "%", 6, (int) ((h - 50) / 1.5f), col);
        g.pose().popPose();
        g.drawString(mc.font, "Uso:", 10, h - 24, 0xFFFFFFFF);
        for (int i = 0; i < Math.min(5, ClientState.usage); i++) {
            int c = i < 2 ? 0xFF40D040 : i < 4 ? 0xFFE0D020 : 0xFFE03030;
            g.fill(36 + i * 9, h - 25, 43 + i * 9, h - 13, c);
        }
        String st = "Puerta I: " + (ClientState.leftDoor ? "§cCERRADA" : "§aabierta") + "  §fLuz I: " + (ClientState.leftLight ? "§eON" : "§7off")
                + "   §fPuerta D: " + (ClientState.rightDoor ? "§cCERRADA" : "§aabierta") + "  §fLuz D: " + (ClientState.rightLight ? "§eON" : "§7off");
        g.drawString(mc.font, st, 10, h - 40, 0xFFFFFFFF);
        if (ClientState.powerOut) g.fill(0, 0, w, h, 0xB0000000);
    }
}
