package net.asd417.tutorialmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.LoggedPrintStream;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity {
    public final ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        public int getStackLimit(int slot, ItemStack stack) {
            return 1; // should match the maxItem output below
        }
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if(!level.isClientSide()){
                level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
            }
        }
    };
    public PedestalBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PEDESTAL_BE.get(), pos, blockState);
    }

    private float rotation;
    public float getRenderingRotation() {
        rotation += 0.5f;
        if(rotation >= 360){
            rotation = 0;
        }
        return rotation;
    }

    public void clearContents(){
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }
    public int maxItem(){
        return 1; // should match the getStackLimit output above
    }
    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        //System.out.println("DROPPING FROM PEDESTALS");
        for(int i = 0;i<inventory.getSlots();i++){
            ItemStack s = inventory.getStackInSlot(i);
            int c = s.getCount();
            //System.out.printf("Slot %d has item count: %d", i, c);
            inv.setItem(i,s);
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
