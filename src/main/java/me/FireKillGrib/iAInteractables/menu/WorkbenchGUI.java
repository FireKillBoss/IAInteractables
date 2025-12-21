package me.FireKillGrib.iAInteractables.menu;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.data.WorkbenchRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.UpdateReason;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class WorkbenchGUI {
    @NotNull final Player player;
    @NotNull final Workbench workbench;
    private Gui gui = null;
    private final static Set<WorkbenchGUI> guis = new HashSet<>();
    
    public void open(){
        guis.add(this);
        if (gui == null){
            gui = gui();
        }
        Window.single()
                .setGui(gui)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(PlaceholderAPI.setPlaceholders(player,workbench.getTitle()))))
                .setViewer(player)
                .build()
                .open();
    }
    
    private Gui gui(){
        int count = (int) workbench.getStructure().stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .filter("O"::equals)
                .count();
        
        Inventory resultInv = new VirtualInventory(1);
        Inventory inv = new VirtualInventory(count);
        
        resultInv.setPreUpdateHandler(event -> {
            if (event.isAdd() || event.isSwap()) event.setCancelled(true);
            if (event.isRemove()) {
                playCraftEffects();
                
                for (int i = 0; i < inv.getItems().length; i ++) {
                    inv.setItemSilently(i, null);
                }
            }
        });
        
        inv.setPostUpdateHandler(event -> {
            WorkbenchRecipe matchedRecipe = null;
            
            for (WorkbenchRecipe recipe : workbench.getRecipes()) {
                if (recipe.check(event.getInventory())) {
                    matchedRecipe = recipe;
                    break;
                }
            }
            
            if (matchedRecipe != null) {
                resultInv.setItem(UpdateReason.SUPPRESSED, 0, matchedRecipe.getResult());
            } else {
                resultInv.setItem(UpdateReason.SUPPRESSED, 0, null);
            }
        });
        
        Structure structure = new Structure(workbench.getStructure().toArray(new String[0]));
        structure.addIngredient('O', inv);
        structure.addIngredient('X', workbench.getFiller());
        structure.addIngredient('R', resultInv);
        return Gui.of(structure);
    }
    
    private void playCraftEffects() {
        if (workbench.getEffects() == null) return;
        if (workbench.getEffects().getOnCraft() == null) return;
        
        Location loc = player.getLocation();
        workbench.getEffects().getOnCraft().play(player, loc);
    }
    
    public static WorkbenchGUI getGui(Player player, Workbench workbench){
        for (WorkbenchGUI gui: guis){
            if (gui.workbench == workbench && gui.player == player){
                return gui;
            }
        }
        return new WorkbenchGUI(player,workbench);
    }
}
