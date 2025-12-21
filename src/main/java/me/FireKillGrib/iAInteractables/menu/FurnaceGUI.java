package me.FireKillGrib.iAInteractables.menu;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.FurnaceInstance;
import me.FireKillGrib.iAInteractables.data.FurnaceRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.UpdateReason;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

@RequiredArgsConstructor
public class FurnaceGUI {
    @NotNull final Player player;
    @NotNull final Furnace furnace;
    @NotNull final FurnaceInstance instance;
    
    private Gui gui = null;
    private final Map<Character, Inventory> slotInventories = new HashMap<>();
    private Inventory resultInv;
    private BukkitTask cookingEffectsTask;

    public void open(){
        if (gui == null){
            gui = gui();
        }

        Window.single()
                .setGui(gui)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(PlaceholderAPI.setPlaceholders(player, furnace.getTitle()))))
                .setViewer(player)
                .build()
                .open();
    }

    private Gui gui(){
        resultInv = new VirtualInventory(1);
        resultInv.setPreUpdateHandler(event -> {
            if (event.isAdd() || event.isSwap()) event.setCancelled(true);
        });
        
        resultInv.setPostUpdateHandler(event -> {
            instance.setResult(event.getInventory().getItem(0));
        });

        Set<Character> inputSlots = new HashSet<>();
        for (FurnaceRecipe r : furnace.getRecipes()) {
            inputSlots.addAll(r.getRaws().keySet());
            inputSlots.addAll(r.getFuels().keySet());
        }

        for (char ch : inputSlots) {
            Inventory inv = new VirtualInventory(1);
            
            ItemStack existing = instance.getSlot(ch);
            if (existing != null) {
                inv.setItem(UpdateReason.SUPPRESSED, 0, existing.clone());
            }
            
            inv.setPostUpdateHandler(event -> {
                instance.setSlot(ch, event.getInventory().getItem(0));
                tryStartCrafting();
            });
            
            slotInventories.put(ch, inv);
        }
        
        if (instance.getResult() != null) {
            resultInv.setItem(UpdateReason.SUPPRESSED, 0, instance.getResult().clone());
        }

        Structure structure = new Structure(furnace.getStructure().toArray(new String[0]));
        structure.addIngredient('X', furnace.getFiller());
        structure.addIngredient('R', resultInv);
        
        for (Map.Entry<Character, Inventory> entry : slotInventories.entrySet()) {
            structure.addIngredient(entry.getKey(), entry.getValue());
        }

        return Gui.of(structure);
    }

    private void tryStartCrafting() {
        if (instance.isCooking()) return;
        
        Map<Character, ItemStack> current = new HashMap<>();
        for (Map.Entry<Character, Inventory> e : slotInventories.entrySet()) {
            current.put(e.getKey(), e.getValue().getItem(0));
        }
        
        for (FurnaceRecipe recipe : furnace.getRecipes()) {
            if (recipe.check(current)) {
                instance.setActiveRecipe(recipe);
                instance.setCooking(true);
                
                playStartEffects();
                
                startCookingTask(recipe);
                break;
            }
        }
    }

    private void playStartEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnStart() == null) return;
        
        Location loc = instance.getLocation().clone().add(0.5, 0.5, 0.5);
        furnace.getEffects().getOnStart().play(loc);
    }

    private void startCookingEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnCooking() == null) return;
        
        int interval = furnace.getEffects().getCookingInterval();
        
        cookingEffectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!instance.isCooking()) {
                    cancel();
                    return;
                }
                
                Location loc = instance.getLocation().clone().add(0.5, 0.5, 0.5);
                furnace.getEffects().getOnCooking().play(loc);
            }
        }.runTaskTimer(Plugin.getInstance(), interval, interval);
    }

    private void playCompleteEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnComplete() == null) return;
        
        Location loc = instance.getLocation().clone().add(0.5, 0.5, 0.5);
        furnace.getEffects().getOnComplete().play(loc);
    }

    private void startCookingTask(FurnaceRecipe recipe) {
        startCookingEffects();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (recipe == null) {
                    instance.setCooking(false);
                    stopCookingEffects();
                    return;
                }
                
                Map<Character, ItemStack> current = new HashMap<>();
                for (Map.Entry<Character, Inventory> e : slotInventories.entrySet()) {
                    current.put(e.getKey(), instance.getSlot(e.getKey()));
                }
                
                if (!recipe.check(current)) {
                    instance.setCooking(false);
                    instance.setCookProgress(0);
                    stopCookingEffects();
                    return;
                }

                for (char ch : recipe.getRaws().keySet()) {
                    ItemStack item = instance.getSlot(ch);
                    if (item != null) {
                        item.setAmount(item.getAmount() - 1);
                        instance.setSlot(ch, item);
                        
                        Inventory inv = slotInventories.get(ch);
                        if (inv != null) {
                            inv.setItem(UpdateReason.SUPPRESSED, 0, item.getAmount() > 0 ? item : null);
                        }
                    }
                }

                for (char ch : recipe.getFuels().keySet()) {
                    ItemStack item = instance.getSlot(ch);
                    if (item != null) {
                        item.setAmount(item.getAmount() - 1);
                        instance.setSlot(ch, item);
                        
                        Inventory inv = slotInventories.get(ch);
                        if (inv != null) {
                            inv.setItem(UpdateReason.SUPPRESSED, 0, item.getAmount() > 0 ? item : null);
                        }
                    }
                }

                ItemStack result = instance.getResult();
                if (result != null && result.isSimilar(recipe.getResult())) {
                    result.setAmount(result.getAmount() + recipe.getResult().getAmount());
                } else {
                    result = recipe.getResult().clone();
                }
                instance.setResult(result);
                resultInv.setItem(UpdateReason.SUPPRESSED, 0, result);

                instance.setCooking(false);
                instance.setCookProgress(0);
                
                playCompleteEffects();
                stopCookingEffects();
                
                tryStartCrafting();
            }
        }.runTaskLater(Plugin.getInstance(), recipe.getCookTimeTicks());
    }

    private void stopCookingEffects() {
        if (cookingEffectsTask != null && !cookingEffectsTask.isCancelled()) {
            cookingEffectsTask.cancel();
        }
    }
}
