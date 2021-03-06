package com.worldcretornica.plotme_core.commands;

import com.worldcretornica.plotme_core.*;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.api.event.InternalPlotProtectChangeEvent;
import net.milkbowl.vault.economy.EconomyResponse;

public class CmdProtect extends PlotCommand {

    public CmdProtect(PlotMe_Core instance) {
        super(instance);
    }

    public boolean exec(IPlayer player) {
        if (player.hasPermission(PermissionNames.ADMIN_PROTECT) || player.hasPermission("PlotMe.use.protect")) {
            IWorld world = player.getWorld();
            PlotMapInfo pmi = plugin.getPlotMeCoreManager().getMap(world);
            if (plugin.getPlotMeCoreManager().isPlotWorld(world)) {
                String id = PlotMeCoreManager.getPlotId(player);

                if (id.isEmpty()) {
                    player.sendMessage("§c" + C(MSG_NO_PLOT_FOUND));
                } else if (!PlotMeCoreManager.isPlotAvailable(id, pmi)) {
                    Plot plot = PlotMeCoreManager.getPlotById(id, pmi);

                    String name = player.getName();

                    if (plot.getOwner().equalsIgnoreCase(name) || player.hasPermission(PermissionNames.ADMIN_PROTECT)) {
                        InternalPlotProtectChangeEvent event;

                        if (plot.isProtect()) {
                            event = serverBridge.getEventFactory().callPlotProtectChangeEvent(plugin, world, plot, player, false);

                            if (!event.isCancelled()) {
                                plot.setProtect(false);
                                plugin.getPlotMeCoreManager().adjustWall(player);

                                plot.updateField("protected", false);

                                player.sendMessage(C("MsgPlotNoLongerProtected"));

                                if (isAdvancedLogging()) {
                                    serverBridge.getLogger().info(name + " " + C("MsgUnprotectedPlot") + " " + id);
                                }
                            }
                        } else {

                            double cost = 0.0;

                            if (plugin.getPlotMeCoreManager().isEconomyEnabled(pmi)) {
                                cost = pmi.getProtectPrice();

                                if (serverBridge.getBalance(player) < cost) {
                                    player.sendMessage("§c" + C("MsgNotEnoughProtectPlot"));
                                    return true;
                                } else {
                                    event = serverBridge.getEventFactory().callPlotProtectChangeEvent(plugin, world, plot, player, true);

                                    if (event.isCancelled()) {
                                        return true;
                                    } else {
                                        EconomyResponse er = serverBridge.withdrawPlayer(player, cost);

                                        if (!er.transactionSuccess()) {
                                            player.sendMessage("§c" + er.errorMessage);
                                            warn(er.errorMessage);
                                            return true;
                                        }
                                    }
                                }

                            } else {
                                event = serverBridge.getEventFactory().callPlotProtectChangeEvent(plugin, world, plot, player, true);
                            }

                            if (!event.isCancelled()) {
                                plot.setProtect(true);
                                plugin.getPlotMeCoreManager().adjustWall(player);

                                plot.updateField("protected", true);

                                double price = -cost;
                                player.sendMessage(C("MsgPlotNowProtected") + " " + Util().moneyFormat(price, true));

                                if (isAdvancedLogging()) {
                                    serverBridge.getLogger().info(name + " " + C("MsgProtectedPlot") + " " + id);
                                }
                            }
                        }
                    } else {
                        player.sendMessage("§c" + C("MsgDoNotOwnPlot"));
                    }
                } else {
                    player.sendMessage("§c" + C("MsgThisPlot") + "(" + id + ") " + C("MsgHasNoOwner"));
                }
            } else {
                player.sendMessage("§c" + C("MsgNotPlotWorld"));
                return true;
            }
        } else {
            player.sendMessage("§c" + C("MsgPermissionDenied"));
            return false;
        }
        return true;
    }
}
